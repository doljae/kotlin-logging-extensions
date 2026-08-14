# Spike: K2 compiler plugin instead of KSP

Throwaway prototype written to answer #172 with measurements instead of guesses.
**Not part of the build, not published, not maintained.** The root `settings.gradle.kts`
does not include it, and nothing in `processor/` depends on it.

## The question it answers

`getVisibilityModifier()` (`processor/src/main/kotlin/.../LoggerProcessor.kt:253`) returns
`null` for `private`, `protected` and local classes, so no `log` is generated for them.
The cause is structural: KSP can only *create* files, never modify them, and a generated
file in a different file cannot name a `private` top-level class.

A compiler plugin has no such limit — it edits the class itself. So: does that remove the
limitation while keeping everything else working?

**Short answer: it removes the limitation, and the cost is that a subclass can no longer
declare its own `log` by hand — which is the exact workaround the README recommends today.**
See #172 for the full write-up.

There are three plugin variants here, each superseding the last: `src/main/kotlin/` →
`variant-override/` → `variant-modes/`. Read them in that order.

## What it does

`src/main/kotlin/proto/Plugin.kt` — about 100 lines, two halves:

- **FIR** (`LogFirExtension`) declares `val log: KLogger` as a **member** of every class,
  so resolution and the IDE can see it.
- **IR** (`LogIrExtension`) fills in the getter body with
  `KotlinLogging.logger("<fqName>")`.

The whole difference from KSP is that word **member**. KSP generates an *extension*
property. Injecting a member is what reaches `private`/`protected`/local classes — and
also what causes both unsolved problems, because members are inherited and members
beat extensions in overload resolution. The three cannot be separated.

## Building and running

There is no wrapper here; everything was driven with `kotlinc` directly.
Kotlin 2.4.10, kotlin-logging 7.0.13, logback on the runtime classpath.

```bash
# 1. build the plugin
JAR=$(ls ~/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-compiler-embeddable/2.4.10/*/kotlin-compiler-embeddable-2.4.10.jar)
kotlinc -nowarn -Xcontext-parameters -cp "$JAR" src/main/kotlin/proto/Plugin.kt -d pbuild
cp -r src/main/resources/META-INF pbuild/
(cd pbuild && jar cf ../plugin.jar .)

# 2. compile a test file with the plugin, then run it
#    CP = kotlin-logging-jvm + slf4j-api + logback-classic + logback-core
kotlinc -nowarn -cp "$CP" -Xplugin=plugin.jar testdata/01-visibility.kt -d out
java -cp "out:$CP:$KOTLIN_LIB/kotlin-stdlib.jar" app.ConsumerKt
```

## What each test file demonstrates

| File | Shows |
| --- | --- |
| `01-visibility.kt` | ✅ the win — `private`, `protected` nested and local classes all get a working logger |
| `02-hard-cases.kt` | ✅ interface, enum, object, data class, user-declared `log`, companion `log` — each of these crashed at first, all fixed |
| `03-inheritance.kt` | ❌ `Sub` logs as `inh.Base`; KSP gives `inh.Sub`. **Fixed by `variant-override/`** |
| `04-user-extension.kt` | ❌ a user-written `Service.log` extension is silently overridden, no warning |
| `05-top-level-log.kt` | ⚠️ a top-level `log` is unconditionally shadowed inside classes, which changes what `warnIfTopLevelLogIsShadowed()` would have to say |
| `cross-module/` | ❌→✅ `IncompatibleClassChangeError` when a subclass in another module inherits a plugin-generated `log`; fixed, but the logger is still named after the superclass |
| `06-subclass-own-log/` | the six shapes of "a subclass writes its own `log`", used to compare the two variants below |

Run `03` and `04` with and without `-Xplugin=` to see the difference.

## `variant-override/` — keeping per-class logger names

`src/main/kotlin/proto/Plugin.kt` generates `log` only in the root of a hierarchy, because a
Kotlin property is `final` and generating one in a subclass would be a final-method override
(`IncompatibleClassChangeError`). That is why `03-inheritance.kt` logs `inh.Base` from `Sub`.

`variant-override/Plugin.kt` generates one in *every* class instead, `open` in the root and
`override` in each subclass — something only a compiler plugin can do, since it controls the
generated declaration's status directly:

```kotlin
createMemberProperty(owner, LogKey, LOG_NAME, KLOGGER_ID.constructClassLikeType(),
    isVal = true, hasBackingField = false) {
    modality = Modality.OPEN
    status { isOverride = superLog(owner) == SuperLog.OPEN }
}
```

It works: `inh.Sub` logs as `inh.Sub`, and a subclass in a consuming module logs under its own
name. Two things had to be added to make it safe:

- **`superLog()` refuses to override a supertype `log` that is not a `KLogger`.** Without the
  return-type check the plugin silently replaced `Framework.log: String` with a logger, and the
  compiler accepted the invalid override (`06-subclass-own-log/C.kt`).
- **`LogStatusTransformer`** rewrites a user's own `log` in a subclass to `public override`, so
  a hand-written logger is not rejected with "hides member of supertype" (`A.kt`).

Measured on `06-subclass-own-log/`, both variants:

| Case | `src/main/kotlin/` | `variant-override/` |
| --- | --- | --- |
| inheritance (`03`) | ❌ `Sub` logs as `inh.Base` | ✅ `inh.Sub` |
| `A` subclass `val log` (public `KLogger`) | ❌ hides member of supertype | ✅ `USER.SUB` kept |
| `B` subclass `private val log` (`KLogger`) | ❌ hides member of supertype | ❌ cannot weaken access privilege |
| `C` framework `open val log: String` | ✅ | ✅ (after the return-type check) |
| `D` subclass `override val log: String` | ❌ (correct — type is not a subtype) | ❌ (same) |
| `E` subclass `private val log` (slf4j) | ❌ hides member of supertype | ❌ cannot weaken access privilege |
| `F` `ArrayList` / `RuntimeException` subclass | ✅ | ✅ |
| user-written `log` extension (`04`) | ❌ silently overridden | ❌ silently overridden |

So `variant-override/` is a strict improvement — A/B/E already failed under the first variant,
and it fixes the logger name plus A. `B` and `E` survive in both, and they are the ones that
matter: `private val log = KotlinLogging.logger { }` inside a subclass stops compiling, because
an override may not narrow visibility below the generated `public` member it overrides. Adding
the accessor overload of `transformStatus` does not help — Kotlin has no `private override`.

## `variant-modes/` — `AnnotationOnly` and `PackageScan`

`variant-override/` generates `log` in every class, so it only answers the `All`-mode
question. `variant-modes/Plugin.kt` adds the processor's two scoping modes through a
`CommandLineProcessor`, which is the plugin's equivalent of `ksp { arg(...) }`:

```
-P plugin:proto.log:mode=AnnotationOnly
-P plugin:proto.log:mode=PackageScan -P plugin:proto.log:targets=scanned.*
```

Testdata: `testdata/07-modes/`.

### Partial selection has the same inheritance defect, and needs the same rule

The generated `log` is a **member**, so a subclass of a selected class inherits it. If the
subclass is not itself selected, it has no `log` of its own and logs under its superclass's
name — the same defect the extension-based processor has for the same reason. Selecting by
mode alone is not enough:

| | selected by mode only | `getsLog` also returns true when `superLog == OPEN` |
| --- | --- | --- |
| `Base().who()` (`@Log` on `Base`) | `annmode.Base` | `annmode.Base` |
| `Sub().mine()` (no annotation) | ❌ `annmode.Base` | ✅ `annmode.Sub` |
| `Plain` (no annotation, no supertype) | no `log` | no `log` |
| `ScannedBase().who()` (`targets=scanned.*`) | `scanned.ScannedBase` | `scanned.ScannedBase` |
| `OutsideSub().mine()` (package `manual`) | ❌ `scanned.ScannedBase` | ✅ `manual.OutsideSub` |
| `OutsideOnly` (package `manual`) | no `log` | no `log` |

One clause in `getsLog` covers it — a class that *inherits* a `log` gets one, whether or not
the mode picks it:

```kotlin
return selectedByMode(classSymbol) || inherited == SuperLog.OPEN
```

### Where the plugin beats KSP: a superclass in a dependency

`@Log` is `@Retention(SOURCE)`, so it is not in a published class file. KSP cannot see it and
cannot recognise a superclass from a dependency as a target — under `AnnotationOnly` that case
is structurally out of reach.

The plugin does not need the annotation to survive: a dependency compiled with the plugin
carries a real `open val log: KLogger` **member**, and that member is the signal. Measured on
`testdata/07-modes/cross-module/`, both modules compiled with `mode=AnnotationOnly` and no
annotation anywhere in `mod-b`:

```
Sub().who()         = appb.Sub
Sub().mine()        = appb.Sub
Unrelated has log   = false
```

### What the modes do *not* buy you

`B`/`E` above — `private val log` in a subclass — still hard-fail, and scoping is not an escape
hatch. The failure is caused by the *superclass* being selected, so narrowing the mode down to
just `Base` still breaks `Sub`; the only way out is not applying the plugin to that hierarchy at
all. Re-measured under `mode=AnnotationOnly`, the `06-subclass-own-log` results are unchanged.

### A semantic divergence worth knowing

A member is virtual; an extension is resolved statically. So for a log statement written in
`Base`'s own body and called on a `Sub` instance, the two designs disagree — measured, not
assumed:

| call | KSP extension | plugin member |
| --- | --- | --- |
| `Base().who()` | `Base` | `Base` |
| `Sub().who()` | `Base` | **`Sub`** |
| `Sub().mine()` | `Sub` | `Sub` |

Both answers have an slf4j idiom behind them (`getLogger(Base::class.java)` vs
`getLogger(javaClass)`), so neither is wrong — but they are not the same library.

## Notes for anyone repeating this

Four things cost real debugging time and are not in any docs:

1. **Never query the member scope from `getCallableNamesForClass`.**
   `processAllDeclarations` / `declaredMemberScope` recurse back into the callback that is
   building that scope — `StackOverflowError` with no diagnostic. The escape hatch is
   `declarationSymbols` under `@OptIn(DirectDeclarationsAccess::class)`, an explicitly
   discouraged API.
2. **The plugin key lands on the `IrProperty`, not on its accessor.** The getter arrives as
   `DEFAULT_PROPERTY_ACCESSOR`, so matching on the accessor's own origin matches nothing.
3. **The accessor's origin must be overwritten before installing a body**, or a later
   lowering regenerates the default body and discards yours. Symptom: compiles clean,
   returns `null` at runtime.
4. **`hasBackingField = false` is not honoured.** Fir2Ir adds one anyway when the accessor
   arrives body-less. Harmless on a class; on an interface it emits a private final
   instance field and the JVM rejects the class at load time
   (`ClassFormatError: Illegal field modifiers`). Hence `property.backingField = null`.

## Version sensitivity

Measured by building the same source against four cached compiler versions and
cross-running the artifacts:

| Compiler | Source compiles | 2.4.10-built plugin runs |
| --- | --- | --- |
| 2.2.21 | ❌ `'pluginId' overrides nothing` | — |
| 2.3.21 | ✅ | ❌ `NoClassDefFoundError: ExtensionPointDescriptor` |
| 2.4.0 | ✅ | ✅ |
| 2.4.10 | ✅ | ✅ |

The reverse direction breaks too — a 2.3.21-built plugin on kotlinc 2.4.10 dies with
`ClassCastException: FirExtensionRegistrarAdapter$Companion cannot be cast to
ProjectExtensionDescriptor`. That direction is the one that matters: users could not
upgrade Kotlin until this repo shipped a matching release. 3.0.0 moved off exactly that
treadmill by building on KSP2's stable APIs.
