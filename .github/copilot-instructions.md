# GitHub Copilot Instructions for kotlin-logging-extensions

You are an expert Kotlin developer assisting with a KSP (Kotlin Symbol Processing) project.

## Project Context
This project automatically generates `log` properties for Kotlin classes using KSP.
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
- `LoggerProcessorProvider.kt`: Entry point for KSP.
- `build.gradle.kts`: Build configuration. Do not suggest Groovy syntax for Gradle.

## Dependencies
- Kotlin: 2.x
- KSP: 2.x
- Kotlin Logging: `io.github.oshai:kotlin-logging-jvm`

When suggesting code changes, always prioritize thread safety and build performance.
