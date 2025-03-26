package com.example

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class LoggerProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 1. 특정 패키지의 모든 클래스 탐색
        val classes = resolver.getAllFiles().distinctBy { it.fileName }.flatMap { file ->
            println(file.packageName)
            println(file.fileName)
            file.declarations.filterIsInstance<KSClassDeclaration>()
        }

        // 2. 각 클래스에 대해 로거 변수를 생성하는 코드 파일 생성
        classes.forEach { classDeclaration ->
            generateLogger(classDeclaration)
        }
        return emptyList()
    }

    private fun generateLogger(classDeclaration: KSClassDeclaration) {
        // 3. 로거 변수 생성 코드 작성
        val className = classDeclaration.simpleName.asString()
        val loggerCode = """
            package ${classDeclaration.packageName.asString()}
            
            import io.github.oshai.kotlinlogging.KLogger
            import io.github.oshai.kotlinlogging.KotlinLogging
            
            val ${className}.log: KLogger
                get() = KotlinLogging.logger {}
        """.trimIndent()

        // 4. build 디렉터리에 파일 생성
        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            "${className}Logger"
        ).bufferedWriter().use { writer ->
            writer.write(loggerCode)
        }
    }
}
