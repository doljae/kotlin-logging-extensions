package io.github.doljae.kotlinlogging.extensions

/**
 * Former name of [Log], kept so code written against v2.3.1–v2.4.0 keeps compiling.
 *
 * This is a real annotation class rather than a `typealias` on purpose: KSP does not expand type
 * aliases when resolving an annotation, so `annotationType.resolve().declaration` would report the
 * alias's own qualified name and the processor's match would silently miss. An aliased deprecation
 * shim would therefore compile fine and quietly generate nothing.
 *
 * The processor matches this alongside [Log]. Removal is a later breaking release.
 */
@Deprecated(
    message = "Renamed to @Log.",
    replaceWith = ReplaceWith("Log"),
    level = DeprecationLevel.WARNING,
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class AutoLog
