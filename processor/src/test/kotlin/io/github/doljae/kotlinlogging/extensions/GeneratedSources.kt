package io.github.doljae.kotlinlogging.extensions

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

/**
 * Finds the generated file holding the `log` extension for [receiverType], or `null` when no
 * extension was generated for it.
 *
 * Extensions are grouped one file per package, so a file's name no longer identifies the class it
 * belongs to. Tests therefore locate it by the declaration it must contain, which asserts the same
 * thing the old per-class file name did: this class either got an extension or it did not.
 *
 * [receiverType] is the receiver as it appears in the generated source — `SimpleClass`,
 * `Outer.Nested`, `GenericClass<T>`.
 */
@OptIn(ExperimentalCompilerApi::class)
internal fun KotlinCompilation.generatedExtensionsFileContaining(receiverType: String): File? {
    return generatedExtensionsFiles()
        .find { file -> file.readText().contains("val $receiverType.log: KLogger") }
}

/** Every file the processor generated, in stable path order. */
@OptIn(ExperimentalCompilerApi::class)
internal fun KotlinCompilation.generatedExtensionsFiles(): List<File> {
    return kspSourcesDir
        .walkTopDown()
        .filter { file -> file.isFile && file.extension == "kt" }
        .sortedBy { file -> file.path }
        .toList()
}
