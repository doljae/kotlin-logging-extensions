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
                classDeclaration.packageName.asString(),
                "${className}KotlinLoggingExtensions",
            ).bufferedWriter()
            .use { writer ->
                writer.write(loggerCode)
            }
    }
}
