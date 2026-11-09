package io.github.doljae.kotlinlogging.extensions

import com.google.devtools.ksp.KspExperimental
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
import com.google.devtools.ksp.symbol.Modifier
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

    /**
     * Distinguishes this compilation's generated file from the one another source set produces for
     * the same package. Set on every [process] call, before any file is written.
     */
    private var moduleDiscriminator: String = ""

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        moduleDiscriminator = discriminatorFor(resolver.getModuleName().asString())

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
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() in LOGGER_GENERATION_ANNOTATIONS
        }
    }

    private fun KSClassDeclaration.shouldAutoLog(): Boolean {
        if (generationMode == LoggerGenerationMode.ALL) {
            return true
        }

        return isGenerationTarget()
    }

    /**
     * True when this class is selected by the configured mode, or inherits from a class that is.
     *
     * An extension receiver accepts subtypes, so a `log` generated for a superclass also resolves
     * inside its subclasses — under the *superclass's* name. [LoggerGenerationMode.ALL] never shows
     * this, because the subclass has an extension of its own and the more specific receiver wins. In
     * the other modes a subclass that is not itself selected would silently log under its
     * superclass's name, which is the one thing this library exists to get right. So a subclass of a
     * target is a target.
     *
     * That grants no logging the class did not already have — it only stops it reporting a name that
     * is not its own.
     *
     * A superclass from a dependency cannot be recognised by annotation: [Log] is `SOURCE`-retained,
     * so it is not in the class file. Only [LoggerGenerationMode.PACKAGE_SCAN] still matches those,
     * by package name.
     */
    private fun KSClassDeclaration.isGenerationTarget(): Boolean {
        if (isSelectedByMode()) {
            return true
        }

        return superTypes.any { superTypeReference ->
            val superType = superTypeReference.resolve().declaration as? KSClassDeclaration ?: return@any false
            // Every class lists Any, so treating it as a target would select the whole module.
            superType.qualifiedName?.asString() != ANY_QUALIFIED_NAME && superType.isGenerationTarget()
        }
    }

    /** Whether the configured mode selects this class on its own, ignoring what it inherits from. */
    private fun KSClassDeclaration.isSelectedByMode(): Boolean {
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

        return LoggerExtension(
            packageName = classDeclaration.packageName.asString(),
            qualifiedName = qualifiedName,
            containingFile = containingFile,
            declaration =
                """
                ${visibility}val ${buildReceiverType(classDeclaration)}.log: KLogger
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
        val baseName = "$GENERATED_FILE_NAME$moduleDiscriminator"
        val fileName = if (writeCount == 1) baseName else "$baseName$writeCount"

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

    /**
     * The visibility the generated extension must carry, or `null` when no extension can be generated
     * for this class.
     *
     * Extensions live in a separate per-package file, so the receiver has to be visible from another
     * file: a `private` class is not (file-private at top level, class-private when nested), and
     * neither is a `protected` or local one. Generating for those produces a file that does not
     * compile, so they are skipped instead. `internal` anywhere in the chain caps the extension at
     * `internal`.
     */
    private fun getVisibilityModifier(classDeclaration: KSClassDeclaration): String? {
        var current: KSDeclaration? = classDeclaration
        var isInternal = false

        while (current is KSClassDeclaration) {
            when (current.getVisibility()) {
                Visibility.PRIVATE -> return null
                Visibility.PROTECTED -> return null
                Visibility.LOCAL -> return null
                Visibility.INTERNAL -> isInternal = true
                else -> {}
            }
            current = current.parentDeclaration
        }

        return if (isInternal) "internal " else ""
    }

    /**
     * The receiver as it must be written in the generated file — `Outer.Nested`, `Generic<*>`,
     * `Outer<*>.Inner<*>`.
     *
     * Type parameters are star-projected instead of being redeclared on the extension. Redeclaring
     * them means restating their bounds too, and a bound the extension does not repeat is a compile
     * error (`class Box<T : Any>` rejects an unbounded `T`). Every instantiation is a subtype of the
     * star projection, so `log` still resolves on `Box<String>` and inside the class body.
     *
     * Type arguments belong to a qualifier only when the segment to its right is an `inner` class; a
     * plain nested class is referenced through the bare outer name, and spelling the arguments there
     * is an error rather than a redundancy. Kotlin forbids a non-inner class inside an `inner` one, so
     * a qualifier can never need arguments that a segment further right has already dropped.
     */
    private fun buildReceiverType(classDeclaration: KSClassDeclaration): String {
        val classChain =
            generateSequence(classDeclaration as KSDeclaration?) { it.parentDeclaration }
                .filterIsInstance<KSClassDeclaration>()
                .toList()
                .asReversed()

        return classChain
            .mapIndexed { index, declaration ->
                val nextIsInner = classChain.getOrNull(index + 1)?.modifiers?.contains(Modifier.INNER) == true
                val carriesTypeArguments = index == classChain.lastIndex || nextIsInner

                val simpleName = declaration.simpleName.asString()
                if (carriesTypeArguments && declaration.typeParameters.isNotEmpty()) {
                    "$simpleName<${declaration.typeParameters.joinToString(", ") { "*" }}>"
                } else {
                    simpleName
                }
            }.joinToString(".")
    }

    companion object {
        public const val GENERATION_MODE_OPTION_KEY: String = "kotlinloggingextensions.mode"
        public const val PACKAGE_SCAN_TARGETS_OPTION_KEY: String = "kotlinloggingextensions.targets"
        public const val LEGACY_PACKAGE_PREFIXES_OPTION_KEY: String =
            "kotlinloggingextensions.autoGeneratePackagePrefixes"

        /** Base name of the per-package generated file, before the module discriminator. */
        public const val GENERATED_FILE_NAME: String = "KotlinLoggingExtensions"

        /**
         * Turns a KSP module name into a file-name suffix that separates this compilation's output
         * from every other source set's.
         *
         * A file holding top-level declarations compiles to a JVM class named after the file, so two
         * source sets that share a package and a file name produce the same class twice. The test
         * compilation's copy then shadows the main one on the runtime classpath and every `log` in
         * main throws `NoSuchMethodError` — at runtime only, because compilation sees main's classes
         * on the classpath and resolves fine.
         *
         * The module name is the only thing KSP exposes that differs between source sets: Gradle
         * reports `group:project` for main and `group:project_test` for the test compilation. Only the
         * part after the last `:` is used, and anything not legal in an identifier is replaced.
         */
        internal fun discriminatorFor(moduleName: String): String {
            val withoutGroup = moduleName.substringAfterLast(':')
            val sanitized = withoutGroup.map { character -> if (character.isLetterOrDigit()) character else '_' }.joinToString("")
            return if (sanitized.isEmpty()) "" else "_$sanitized"
        }
        /**
         * Matched by qualified name only, so the processor needs no compile dependency on the
         * annotations artifact.
         *
         * `AutoLog` is the pre-3.0.0 name and stays here for as long as its deprecation lasts. It has
         * to be a genuine annotation class on the consumer's side rather than a `typealias` — KSP
         * reports an aliased annotation under the alias's own name, so a shim would resolve to
         * something that is not in this set and generate nothing, silently.
         */
        private val LOGGER_GENERATION_ANNOTATIONS =
            setOf(
                "io.github.doljae.kotlinlogging.extensions.Log",
                "io.github.doljae.kotlinlogging.extensions.AutoLog",
            )

        private const val ANY_QUALIFIED_NAME = "kotlin.Any"

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
