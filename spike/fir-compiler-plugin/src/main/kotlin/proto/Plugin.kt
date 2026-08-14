package proto

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/** Marks every declaration this plugin creates, so the IR phase can find them again. */
object LogKey : GeneratedDeclarationKey()

private val LOG_NAME = Name.identifier("log")
private val LOGGING_PACKAGE = FqName("io.github.oshai.kotlinlogging")
private val KLOGGER_ID = ClassId(LOGGING_PACKAGE, Name.identifier("KLogger"))
private val KOTLIN_LOGGING_ID = ClassId(LOGGING_PACKAGE, Name.identifier("KotlinLogging"))
private val LOGGER_FN = CallableId(KOTLIN_LOGGING_ID, Name.identifier("logger"))

/**
 * Frontend half: declares `log: KLogger` on every class so resolution — and the IDE — can see it.
 * FIR only describes the signature; the body is the backend's job.
 */
class LogFirExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {
    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        // An annotation class cannot hold members at all.
        if (classSymbol.classKind == ClassKind.ANNOTATION_CLASS) return emptySet()

        // The KSP equivalent is hasDeclaredLogProperty(): a user-written `log` makes the processor
        // step aside. Injecting a member instead would be a redeclaration conflict, so check first.
        if (declaresLog(classSymbol)) return emptySet()

        // A member is inherited, so generating one in a subclass overrides the (final) parent's.
        // Only the root of a hierarchy gets `log`; see the findings note on what this costs.
        if (hasSuperClassWithLog(classSymbol)) return emptySet()

        return setOf(LOG_NAME)
    }

    /**
     * Only source declarations, never the member scope. The scope includes plugin-generated members,
     * so querying it from inside [getCallableNamesForClass] — which is what builds that scope —
     * recurses until the stack dies. That is what [DirectDeclarationsAccess] exists for.
     */
    @OptIn(DirectDeclarationsAccess::class)
    private fun declaresLog(classSymbol: FirClassSymbol<*>): Boolean {
        fun FirClassSymbol<*>.hasOwnLog(): Boolean =
            declarationSymbols.any { it is FirPropertySymbol && it.name == LOG_NAME }

        if (classSymbol.hasOwnLog()) return true
        val companion =
            classSymbol.declarationSymbols
                .filterIsInstance<FirRegularClassSymbol>()
                .firstOrNull { it.isCompanion }
        return companion?.hasOwnLog() == true
    }

    /**
     * True when the superclass will itself receive a `log`, which is what makes generating one here
     * an override of a final member.
     *
     * A source superclass is assumed to get one — it is either generated for it or declared by hand,
     * and both mean the subclass must stay out. `kotlin.Enum` and dependency classes are not source,
     * so they must be inspected instead: a dependency compiled with this same plugin carries a real
     * `log` in its class file, and generating one here would override that final member.
     */
    private fun hasSuperClassWithLog(classSymbol: FirClassSymbol<*>): Boolean =
        classSymbol.resolvedSuperTypeRefs.any { ref ->
            val superSymbol = ref.coneType.toRegularClassSymbol(session) ?: return@any false
            if (superSymbol.classKind != ClassKind.CLASS) return@any false
            if (superSymbol.classId == StandardClassIds.Any) return@any false
            superSymbol.origin == FirDeclarationOrigin.Source || declaresLog(superSymbol)
        }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        if (callableId.callableName != LOG_NAME) return emptyList()
        val owner = context?.owner ?: return emptyList()

        val property =
            createMemberProperty(
                owner = owner,
                key = LogKey,
                name = LOG_NAME,
                returnType = KLOGGER_ID.constructClassLikeType(),
                isVal = true,
                hasBackingField = false,
            )
        return listOf(property.symbol)
    }
}

class LogFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::LogFirExtension
    }
}

/** Backend half: fills in the getter body FIR left empty with `KotlinLogging.logger("<fqName>")`. */
class LogIrExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // `logger` is overloaded — logger(String) and logger(() -> Unit) — so pick by parameter type.
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
                    // The plugin key lands on the property, not on its accessor — the getter comes
                    // through as DEFAULT_PROPERTY_ACCESSOR.
                    val property = declaration.correspondingPropertySymbol?.owner
                    val origin = property?.origin
                    if (origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey == LogKey) {
                        val fqName = declaration.parentAsClass.kotlinFqName.asString()
                        // FIR said hasBackingField = false, but Fir2Ir still adds one because the
                        // accessor arrives body-less. In an interface that emits a private final
                        // instance field, which the JVM rejects at class load (ClassFormatError).
                        property.backingField = null
                        // Leaving the accessor as DEFAULT_PROPERTY_ACCESSOR lets a later lowering
                        // regenerate the default body and discard the one installed here.
                        declaration.origin = origin
                        declaration.body =
                            DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
                                +irReturn(
                                    irCall(loggerFn).apply {
                                        // KotlinLogging is an object, so slot 0 is its dispatch receiver.
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

@OptIn(ExperimentalCompilerApi::class)
class LogPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "proto.log"
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(LogFirRegistrar())
        IrGenerationExtension.registerExtension(LogIrExtension())
    }
}
