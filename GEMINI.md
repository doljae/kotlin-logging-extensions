# Gemini CLI Project Context

> **Note**: This file serves as the primary context for the Gemini CLI agent. It defines the project structure, architecture, and developer guidelines.

## Project Management
- **Tracking Tasks**: Use the `write_todos` tool to maintain a list of active tasks, bugs, and feature requests.
- **Status Updates**: Before starting a major task, check existing TODOs to avoid duplication. After completing a task, mark it as done.

## Project Overview
This project, `kotlin-logging-extensions`, is a Kotlin Symbol Processing (KSP) plugin designed to automatically generate `KLogger` instances for Kotlin classes. It simplifies logging by removing the boilerplate of declaring loggers manually.

## Architecture
The project is a multi-module Gradle project:
- **`processor`**: The core module containing the KSP processor logic. It scans user code and generates Kotlin extension properties for logging.
- **`workload`**: A consumer module used for testing and demonstrating the processor's capabilities. It depends on the `processor` module.

### How it works
1. The `processor` looks for classes (currently all classes, or specific ones based on future logic).
2. It generates a companion object extension property or a top-level extension property named `log`.
3. The generated code uses `io.github.oshai.kotlin-logging.KotlinLogging` to instantiate the logger.

## Tech Stack
- **Language**: Kotlin (v2.2.21)
- **Build System**: Gradle (Kotlin DSL)
- **Compiler Plugin**: KSP (Kotlin Symbol Processing) v2.2.21-2.0.4
- **Testing**: 
  - Framework: JUnit 5
  - Assertions: Kotest Assertions
  - Compilation Testing: `kotlin-compile-testing-ksp`
- **Logging Library**: `kotlin-logging-jvm` (v7.0.14)

## Key Directories
- `processor/src/main/kotlin`: KSP Processor implementation (`LoggerProcessor`, `LoggerProcessorProvider`).
- `processor/src/test/kotlin`: Unit tests for the processor.
- `workload/src/main/kotlin`: Example usage of the plugin.

## Development Workflow

### Build & Test
- **Run all tests**: `./gradlew test`
- **Run processor tests**: `./gradlew :processor:test`
- **Build project**: `./gradlew build`

### Common Tasks for Agents
1. **Adding Features**: Modify `LoggerProcessor.kt` to change generation logic. Ensure to update `LoggerProcessorTest.kt`.
2. **Debugging**: Use `workload` module to verify generated code behavior in a real compiled environment.
3. **Dependencies**: Manage dependencies in `build.gradle.kts` (root or module-level). Prefer explicit versions over catalog for this small project scale unless refactoring to `libs.versions.toml` is requested.

## Code Style Guidelines
- Follow official Kotlin coding conventions.
- Use `val` over `var` wherever possible.
- Prefer expression bodies for single-line functions.
- Use trailing commas in multi-line definitions.
