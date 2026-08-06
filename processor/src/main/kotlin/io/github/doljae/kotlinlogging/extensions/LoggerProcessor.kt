package io.github.doljae.kotlinlogging.extensions

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Visibility
import io.github.doljae.common.StringUtility.wrapReservedWords

class LoggerProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val generationMode: LoggerGenerationMode = LoggerGenerationMode.ALL,
    private val packageScanTargetPatterns: Set<String> = emptySet(),
) : SymbolProcessor {
    /**
     * Counts how many files have already been written for a package. A package is normally written
     * once, but [process] may run over several rounds, and [CodeGenerator.createNewFile] cannot write
     * the same path twice. A package seen again in a later round therefore gets a numbered file rather
     * than losing its extensions.
     */
    private val writtenFilesPerPackage = mutableMapOf<String, Int>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classes =
            resolver
                .getNewFiles()
                .flatMap { file ->
                    file.declarations.flatMap { declaration ->
                        findAllClasses(declaration)
                    }
                }
                .filter { classDeclaration ->
                    classDeclaration.shouldAutoLog()
                }
                .filterNot { classDeclaration ->
                    classDeclaration.hasDeclaredLogProperty()
                }

        classes
            .mapNotNull { classDeclaration -> buildExtension(classDeclaration) }
            .groupBy { extension -> extension.packageName }
            .toSortedMap()
            .forEach { (packageName, extensions) ->
                writePackageFile(packageName, extensions)
            }

        return emptyList()
    }

    private fun findAllClasses(declaration: KSDeclaration): Sequence<KSClassDeclaration> {
        return sequence {
            if (declaration is KSClassDeclaration) {
                yield(declaration)
                declaration.declarations.forEach {
                    yieldAll(findAllClasses(it))
                }
            }
        }
    }

    private fun KSClassDeclaration.hasLoggerGenerationAnnotation(): Boolean {
        return annotations.any { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == LOGGER_GENERATION_ANNOTATION
        }
    }

    private fun KSClassDeclaration.shouldAutoLog(): Boolean {
        if (generationMode == LoggerGenerationMode.ALL) {
            return true
        }

        if (hasLoggerGenerationAnnotation()) {
            return true
        }

        if (generationMode != LoggerGenerationMode.PACKAGE_SCAN || packageScanTargetPatterns.isEmpty()) {
            return false
        }

        val declarationPackageName = packageName.asString()
        return packageScanTargetPatterns.any { targetPattern ->
            if (targetPattern.endsWith(".*")) {
                val packagePrefix = targetPattern.removeSuffix(".*")
                packagePrefix.isNotEmpty() &&
                    (declarationPackageName == packagePrefix || declarationPackageName.startsWith("$packagePrefix."))
            } else {
                declarationPackageName == targetPattern
            }
        }
    }

    private fun KSClassDeclaration.hasDeclaredLogProperty(): Boolean {
        fun KSClassDeclaration.declaredLogPropertyExists(): Boolean {
            return declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .any { property ->
                    property.simpleName.asString() == "log"
                }
        }

        if (declaredLogPropertyExists()) {
            return true
        }

        return declarations
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { nestedClass ->
                nestedClass.isCompanionObject
            }
            ?.declaredLogPropertyExists()
            ?: false
    }

    /**
     * A generated `log` extension wins over a top-level `log` in the same file: inside the class the
     * implicit receiver sits at an inner scope level than the file scope, so the extension is found
     * first and the top-level property is never reached. This is silent — no ambiguity, no warning
     * from the compiler — so report it here.
     *
     * Generation is still correct and must not be skipped: top-level functions in the same file have
     * no implicit receiver and keep resolving to the top-level property, so a top-level `log` is not a
     * signal that the file's classes want no logger.
     */
    private fun warnIfTopLevelLogIsShadowed(classDeclaration: KSClassDeclaration) {
        val hasTopLevelLog =
            classDeclaration.containingFile
                ?.declarations
                ?.filterIsInstance<KSPropertyDeclaration>()
                ?.any { property -> property.simpleName.asString() == "log" }
                ?: false

        if (!hasTopLevelLog) return

        logger.warn(
            "Top-level 'log' in this file is shadowed inside ${classDeclaration.simpleName.asString()}: " +
                "'log' there resolves to the generated extension, not the top-level property. " +
                "Rename one of them if that is not intended.",
            classDeclaration,
        )
    }

    private fun buildExtension(classDeclaration: KSClassDeclaration): LoggerExtension? {
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: return null
        if (classDeclaration.classKind == ClassKind.ENUM_ENTRY) return null

        val visibility = getVisibilityModifier(classDeclaration) ?: return null
        val containingFile = classDeclaration.containingFile ?: return null

        warnIfTopLevelLogIsShadowed(classDeclaration)

        val receiverDeclaration = buildReceiverDeclaration(classDeclaration)

        return LoggerExtension(
            packageName = classDeclaration.packageName.asString(),
            qualifiedName = qualifiedName,
            containingFile = containingFile,
            declaration =
                """
                ${visibility}val ${receiverDeclaration.typeParameters}${receiverDeclaration.receiverType}.log: KLogger
                    get() = KotlinLogging.logger("$qualifiedName")
                """.trimIndent(),
        )
    }

    /**
     * Writes every extension of a package into a single file. The extension must live in the target
     * class's own package so that `log` resolves without an import, and a Kotlin file declares exactly
     * one package — so one file per package is the smallest number of files reachable without forcing
     * users to add imports.
     */
    private fun writePackageFile(packageName: String, extensions: List<LoggerExtension>) {
        // Sorted so that identical sources always produce byte-identical output.
        val sortedExtensions = extensions.sortedBy { extension -> extension.qualifiedName }

        val safePackageName =
            wrapReservedWords(
                target = packageName,
                delimiter = '.',
                reservedWords = hardKeywords,
                quoteChar = '`',
            )

        val fileContent =
            buildString {
                if (safePackageName.isNotEmpty()) {
                    appendLine("package $safePackageName")
                    appendLine()
                }
                appendLine("import io.github.oshai.kotlinlogging.KLogger")
                appendLine("import io.github.oshai.kotlinlogging.KotlinLogging")
                sortedExtensions.forEach { extension ->
                    appendLine()
                    appendLine(extension.declaration)
                }
            }.trimEnd()

        val writeCount = writtenFilesPerPackage.merge(packageName, 1, Int::plus)!!
        val fileName =
            if (writeCount == 1) GENERATED_FILE_NAME else "$GENERATED_FILE_NAME$writeCount"

        codeGenerator
            .createNewFile(
                // Aggregating: this file holds extensions from every source file in the package, so any
                // source change must regenerate it. An isolating dependency would drop the entries of
                // files that were not themselves reprocessed.
                Dependencies(
                    aggregating = true,
                    sources = sortedExtensions.map { it.containingFile }.distinct().toTypedArray(),
                ),
                packageName,
                fileName,
            ).bufferedWriter()
            .use { writer ->
                writer.write(fileContent)
            }
    }

    private data class LoggerExtension(
        val packageName: String,
        val qualifiedName: String,
        val containingFile: KSFile,
        val declaration: String,
    )

    private fun getVisibilityModifier(classDeclaration: KSClassDeclaration): String? {
        var current: KSDeclaration? = classDeclaration
        var isInternal = false
        
        while (current is KSClassDeclaration) {
            when (current.getVisibility()) {
                Visibility.PRIVATE -> return "private "
                Visibility.PROTECTED -> return null // Cannot generate top-level extension for protected class
                Visibility.LOCAL -> return null
                Visibility.INTERNAL -> isInternal = true
                else -> {}
            }
            current = current.parentDeclaration
        }
        
        return if (isInternal) "internal " else ""
    }

    private fun buildReceiverDeclaration(classDeclaration: KSClassDeclaration): ReceiverDeclaration {
        val classChain =
            generateSequence(classDeclaration as KSDeclaration?) { it.parentDeclaration }
                .filterIsInstance<KSClassDeclaration>()
                .toList()
                .asReversed()

        val usedTypeParameterNames = mutableMapOf<String, Int>()
        val declaredTypeParameters = mutableListOf<String>()
        val receiverSegments =
            classChain.map { declaration ->
                val receiverTypeParameters =
                    declaration.typeParameters.map { typeParameter ->
                        val baseName = typeParameter.name.asString()
                        val index = usedTypeParameterNames.getOrDefault(baseName, 0) + 1
                        usedTypeParameterNames[baseName] = index
                        val uniqueName = if (index == 1) baseName else "$baseName$index"
                        declaredTypeParameters += uniqueName
                        uniqueName
                    }

                val simpleName = declaration.simpleName.asString()
                if (receiverTypeParameters.isEmpty()) {
                    simpleName
                } else {
                    "$simpleName<${receiverTypeParameters.joinToString(", ")}>"
                }
            }

        val typeParameters =
            if (declaredTypeParameters.isEmpty()) {
                ""
            } else {
                "<${declaredTypeParameters.joinToString(", ")}> "
            }

        return ReceiverDeclaration(
            typeParameters = typeParameters,
            receiverType = receiverSegments.joinToString("."),
        )
    }

    private data class ReceiverDeclaration(
        val typeParameters: String,
        val receiverType: String,
    )

    companion object {
        public const val GENERATION_MODE_OPTION_KEY: String = "kotlinloggingextensions.mode"
        public const val PACKAGE_SCAN_TARGETS_OPTION_KEY: String = "kotlinloggingextensions.targets"
        public const val LEGACY_PACKAGE_PREFIXES_OPTION_KEY: String =
            "kotlinloggingextensions.autoGeneratePackagePrefixes"

        /** Base name of the per-package generated file. */
        public const val GENERATED_FILE_NAME: String = "KotlinLoggingExtensions"
        private const val LOGGER_GENERATION_ANNOTATION = "io.github.doljae.kotlinlogging.extensions.AutoLog"

        // Ref: https://kotlinlang.org/docs/keyword-reference.html
        private val hardKeywords =
            setOf(
                "as",
                "break",
                "class",
                "continue",
                "do",
                "else",
                "false",
                "for",
                "fun",
                "if",
                "in",
                "interface",
                "is",
                "null",
                "object",
                "package",
                "return",
                "super",
                "this",
                "throw",
                "true",
                "try",
                "typealias",
                "typeof",
                "val",
                "var",
                "when",
                "while",
            )
    }
}
