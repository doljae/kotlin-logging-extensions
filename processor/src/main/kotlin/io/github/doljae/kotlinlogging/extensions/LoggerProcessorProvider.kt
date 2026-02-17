package io.github.doljae.kotlinlogging.extensions

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class LoggerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val options = environment.options
        val packageScanTargetPatterns = resolvePackageScanTargetPatterns(options, environment.logger)
        val generationMode = resolveGenerationMode(options, packageScanTargetPatterns, environment.logger)

        if (generationMode == LoggerGenerationMode.PACKAGE_SCAN && packageScanTargetPatterns.isEmpty()) {
            environment.logger.warn(
                "Package scan mode is enabled but no valid targets were configured. " +
                    "Only classes annotated with @AutoLog (or deprecated @GenerateLogger) will get log extensions.",
            )
        }

        return LoggerProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            generationMode = generationMode,
            packageScanTargetPatterns = packageScanTargetPatterns,
        )
    }

    private fun resolveGenerationMode(
        options: Map<String, String>,
        packageScanTargetPatterns: Set<String>,
        logger: KSPLogger,
    ): LoggerGenerationMode {
        val configuredMode = options[LoggerProcessor.GENERATION_MODE_OPTION_KEY]
        val parsedConfiguredMode = LoggerGenerationMode.fromOptionOrNull(configuredMode)
        val hasConfiguredPackageTargets = packageScanTargetPatterns.isNotEmpty()

        if (configuredMode != null && parsedConfiguredMode == null) {
            logger.warn(
                "Unsupported value '$configuredMode' for ${LoggerProcessor.GENERATION_MODE_OPTION_KEY}. " +
                    "Supported values: AnnotationOnly, PackageScan. Falling back to AnnotationOnly.",
            )
        }

        return when {
            hasConfiguredPackageTargets -> {
                if (parsedConfiguredMode == LoggerGenerationMode.ANNOTATION_ONLY) {
                    logger.warn(
                        "${LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY} (or legacy prefixes) was configured while " +
                            "${LoggerProcessor.GENERATION_MODE_OPTION_KEY} resolves to AnnotationOnly. " +
                            "Package scan targets take precedence and PackageScan mode will be used.",
                    )
                }
                LoggerGenerationMode.PACKAGE_SCAN
            }
            parsedConfiguredMode != null -> parsedConfiguredMode
            else -> LoggerGenerationMode.ANNOTATION_ONLY
        }
    }

    private fun resolvePackageScanTargetPatterns(options: Map<String, String>, logger: KSPLogger): Set<String> {
        val packageTargets =
            options[LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY]
                .toOptionValues()
                .mapNotNull { rawTarget ->
                    normalizePackageTargetPattern(
                        rawTarget = rawTarget,
                        optionKey = LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY,
                        logger = logger,
                    )
                }

        // Backward compatibility for previously introduced option.
        val legacyPackageTargets =
            options[LoggerProcessor.LEGACY_PACKAGE_PREFIXES_OPTION_KEY]
                .toOptionValues()
                .map { legacyPrefix -> legacyPrefix.trimEnd('.') }
                .mapNotNull { legacyPrefix ->
                    normalizePackageTargetPattern(
                        rawTarget = "$legacyPrefix.*",
                        optionKey = LoggerProcessor.LEGACY_PACKAGE_PREFIXES_OPTION_KEY,
                        logger = logger,
                    )
                }

        return (packageTargets + legacyPackageTargets).toSet()
    }

    private fun normalizePackageTargetPattern(rawTarget: String, optionKey: String, logger: KSPLogger): String? {
        val trimmedTarget = rawTarget.trim()

        if (trimmedTarget.isEmpty()) {
            return null
        }

        val normalizedTarget =
            if (trimmedTarget.endsWith(".*")) {
                "${trimmedTarget.removeSuffix(".*").trimEnd('.')}.*"
            } else {
                trimmedTarget.trimEnd('.')
            }

        val packageNamePart = normalizedTarget.removeSuffix(".*")
        val isValidPackageName = packageNamePart.matches(PACKAGE_NAME_PATTERN)

        if (!isValidPackageName) {
            logger.warn(
                "Ignoring invalid package target '$rawTarget' from option '$optionKey'. " +
                    "Use 'com.example' or 'com.example.*' format.",
            )
            return null
        }

        return normalizedTarget
    }

    private fun String?.toOptionValues(): List<String> {
        return this
            ?.split(',')
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    companion object {
        private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")
    }
}
