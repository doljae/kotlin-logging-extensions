package io.github.doljae.kotlinlogging.extensions

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class InnerClassSupportTest {

    @Test
    fun `should generate log extension for nested class`() {
        val source = SourceFile.kotlin(
            "NestedClasses.kt",
            """
            package com.example

            class Outer {
                class Nested {
                    fun foo() {}
                }
            }
            """.trimIndent()
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            configureKsp {
                symbolProcessorProviders += LoggerProcessorProvider()
            }
            inheritClassPath = true
        }

        val result = compilation.compile()
        // result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        // We expect a file for Nested class with unique name
        val files = compilation.kspSourcesDir.walkTopDown().toList()
        val generatedFile = files.find { it.name == "Outer_NestedKotlinLoggingExtensions.kt" }
        
        generatedFile?.exists() shouldBe true
        
        val content = generatedFile?.readText() ?: ""
        
        // This is what we check for: verify if it generates correct code for Nested class
        content shouldContain "val Outer.Nested.log: KLogger"
        content shouldContain "KotlinLogging.logger(\"com.example.Outer.Nested\")"
    }

    @Test
    fun `should generate log extension for nested generic class`() {
        val source =
            SourceFile.kotlin(
                "NestedGenericClasses.kt",
                """
                package com.example

                class Outer<T> {
                    class Nested<U>
                }
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "Outer_NestedKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        val content = generatedFile?.readText() ?: ""
        content shouldContain "val <T, U> Outer<T>.Nested<U>.log: KLogger"
        content shouldContain "KotlinLogging.logger(\"com.example.Outer.Nested\")"
    }
}
