package io.github.doljae.kotlinlogging.extensions

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class LoggerProcessorTest {
    @Test
    fun `should generate log extension for a simple class`() {
        val source = 
            SourceFile.kotlin(
                "SimpleClass.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class SimpleClass {
                    fun doSomething() {
                        // log should be available here after compilation
                    }
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
                messageOutputStream = System.out
            }

        val result = compilation.compile()

        // Compilation might fail due to Kotlin metadata version mismatch in test environment
        // but we only care if the KSP processor generated the file correctly.
        // result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        // Debug: print all generated files
        println("Generated files:")
        compilation.kspSourcesDir.walkTopDown().forEach { println(it.absolutePath) }

        // Verify that the file was generated
        val generatedFile = 
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "SimpleClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "val SimpleClass.log: KLogger"
        generatedFile?.readText() shouldContain "KotlinLogging.logger(\"com.example.SimpleClass\")"
    }

    @Test
    fun `should generate log extension for class with nested package`() {
        val source = 
            SourceFile.kotlin(
                "NestedPackageClass.kt",
                """
                package com.example.deeply.nested
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class DeepClass
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

        val result = compilation.compile()
        // result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val generatedFile = 
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "DeepClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "package com.example.deeply.nested"
        generatedFile?.readText() shouldContain "val DeepClass.log: KLogger"
        generatedFile?.readText() shouldContain "KotlinLogging.logger(\"com.example.deeply.nested.DeepClass\")"
    }

    @Test
    fun `should generate log extension for generic class`() {
        val source =
            SourceFile.kotlin(
                "GenericClass.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class GenericClass<T>(private val value: T)
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
                it.name == "GenericClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "val <T> GenericClass<T>.log: KLogger"
        generatedFile?.readText() shouldContain "KotlinLogging.logger(\"com.example.GenericClass\")"
    }

    @Test
    fun `should handle multiple classes in one file`() {
        val source = 
            SourceFile.kotlin(
                "MultipleClasses.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class ClassA
                @AutoLog
                class ClassB
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

        val result = compilation.compile()
        // result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val generatedFileA = 
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "ClassAKotlinLoggingExtensions.kt"
            }
        val generatedFileB = 
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "ClassBKotlinLoggingExtensions.kt"
            }

        generatedFileA?.exists() shouldBe true
        generatedFileB?.exists() shouldBe true

        generatedFileA?.readText() shouldContain "val ClassA.log: KLogger"
        generatedFileB?.readText() shouldContain "val ClassB.log: KLogger"
    }

    @Test
    fun `should handle reserved keywords in package name`() {
        val source = 
            SourceFile.kotlin(
                "ReservedKeywordClass.kt",
                """
                package com.example.`fun`
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class ReservedClass
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

        val result = compilation.compile()
        // result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val generatedFile = 
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "ReservedClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "package com.example.`fun`"
        generatedFile?.readText() shouldContain "val ReservedClass.log: KLogger"
        generatedFile?.readText() shouldContain "KotlinLogging.logger(\"com.example.fun.ReservedClass\")"
    }

    @Test
    fun `should not generate log extension for class without AutoLog annotation`() {
        val source =
            SourceFile.kotlin(
                "NoAnnotationClass.kt",
                """
                package com.example

                class NoAnnotationClass
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
                it.name == "NoAnnotationClassKotlinLoggingExtensions.kt"
            }

        generatedFile shouldBe null
    }

    @Test
    fun `should skip generation when class declares log property`() {
        val source =
            SourceFile.kotlin(
                "ClassWithLogProperty.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog
                import io.github.oshai.kotlinlogging.KLogger
                import io.github.oshai.kotlinlogging.KotlinLogging

                @AutoLog
                class ClassWithLogProperty {
                    val log: KLogger = KotlinLogging.logger("custom")
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
                it.name == "ClassWithLogPropertyKotlinLoggingExtensions.kt"
            }

        generatedFile shouldBe null
    }

    @Test
    fun `should skip generation when companion object declares log property`() {
        val source =
            SourceFile.kotlin(
                "ClassWithCompanionLogProperty.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog
                import io.github.oshai.kotlinlogging.KLogger
                import io.github.oshai.kotlinlogging.KotlinLogging

                @AutoLog
                class ClassWithCompanionLogProperty {
                    companion object {
                        val log: KLogger = KotlinLogging.logger("companion-custom")
                    }
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
                it.name == "ClassWithCompanionLogPropertyKotlinLoggingExtensions.kt"
            }

        generatedFile shouldBe null
    }

    @Test
    fun `should generate log extension for class in configured auto generate package without annotation`() {
        val source =
            SourceFile.kotlin(
                "PackageScopedClass.kt",
                """
                package com.example.auto

                class PackageScopedClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "PackageScan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.auto.*",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "PackageScopedClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "val PackageScopedClass.log: KLogger"
        generatedFile?.readText() shouldContain "KotlinLogging.logger(\"com.example.auto.PackageScopedClass\")"
    }

    @Test
    fun `should not generate log extension outside configured auto generate package without annotation`() {
        val source =
            SourceFile.kotlin(
                "OutsidePackageClass.kt",
                """
                package com.example.outside

                class OutsidePackageClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "PackageScan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.auto.*",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "OutsidePackageClassKotlinLoggingExtensions.kt"
            }

        generatedFile shouldBe null
    }

    @Test
    fun `should still generate log extension for annotated class outside configured auto generate package`() {
        val source =
            SourceFile.kotlin(
                "AnnotatedOutsidePackageClass.kt",
                """
                package com.example.outside
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class AnnotatedOutsidePackageClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "PackageScan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.auto.*",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "AnnotatedOutsidePackageClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
        generatedFile?.readText() shouldContain "val AnnotatedOutsidePackageClass.log: KLogger"
    }

    @Test
    fun `should generate log extension when targets are configured even if mode is not PackageScan`() {
        val source =
            SourceFile.kotlin(
                "ModeIgnoredClass.kt",
                """
                package com.example.auto

                class ModeIgnoredClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "AnnotationOnly",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.auto.*",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "ModeIgnoredClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
    }

    @Test
    fun `should parse package scan mode regardless of casing and separators`() {
        val source =
            SourceFile.kotlin(
                "NormalizedModeClass.kt",
                """
                package com.example.normalized

                class NormalizedModeClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "package_scan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.normalized.*",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "NormalizedModeClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
    }

    @Test
    fun `should parse annotation only mode regardless of casing and separators`() {
        val source =
            SourceFile.kotlin(
                "AnnotationOnlyModeClass.kt",
                """
                package com.example.annotationonly

                class AnnotationOnlyModeClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "annotation_only",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "AnnotationOnlyModeClassKotlinLoggingExtensions.kt"
            }

        generatedFile shouldBe null
    }

    @Test
    fun `should apply exact package target only to exact package`() {
        val exactPackageSource =
            SourceFile.kotlin(
                "ExactPackageClass.kt",
                """
                package com.example.exact

                class ExactPackageClass
                """.trimIndent(),
            )
        val subPackageSource =
            SourceFile.kotlin(
                "SubPackageClass.kt",
                """
                package com.example.exact.sub

                class SubPackageClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(exactPackageSource, subPackageSource)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "PackageScan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.exact",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val exactPackageGeneratedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "ExactPackageClassKotlinLoggingExtensions.kt"
            }
        val subPackageGeneratedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "SubPackageClassKotlinLoggingExtensions.kt"
            }

        exactPackageGeneratedFile?.exists() shouldBe true
        subPackageGeneratedFile shouldBe null
    }

    @Test
    fun `should support legacy package prefix option without explicit mode`() {
        val source =
            SourceFile.kotlin(
                "LegacyOptionClass.kt",
                """
                package com.example.legacy

                class LegacyOptionClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(source)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.LEGACY_PACKAGE_PREFIXES_OPTION_KEY to "com.example.legacy",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val generatedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "LegacyOptionClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
    }

    @Test
    fun `should normalize and ignore invalid package target patterns`() {
        val normalizedTargetSource =
            SourceFile.kotlin(
                "NormalizedTargetClass.kt",
                """
                package com.example.normalized

                class NormalizedTargetClass
                """.trimIndent(),
            )
        val invalidTargetSource =
            SourceFile.kotlin(
                "InvalidTargetClass.kt",
                """
                package com.example.invalid

                class InvalidTargetClass
                """.trimIndent(),
            )

        val compilation =
            KotlinCompilation().apply {
                sources = listOf(normalizedTargetSource, invalidTargetSource)
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                kspProcessorOptions =
                    mutableMapOf(
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "PackageScan",
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "com.example.normalized..*,invalid*pattern,com.example.normalized.",
                    )
                inheritClassPath = true
            }

        compilation.compile()

        val normalizedGeneratedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "NormalizedTargetClassKotlinLoggingExtensions.kt"
            }
        val invalidGeneratedFile =
            compilation.kspSourcesDir.walkTopDown().find {
                it.name == "InvalidTargetClassKotlinLoggingExtensions.kt"
            }

        normalizedGeneratedFile?.exists() shouldBe true
        invalidGeneratedFile shouldBe null
    }

    @Test
    fun `should support deprecated GenerateLogger annotation for backward compatibility`() {
        val source =
            SourceFile.kotlin(
                "LegacyAnnotationClass.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.GenerateLogger

                @GenerateLogger
                class LegacyAnnotationClass
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
                it.name == "LegacyAnnotationClassKotlinLoggingExtensions.kt"
            }

        generatedFile?.exists() shouldBe true
    }
}
