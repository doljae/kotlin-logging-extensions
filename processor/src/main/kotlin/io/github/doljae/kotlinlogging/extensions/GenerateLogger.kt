package io.github.doljae.kotlinlogging.extensions

@Deprecated(
    message = "Use @AutoLog instead.",
    replaceWith = ReplaceWith("AutoLog"),
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class GenerateLogger
