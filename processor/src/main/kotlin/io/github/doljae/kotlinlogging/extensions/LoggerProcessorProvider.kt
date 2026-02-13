package io.github.doljae.kotlinlogging.extensions

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class LoggerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        LoggerProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            generationMode = resolveGenerationMode(environment.options),
            packageScanTargetPatterns = resolvePackageScanTargetPatterns(environment.options),
        )

    private fun resolveGenerationMode(options: Map<String, String>): LoggerGenerationMode {
        val configuredMode = options[LoggerProcessor.GENERATION_MODE_OPTION_KEY]
        val hasConfiguredPackageTargets = options[LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY]?.isNotBlank() == true
        val hasLegacyPackagePrefixes = options[LoggerProcessor.LEGACY_PACKAGE_PREFIXES_OPTION_KEY]?.isNotBlank() == true

        return when {
            hasConfiguredPackageTargets || hasLegacyPackagePrefixes -> LoggerGenerationMode.PACKAGE_SCAN
            configuredMode != null -> LoggerGenerationMode.fromOption(configuredMode)
            else -> LoggerGenerationMode.ANNOTATION_ONLY
        }
    }

    private fun resolvePackageScanTargetPatterns(options: Map<String, String>): Set<String> {
        val packageTargets =
            options[LoggerProcessor.PACKAGE_SCAN_TARGETS_OPTION_KEY]
                .toOptionValues()

        // Backward compatibility for previously introduced option.
        val legacyPackageTargets =
            options[LoggerProcessor.LEGACY_PACKAGE_PREFIXES_OPTION_KEY]
                .toOptionValues()
                .map { "$it.*" }

        return (packageTargets + legacyPackageTargets).toSet()
    }

    private fun String?.toOptionValues(): List<String> {
        return this
            ?.split(',')
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
