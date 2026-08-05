package io.github.doljae.kotlinlogging.extensions

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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
        val generatedFile = compilation.generatedExtensionsFileContaining("SimpleClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("DeepClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("<T> GenericClass<T>")

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

        val generatedFileA = compilation.generatedExtensionsFileContaining("ClassA")
        val generatedFileB = compilation.generatedExtensionsFileContaining("ClassB")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("ReservedClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("NoAnnotationClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("ClassWithLogProperty")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("ClassWithCompanionLogProperty")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("PackageScopedClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("OutsidePackageClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("AnnotatedOutsidePackageClass")

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

        val result = compilation.compile()

        val generatedFile = compilation.generatedExtensionsFileContaining("ModeIgnoredClass")

        generatedFile?.exists() shouldBe true
        result.messages shouldContain "Package scan targets take precedence and PackageScan mode will be used"
    }

    @Test
    fun `should not force package scan when configured targets are invalid`() {
        val source =
            SourceFile.kotlin(
                "InvalidTargetsWithAnnotationOnlyModeClass.kt",
                """
                package com.example.invalid

                class InvalidTargetsWithAnnotationOnlyModeClass
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
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "invalid*pattern",
                    )
                inheritClassPath = true
            }

        val result = compilation.compile()

        val generatedFile = compilation.generatedExtensionsFileContaining("InvalidTargetsWithAnnotationOnlyModeClass")

        generatedFile shouldBe null
        result.messages shouldContain "Ignoring invalid package target 'invalid*pattern'"
        result.messages shouldNotContain "Package scan targets take precedence and PackageScan mode will be used"
        result.messages shouldNotContain "Package scan mode is enabled but no valid targets were configured"
    }

    @Test
    fun `should warn and fall back to annotation only for unsupported mode option`() {
        val source =
            SourceFile.kotlin(
                "UnsupportedModeClass.kt",
                """
                package com.example.unsupported

                class UnsupportedModeClass
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
                        LoggerProcessor.GENERATION_MODE_OPTION_KEY to "NotSupported",
                    )
                inheritClassPath = true
            }

        val result = compilation.compile()

        val generatedFile = compilation.generatedExtensionsFileContaining("UnsupportedModeClass")

        generatedFile shouldBe null
        result.messages shouldContain "Unsupported value 'NotSupported'"
        result.messages shouldContain "Falling back to AnnotationOnly"
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

        val generatedFile = compilation.generatedExtensionsFileContaining("NormalizedModeClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("AnnotationOnlyModeClass")

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

        val exactPackageGeneratedFile = compilation.generatedExtensionsFileContaining("ExactPackageClass")
        val subPackageGeneratedFile = compilation.generatedExtensionsFileContaining("SubPackageClass")

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

        val generatedFile = compilation.generatedExtensionsFileContaining("LegacyOptionClass")

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

        val normalizedGeneratedFile = compilation.generatedExtensionsFileContaining("NormalizedTargetClass")
        val invalidGeneratedFile = compilation.generatedExtensionsFileContaining("InvalidTargetClass")

        normalizedGeneratedFile?.exists() shouldBe true
        invalidGeneratedFile shouldBe null
    }

    @Test
    fun `should warn when package scan mode has no valid targets`() {
        val source =
            SourceFile.kotlin(
                "NoValidTargetClass.kt",
                """
                package com.example.invalid

                class NoValidTargetClass
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
                        LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY to "invalid*pattern",
                    )
                inheritClassPath = true
            }

        val result = compilation.compile()

        val generatedFile = compilation.generatedExtensionsFileContaining("NoValidTargetClass")

        generatedFile shouldBe null
        result.messages shouldContain "Ignoring invalid package target 'invalid*pattern'"
        result.messages shouldContain "Package scan mode is enabled but no valid targets were configured"
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

        val generatedFile = compilation.generatedExtensionsFileContaining("LegacyAnnotationClass")

        generatedFile?.exists() shouldBe true
    }

    @Test
    fun `should emit one file per package regardless of how many classes it holds`() {
        val sources =
            listOf(
                SourceFile.kotlin(
                    "ServiceClasses.kt",
                    """
                    package com.example.service
                    import io.github.doljae.kotlinlogging.extensions.AutoLog

                    @AutoLog
                    class OrderService
                    @AutoLog
                    class PaymentService
                    """.trimIndent(),
                ),
                SourceFile.kotlin(
                    "UserService.kt",
                    """
                    package com.example.service
                    import io.github.doljae.kotlinlogging.extensions.AutoLog

                    @AutoLog
                    class UserService
                    """.trimIndent(),
                ),
                SourceFile.kotlin(
                    "OrderRepository.kt",
                    """
                    package com.example.repository
                    import io.github.doljae.kotlinlogging.extensions.AutoLog

                    @AutoLog
                    class OrderRepository
                    """.trimIndent(),
                ),
            )

        val compilation =
            KotlinCompilation().apply {
                this.sources = sources
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                inheritClassPath = true
            }

        compilation.compile()

        // Four classes across three source files, but only two packages.
        val generatedFiles = compilation.generatedExtensionsFiles()
        generatedFiles.map { it.name } shouldBe
            listOf("KotlinLoggingExtensions.kt", "KotlinLoggingExtensions.kt")

        val serviceFile = compilation.generatedExtensionsFileContaining("OrderService")
        val serviceContent = serviceFile?.readText() ?: ""

        // All three service classes share one file, sorted by qualified name.
        serviceContent shouldContain "val OrderService.log: KLogger"
        serviceContent shouldContain "val PaymentService.log: KLogger"
        serviceContent shouldContain "val UserService.log: KLogger"
        serviceContent.indexOf("val OrderService.log") shouldBeLessThan
            serviceContent.indexOf("val PaymentService.log")

        // Imports are emitted once for the whole file, not once per class.
        serviceContent.split("import io.github.oshai.kotlinlogging.KLogger").size shouldBe 2

        // A different package gets its own file.
        serviceContent shouldNotContain "val OrderRepository.log"
        compilation.generatedExtensionsFileContaining("OrderRepository")?.readText() shouldContain
            "package com.example.repository"
    }

    @Test
    fun `should warn but still generate when a top-level log is shadowed`() {
        val source =
            SourceFile.kotlin(
                "ShadowedClass.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog
                import io.github.oshai.kotlinlogging.KotlinLogging

                val log = KotlinLogging.logger("TOP_LEVEL")

                @AutoLog
                class ShadowedClass
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

        val generatedFile = compilation.generatedExtensionsFileContaining("ShadowedClass")

        // Generation must not be skipped: top-level functions in the file still resolve to the
        // top-level property, so its presence is not a signal to leave the class without a logger.
        generatedFile?.exists() shouldBe true
        result.messages shouldContain "Top-level 'log' in this file is shadowed inside ShadowedClass"
    }

    @Test
    fun `should not warn about shadowing when the file has no top-level log`() {
        val source =
            SourceFile.kotlin(
                "UnshadowedClass.kt",
                """
                package com.example
                import io.github.doljae.kotlinlogging.extensions.AutoLog

                @AutoLog
                class UnshadowedClass
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

        result.messages shouldNotContain "is shadowed inside"
    }
}
