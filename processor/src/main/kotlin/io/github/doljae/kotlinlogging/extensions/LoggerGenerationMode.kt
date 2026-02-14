package io.github.doljae.kotlinlogging.extensions

enum class LoggerGenerationMode {
    ANNOTATION_ONLY,
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
                "annotation", "annotationonly" -> ANNOTATION_ONLY
                "packagescan" -> PACKAGE_SCAN
                else -> null
            }
        }

        fun fromOption(optionValue: String?): LoggerGenerationMode {
            return fromOptionOrNull(optionValue) ?: ANNOTATION_ONLY
        }
    }
}
