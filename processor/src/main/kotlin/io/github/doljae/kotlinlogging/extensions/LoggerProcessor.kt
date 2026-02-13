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
import com.google.devtools.ksp.symbol.Visibility
import io.github.doljae.common.StringUtility.wrapReservedWords

class LoggerProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classes =
            resolver
                .getNewFiles()
                .flatMap { file ->
                    file.declarations.flatMap { declaration ->
                        findAllClasses(declaration)
                    }
                }

        classes.forEach { classDeclaration ->
            generateLogger(classDeclaration)
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

    private fun generateLogger(classDeclaration: KSClassDeclaration) {
        if (classDeclaration.qualifiedName == null) return
        if (classDeclaration.classKind == ClassKind.ENUM_ENTRY) return

        val visibility = getVisibilityModifier(classDeclaration) ?: return
        val receiverDeclaration = buildReceiverDeclaration(classDeclaration)
        
        val rawPackageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName!!.asString()
        val className = if (rawPackageName.isEmpty()) {
            qualifiedName
        } else {
            qualifiedName.substring(rawPackageName.length + 1)
        }
        
        val fileName = "${className.replace('.', '_')}KotlinLoggingExtensions"

        val safePackageName = wrapReservedWords(
            target = rawPackageName,
            delimiter = '.',
            reservedWords = hardKeywords,
            quoteChar = '`',
        )

        val loggerCode =
            """
            package $safePackageName
            
            import io.github.oshai.kotlinlogging.KLogger
            import io.github.oshai.kotlinlogging.KotlinLogging
            
            ${visibility}val ${receiverDeclaration.typeParameters}${receiverDeclaration.receiverType}.log: KLogger
                get() = KotlinLogging.logger("$qualifiedName")
            """.trimIndent()

        codeGenerator
            .createNewFile(
                Dependencies(false, classDeclaration.containingFile!!),
                rawPackageName,
                fileName,
            ).bufferedWriter()
            .use { writer ->
                writer.write(loggerCode)
            }
    }

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
