# kotlin-logging-extensions — Project Instructions

KSP plugin that generates `kotlin-logging` `log` extensions at compile time.

## Modules

| Module | Purpose |
|--------|---------|
| `annotations` | `@Log` (and deprecated `@AutoLog`), published as `kotlin-logging-extensions-annotations` |
| `processor` | KSP processor, code generation, processor unit tests |
| `workload` | Consumer module for integration verification of generated extensions |
| `scripts` | Release and maintenance scripts (not a Gradle module) |

## Build Commands

```
./gradlew clean build       # full build
./gradlew test              # all tests
./gradlew :processor:test   # processor tests only
./gradlew :workload:test    # workload tests only
./gradlew ktlintCheck       # lint
```

Always use the Gradle wrapper (`./gradlew`), never a system-installed Gradle.

## Key Constraints

- Keep processor logic in `processor`; do not leak it into `workload`.
- Keep `workload` focused on usage examples and regression coverage.
- Generated output must be deterministic for identical source inputs.
- Do not change generated extension signatures without updating tests.
- `annotations` is the only artifact on a consumer's compile classpath: it publishes zero
  dependencies (enforced by a `check` task) and targets Kotlin language/API 2.0. Adding a dependency
  or raising that floor is a breaking change for consumers.
- Every module uses `jvmToolchain(17)`. For the two published ones the toolchain sets
  `org.gradle.jvm.version` in the module metadata, so raising it breaks resolution on JDK 17
  consumers (issue #152).
- The processor matches `@Log`/`@AutoLog` by qualified name and must not depend on `:annotations`
  outside tests. A `typealias` shim is not usable, because KSP reports aliases under their own name.
- Generation is project-wide (`All`) by default; `kotlinloggingextensions.mode` /
  `.targets` narrow it. One file per package per source set:
  `KotlinLoggingExtensions_<module>.kt`.

## General Coding Guidelines

See [karpathy-coding-guidelines.md](karpathy-coding-guidelines.md).
