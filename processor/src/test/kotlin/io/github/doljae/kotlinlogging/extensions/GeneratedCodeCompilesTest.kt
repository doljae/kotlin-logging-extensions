package io.github.doljae.kotlinlogging.extensions

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test

/**
 * Asserts that the generated file **compiles**, not merely that it contains the expected text.
 *
 * The rest of the suite reads the generated source as a string, which cannot catch a receiver the
 * generated file is not allowed to name. That gap only became dangerous once project-wide generation
 * became the default: every class in a project is now a receiver, including ones that used to need an
 * explicit `@Log` before the processor would touch them.
 */
@OptIn(ExperimentalCompilerApi::class)
class GeneratedCodeCompilesTest {
    private fun compile(vararg sources: SourceFile): Pair<KotlinCompilation, String> {
        val compilation =
            KotlinCompilation().apply {
                this.sources = sources.toList()
                configureKsp {
                    symbolProcessorProviders += LoggerProcessorProvider()
                }
                inheritClassPath = true
            }

        val result = compilation.compile()

        // The message is attached so a failure names the offending line instead of just "not OK".
        withClue(result.messages) {
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        }

        return compilation to (compilation.generatedExtensionsFiles().singleOrNull()?.readText() ?: "")
    }

    @Test
    fun `should generate compilable extensions for every class kind`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "ClassKinds.kt",
                    """
                    package com.example.kinds

                    class PlainClass
                    data class DataClass(val id: String)
                    internal class InternalClass
                    interface AnInterface
                    object AnObject
                    enum class AnEnum { FIRST, SECOND }
                    annotation class AnAnnotation
                    @JvmInline value class AValue(val raw: String)
                    abstract class AnAbstractClass
                    sealed class ASealedClass {
                        class Child : ASealedClass()
                    }
                    class Outer {
                        class Nested
                        inner class Inner
                    }
                    """.trimIndent(),
                ),
            )

        // Every kind above is a legal extension receiver, so all of them are expected — the point of
        // the test is that the file they share still compiles.
        listOf(
            "PlainClass",
            "DataClass",
            "InternalClass",
            "AnInterface",
            "AnObject",
            "AnEnum",
            "AnAnnotation",
            "AValue",
            "AnAbstractClass",
            "ASealedClass",
            "ASealedClass.Child",
            "Outer",
            "Outer.Nested",
            "Outer.Inner",
        ).forEach { receiver ->
            generated shouldContain "val $receiver.log: KLogger"
        }

        // Enum entries are not types, so they must not become receivers.
        generated shouldNotContain "AnEnum.FIRST"
    }

    @Test
    fun `should star-project type parameters instead of restating their bounds`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "Generics.kt",
                    """
                    package com.example.generics

                    class Bounded<T : Any>
                    class Recursive<T : Comparable<T>>
                    class MultiBound<T> where T : CharSequence, T : Comparable<T>
                    class Variant<out T : Any>
                    class GenericOuter<A : Any> {
                        class Nested<B : Any>
                        inner class Inner<C : Any>
                        inner class PlainInner
                    }

                    fun use(bounded: Bounded<String>) {
                        bounded.log.info { "reachable from an instantiation" }
                    }
                    """.trimIndent(),
                ),
            )

        // A bound the extension does not repeat is a compile error, so none are declared at all.
        generated shouldContain "val Bounded<*>.log: KLogger"
        generated shouldContain "val Recursive<*>.log: KLogger"
        generated shouldContain "val MultiBound<*>.log: KLogger"
        generated shouldContain "val Variant<*>.log: KLogger"

        // A plain nested class is reached through the bare outer name; spelling the outer's arguments
        // there is rejected as redundant. An inner class needs them.
        generated shouldContain "val GenericOuter.Nested<*>.log: KLogger"
        generated shouldContain "val GenericOuter<*>.Inner<*>.log: KLogger"
        generated shouldContain "val GenericOuter<*>.PlainInner.log: KLogger"
    }

    @Test
    fun `should skip a private top-level class so the package file still compiles`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "PrivateTopLevel.kt",
                    """
                    package com.example.priv

                    private class HiddenService
                    class VisibleService
                    """.trimIndent(),
                ),
            )

        // A private top-level class is visible only inside its own file, and the extension is written
        // to a different file in the same package — naming it there does not compile.
        generated shouldNotContain "HiddenService"
        generated shouldContain "val VisibleService.log: KLogger"
    }

    @Test
    fun `should skip a private nested class but keep its non-private sibling`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "PrivateNested.kt",
                    """
                    package com.example.nested

                    class Holder {
                        private class HiddenNested
                        internal class VisibleNested
                    }
                    """.trimIndent(),
                ),
            )

        generated shouldNotContain "HiddenNested"
        generated shouldContain "internal val Holder.VisibleNested.log: KLogger"
    }

    @Test
    fun `should skip a class nested inside a private class`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "PrivateOuter.kt",
                    """
                    package com.example.privouter

                    private class HiddenHolder {
                        class PublicButUnreachable
                    }
                    class Reachable
                    """.trimIndent(),
                ),
            )

        // The nested class is public, but it cannot be named without naming its private outer class.
        generated shouldNotContain "PublicButUnreachable"
        generated shouldContain "val Reachable.log: KLogger"
    }

    @Test
    fun `should generate a compilable file for a package named with a reserved keyword`() {
        val (_, generated) =
            compile(
                SourceFile.kotlin(
                    "ReservedPackage.kt",
                    """
                    package com.example.`is`.`object`

                    class ReservedPackageClass
                    """.trimIndent(),
                ),
            )

        generated shouldContain "package com.example.`is`.`object`"
        generated shouldContain "val ReservedPackageClass.log: KLogger"
    }
}
