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

**Short answer: it removes the limitation and breaks two things that matter more.**
See #172 for the full write-up.

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
| `03-inheritance.kt` | ❌ `Sub` logs as `inh.Base`; KSP gives `inh.Sub` |
| `04-user-extension.kt` | ❌ a user-written `Service.log` extension is silently overridden, no warning |
| `05-top-level-log.kt` | ⚠️ a top-level `log` is unconditionally shadowed inside classes, which changes what `warnIfTopLevelLogIsShadowed()` would have to say |
| `cross-module/` | ❌→✅ `IncompatibleClassChangeError` when a subclass in another module inherits a plugin-generated `log`; fixed, but the logger is still named after the superclass |

Run `03` and `04` with and without `-Xplugin=` to see the difference.

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
