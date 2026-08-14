package proto

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

/** Marks every declaration this plugin creates, so the IR phase can find them again. */
object LogKey : GeneratedDeclarationKey()

private val LOG_NAME = Name.identifier("log")
private val LOGGING_PACKAGE = FqName("io.github.oshai.kotlinlogging")
private val KLOGGER_ID = ClassId(LOGGING_PACKAGE, Name.identifier("KLogger"))
private val KOTLIN_LOGGING_ID = ClassId(LOGGING_PACKAGE, Name.identifier("KotlinLogging"))
private val LOGGER_FN = CallableId(KOTLIN_LOGGING_ID, Name.identifier("logger"))

private val LOG_ANNOTATION_ID =
    ClassId(FqName("io.github.doljae.kotlinlogging.extensions"), Name.identifier("Log"))

/** Mirrors the processor's `kotlinloggingextensions.mode`. */
enum class Mode { ALL, ANNOTATION_ONLY, PACKAGE_SCAN }

class LogConfig(val mode: Mode, val targets: List<String>)

/**
 * VARIANT: `variant-override` plus the processor's scoping modes.
 *
 * The point of interest is what partial selection does to a hierarchy. The generated `log` is a
 * *member*, so a subclass of a selected class inherits it — and, without an override of its own,
 * logs under its superclass's name. That is the same defect the extension-based processor has, and
 * it needs the same rule: a class that inherits a `log` gets one.
 */
class LogFirExtension(session: FirSession, private val config: LogConfig) :
    FirDeclarationGenerationExtension(session) {
    /** What a supertype's `log` looks like, which decides what this class may do. */
    private enum class SuperLog { NONE, OPEN, FINAL }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> = if (getsLog(classSymbol)) setOf(LOG_NAME) else emptySet()

    /**
     * Whether this plugin puts a `log` in this class.
     *
     * The last clause is the whole answer to scoped generation: `superLog(...) == OPEN` means the
     * class inherits a `log` it did not ask for, so it gets an override even when the mode does not
     * select it. Otherwise it would log under its superclass's name, silently.
     */
    private fun getsLog(classSymbol: FirClassSymbol<*>): Boolean {
        if (classSymbol.classKind == ClassKind.ANNOTATION_CLASS) return false
        if (declaresLog(classSymbol)) return false
        val inherited = superLog(classSymbol)
        // A final `log` in a supertype still cannot be overridden — see the FINAL case below.
        if (inherited == SuperLog.FINAL) return false
        return selectedByMode(classSymbol) || inherited == SuperLog.OPEN
    }

    /** Whether the configured mode picks this class on its own, ignoring what it inherits. */
    private fun selectedByMode(classSymbol: FirClassSymbol<*>): Boolean {
        if (config.mode == Mode.ALL) return true
        // SOURCE retention: present for classes being compiled, gone for anything from a dependency.
        if (classSymbol.hasAnnotation(LOG_ANNOTATION_ID, session)) return true
        if (config.mode != Mode.PACKAGE_SCAN) return false
        val packageName = classSymbol.classId.packageFqName.asString()
        return config.targets.any { target ->
            if (target.endsWith(".*")) {
                val prefix = target.removeSuffix(".*")
                prefix.isNotEmpty() && (packageName == prefix || packageName.startsWith("$prefix."))
            } else {
                packageName == target
            }
        }
    }

    @OptIn(DirectDeclarationsAccess::class)
    private fun FirClassSymbol<*>.ownLog(): FirPropertySymbol? =
        declarationSymbols.filterIsInstance<FirPropertySymbol>().firstOrNull { it.name == LOG_NAME }

    @OptIn(DirectDeclarationsAccess::class)
    private fun declaresLog(classSymbol: FirClassSymbol<*>): Boolean {
        if (classSymbol.ownLog() != null) return true
        val companion =
            classSymbol.declarationSymbols
                .filterIsInstance<FirRegularClassSymbol>()
                .firstOrNull { it.isCompanion }
        return companion?.ownLog() != null
    }

    /**
     * Classifies the `log` this class will inherit.
     *
     * A supertype that declares `log` by hand is whatever the user made it — usually FINAL, since
     * Kotlin properties are final by default. A supertype **from a dependency** compiled with this
     * plugin carries a real `open val log: KLogger` in its class file, so it lands in the same
     * branch: the member itself is the signal, and no annotation has to survive compilation for
     * this to work. A supertype in *this* compilation has no member yet, so the recursion into
     * [getsLog] asks whether the plugin is about to give it one.
     */
    private fun superLog(classSymbol: FirClassSymbol<*>): SuperLog {
        var result = SuperLog.NONE
        for (ref in classSymbol.resolvedSuperTypeRefs) {
            val superSymbol = ref.coneType.toRegularClassSymbol(session) ?: continue
            if (superSymbol.classId == StandardClassIds.Any) continue
            if (superSymbol.classKind == ClassKind.ANNOTATION_CLASS) continue
            val declared = superSymbol.ownLog()
            val state =
                when {
                    // A supertype `log` of some other type cannot be overridden with a KLogger.
                    // Without this the compiler accepts the invalid override and the user's own
                    // `log` silently changes meaning at runtime.
                    declared != null &&
                        declared.resolvedReturnType.classId != KLOGGER_ID -> SuperLog.FINAL
                    declared != null ->
                        if (declared.modality == Modality.FINAL) SuperLog.FINAL else SuperLog.OPEN
                    getsLog(superSymbol) -> SuperLog.OPEN
                    else -> SuperLog.NONE
                }
            // FINAL anywhere wins: it blocks generation regardless of the other supertypes.
            if (state == SuperLog.FINAL) return SuperLog.FINAL
            if (state == SuperLog.OPEN) result = SuperLog.OPEN
        }
        return result
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        if (callableId.callableName != LOG_NAME) return emptyList()
        val owner = context?.owner ?: return emptyList()
        val overriding = superLog(owner) == SuperLog.OPEN

        val property =
            createMemberProperty(
                owner = owner,
                key = LogKey,
                name = LOG_NAME,
                returnType = KLOGGER_ID.constructClassLikeType(),
                isVal = true,
                hasBackingField = false,
            ) {
                // Open so subclasses can override and keep their own logger name.
                modality = Modality.OPEN
                status { isOverride = overriding }
            }
        return listOf(property.symbol)
    }
}

/**
 * ATTEMPT: rewrite a user's own `log` in a subclass into an `override`, so that hand-written
 * loggers survive the open/override scheme instead of failing with "hides member of supertype".
 */
class LogStatusTransformer(session: FirSession) : FirStatusTransformerExtension(session) {
    override fun needTransformStatus(declaration: FirDeclaration): Boolean =
        declaration is FirProperty && declaration.name == LOG_NAME

    /** The property's own status is not enough — the getter keeps `private` and fails separately. */
    override fun transformStatus(
        status: FirDeclarationStatus,
        propertyAccessor: FirPropertyAccessor,
        containingClass: FirClassLikeSymbol<*>?,
        containingProperty: FirProperty?,
        isLocal: Boolean,
    ): FirDeclarationStatus {
        if (containingProperty?.name != LOG_NAME) return status
        val owner = containingClass as? FirClassSymbol<*> ?: return status
        if (!hasSuperCandidate(owner)) return status
        return FirDeclarationStatusImpl(Visibilities.Public, status.modality ?: Modality.FINAL)
            .apply { isOverride = true }
    }

    private fun hasSuperCandidate(owner: FirClassSymbol<*>): Boolean =
        owner.resolvedSuperTypeRefs.any { ref ->
            val s = ref.coneType.toRegularClassSymbol(session) ?: return@any false
            s.classId != StandardClassIds.Any &&
                (s.classKind == ClassKind.CLASS || s.classKind == ClassKind.INTERFACE)
        }

    override fun transformStatus(
        status: FirDeclarationStatus,
        property: FirProperty,
        containingClass: FirClassLikeSymbol<*>?,
        isLocal: Boolean,
    ): FirDeclarationStatus {
        val owner = containingClass as? FirClassSymbol<*> ?: return status
        if (!hasSuperCandidate(owner)) return status
        // Force public: an override may not weaken visibility, and the generated super `log`
        // is public. This silently widens the user's `private val log`.
        return FirDeclarationStatusImpl(Visibilities.Public, status.modality ?: Modality.FINAL)
            .apply { isOverride = true }
    }
}

class LogFirRegistrar(private val config: LogConfig) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +{ session: FirSession -> LogFirExtension(session, config) }
        +::LogStatusTransformer
    }
}

/** Backend half: fills in the getter body FIR left empty with `KotlinLogging.logger("<fqName>")`. */
class LogIrExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val loggerFn =
            pluginContext.referenceFunctions(LOGGER_FN).singleOrNull { function ->
                val regular = function.owner.parameters.filter { it.kind.name == "Regular" }
                regular.size == 1 && regular[0].type == pluginContext.irBuiltIns.stringType
            } ?: error("KotlinLogging.logger(String) not found on the compile classpath")
        val kotlinLogging =
            pluginContext.referenceClass(KOTLIN_LOGGING_ID)
                ?: error("KotlinLogging not found on the compile classpath")

        moduleFragment.acceptChildrenVoid(
            object : IrVisitorVoid() {
                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                    val property = declaration.correspondingPropertySymbol?.owner
                    val origin = property?.origin
                    if (origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey == LogKey) {
                        val fqName = declaration.parentAsClass.kotlinFqName.asString()
                        property.backingField = null
                        declaration.origin = origin
                        declaration.body =
                            DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
                                +irReturn(
                                    irCall(loggerFn).apply {
                                        arguments[0] = irGetObject(kotlinLogging)
                                        arguments[1] = irString(fqName)
                                    },
                                )
                            }
                    }
                    super.visitSimpleFunction(declaration)
                }
            },
        )
    }
}

private val MODE_KEY = CompilerConfigurationKey.create<String>("log mode")
private val TARGETS_KEY = CompilerConfigurationKey.create<String>("log targets")

/** `-P plugin:proto.log:mode=AnnotationOnly` — the plugin's answer to `ksp { arg(...) }`. */
@OptIn(ExperimentalCompilerApi::class)
class LogCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "proto.log"

    override val pluginOptions: Collection<CliOption> =
        listOf(
            CliOption("mode", "<mode>", "All | AnnotationOnly | PackageScan", required = false),
            CliOption("targets", "<packages>", "comma-separated package targets", required = false),
        )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            "mode" -> configuration.put(MODE_KEY, value)
            "targets" -> configuration.put(TARGETS_KEY, value)
            else -> error("Unknown option ${option.optionName}")
        }
    }
}

@OptIn(ExperimentalCompilerApi::class)
class LogPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "proto.log"
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val targets =
            configuration.get(TARGETS_KEY).orEmpty()
                .split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val mode =
            when (configuration.get(MODE_KEY)?.lowercase()?.replace("_", "")?.replace("-", "")) {
                "annotationonly" -> Mode.ANNOTATION_ONLY
                "packagescan" -> Mode.PACKAGE_SCAN
                else -> Mode.ALL
            }
        FirExtensionRegistrarAdapter.registerExtension(LogFirRegistrar(LogConfig(mode, targets)))
        IrGenerationExtension.registerExtension(LogIrExtension())
    }
}
