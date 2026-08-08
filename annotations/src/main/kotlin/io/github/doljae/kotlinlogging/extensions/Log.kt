package io.github.doljae.kotlinlogging.extensions

/**
 * Marks a class to receive a generated `log` extension property.
 *
 * Only consulted in the `ANNOTATED` and `PACKAGE_SCAN` generation modes — under the default `ALL`
 * mode every eligible class gets one regardless.
 *
 * Retention is `SOURCE` because the processor reads it from the syntax tree; it never needs to exist
 * at runtime, and keeping it out of the bytecode means it adds nothing to a consumer's artifacts.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Log
