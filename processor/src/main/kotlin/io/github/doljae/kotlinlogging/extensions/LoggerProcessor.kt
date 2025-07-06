package io.github.doljae.kotlinlogging.extensions

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class LoggerProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
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

        fun String.wrapReservedWords(
            delimiter: Char,
            reservedWords: Collection<String>,
            quoteChar: Char,
        ): String {
            val tokens = split(delimiter)
            return tokens.joinToString(delimiter.toString()) { token ->
                if (reservedWords.contains(token)) {
                    "$quoteChar$token$quoteChar"
                } else {
                    token
                }
            }
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classes =
            resolver
                .getNewFiles()
                .flatMap { file -> file.declarations.filterIsInstance<KSClassDeclaration>() }

        classes.forEach { classDeclaration ->
            generateLogger(classDeclaration)
        }

        return emptyList()
    }

    private fun generateLogger(classDeclaration: KSClassDeclaration) {
        val className = classDeclaration.simpleName.asString()
        val loggerCode =
            """
            package ${classDeclaration.packageName.asString()}
            
            import io.github.oshai.kotlinlogging.KLogger
            import io.github.oshai.kotlinlogging.KotlinLogging
            
            val $className.log: KLogger
                get() = KotlinLogging.logger("${classDeclaration.qualifiedName!!.asString()}")
            """.trimIndent()

        codeGenerator
            .createNewFile(
                Dependencies(false),
                classDeclaration.packageName.asString()
                    .wrapReservedWords(delimiter = '.', reservedWords = keywords, quoteChar = '`'),
                "${className}KotlinLoggingExtensions",
            ).bufferedWriter()
            .use { writer ->
                writer.write(loggerCode)
            }
    }
}
