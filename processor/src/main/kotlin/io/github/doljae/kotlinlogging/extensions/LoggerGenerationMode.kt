package io.github.doljae.kotlinlogging.extensions

enum class LoggerGenerationMode {
    /** Every class gets a `log` extension. The default when nothing is configured. */
    ALL,

    /** Only classes annotated with `@AutoLog`. */
    ANNOTATION_ONLY,

    /** Only classes in the configured package targets, plus any annotated with `@AutoLog`. */
    PACKAGE_SCAN,
    ;

    companion object {
        fun fromOptionOrNull(optionValue: String?): LoggerGenerationMode? {
            val normalizedValue =
                optionValue
                    ?.trim()
                    ?.replace("_", "")
                    ?.replace("-", "")
                    ?.lowercase()

            return when (normalizedValue) {
                "all", "projectwide" -> ALL
                "annotation", "annotationonly" -> ANNOTATION_ONLY
                "packagescan" -> PACKAGE_SCAN
                else -> null
            }
        }
    }
}
