package io.github.doljae.kotlinlogging.extensions

enum class LoggerGenerationMode {
    ANNOTATION_ONLY,
    PACKAGE_SCAN,
    ;

    companion object {
        fun fromOption(optionValue: String?): LoggerGenerationMode {
            val normalizedValue =
                optionValue
                    ?.trim()
                    ?.replace("_", "")
                    ?.replace("-", "")
                    ?.lowercase()

            return when (normalizedValue) {
                "packagescan" -> PACKAGE_SCAN
                else -> ANNOTATION_ONLY
            }
        }
    }
}
