# GitHub Copilot Instructions for kotlin-logging-extensions

You are an expert Kotlin developer assisting with a KSP (Kotlin Symbol Processing) project.

## Project Context
This project automatically generates `log` properties for Kotlin classes using KSP. Generation is
project-wide by default; `kotlinloggingextensions.mode` narrows it to `PackageScan` or
`AnnotationOnly`.
- **Module `annotations`**: `@Log` (and the deprecated `@AutoLog`), published separately as
  `kotlin-logging-extensions-annotations`. It is the only artifact on a consumer's compile classpath,
  so it must stay dependency-free and on Kotlin language/API 2.0.
- **Module `processor`**: Contains the KSP logic.
- **Module `workload`**: Contains example code and integration scenarios.

## Coding Conventions
1. **Kotlin Idioms**: Use idiomatic Kotlin (extension functions, scope functions `let/apply/also`, sealed classes).
2. **KSP API**: When using KSP, prefer `KSClassDeclaration`, `KSFunctionDeclaration`, etc. Be mindful of the difference between `KSName` and `String`.
3. **Tests**:
   - Write tests in `processor/src/test/kotlin`.
   - Use **Kotest Assertions** (`infix` style: `value shouldBe expected`).
   - Use `SourceFile.kotlin(...)` to mock source code for compilation tests.

## Important Files
- `LoggerProcessor.kt`: Main logic for code generation.
- `LoggerProcessorProvider.kt`: Entry point for KSP, option parsing and mode resolution.
- `LoggerGenerationMode.kt`: `All` / `PackageScan` / `AnnotationOnly`.
- `annotations/.../Log.kt`: The `@Log` annotation (`AutoLog.kt` is the deprecated pre-3.0.0 name).
- `build.gradle.kts`: Build configuration. Do not suggest Groovy syntax for Gradle.

## Dependencies
- JDK: 17 (`jvmToolchain(17)`; do not raise it, since it is published as `org.gradle.jvm.version`)
- Kotlin: 2.x
- KSP: 2.x
- Kotlin Logging: `io.github.oshai:kotlin-logging-jvm` 5.0.0+

When suggesting code changes, always prioritize thread safety and build performance.
