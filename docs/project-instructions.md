# kotlin-logging-extensions — Project Instructions

KSP plugin that generates `kotlin-logging` `log` extensions at compile time.

## Modules

| Module | Purpose |
|--------|---------|
| `processor` | KSP processor, code generation, processor unit tests |
| `workload` | Consumer module for integration verification of generated extensions |
| `scripts` | Release and maintenance scripts |

## Build Commands

```
./gradlew clean build       # full build
./gradlew test              # all tests
./gradlew :processor:test   # processor tests only
./gradlew :workload:test    # workload tests only
```

Always use the Gradle wrapper (`./gradlew`), never a system-installed Gradle.

## Key Constraints

- Keep processor logic in `processor`; do not leak it into `workload`.
- Keep `workload` focused on usage examples and regression coverage.
- Generated output must be deterministic for identical source inputs.
- Do not change generated extension signatures without updating tests.

## General Coding Guidelines

See [karpathy-coding-guidelines.md](karpathy-coding-guidelines.md).
