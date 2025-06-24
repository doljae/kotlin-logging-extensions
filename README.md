# Kotlin Logging Extensions

A Kotlin Symbol Processing (KSP) library that automatically generates logger extensions for Kotlin classes using [kotlin-logging](https://github.com/oshai/kotlin-logging).

## Features

- **Automatic Logger Generation**: Automatically generates logger extensions for all Kotlin classes during compilation
- **Zero Boilerplate**: No need to manually declare loggers in each class
- **Structured Logging**: Uses kotlin-logging for structured and efficient logging
- **Compile-time Safety**: Leverages KSP for compile-time code generation with no runtime overhead
- **Package-aware Naming**: Logger names are automatically generated based on the fully qualified class name

## Installation

### Gradle (Kotlin DSL)

Add the following dependencies to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

dependencies {
    // Kotlin logging dependencies
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    
    // kotlin-logging-extensions
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1-SNAPSHOT")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1-SNAPSHOT")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content {
            includeGroup("io.github.doljae")
        }
    }
}
```

## Usage

Once the dependencies are added, you can use the automatically generated `log` property in any Kotlin class:

```kotlin
package com.example

class UserService {
    fun createUser(name: String) {
        log.info { "Creating user: $name" }
        
        try {
            // User creation logic
            log.debug { "User created successfully: $name" }
        } catch (e: Exception) {
            log.error(e) { "Failed to create user: $name" }
        }
    }
}
```

The logger will be automatically available as a `log` property with the logger name set to the fully qualified class name (`com.example.UserService` in this example).

## How It Works

The library uses Kotlin Symbol Processing (KSP) to:

1. **Scan all Kotlin classes** during compilation
2. **Generate extension files** for each class with a `log` property
3. **Create package-specific loggers** with names based on the fully qualified class name

For a class `com.example.UserService`, the processor generates:

```kotlin
package com.example

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

val UserService.log: KLogger
    get() = KotlinLogging.logger("com.example.UserService")
```

## Project Structure

```
kotlin-logging-extensions/
├── processor/           # KSP processor implementation
│   └── src/main/kotlin/
│       └── io/github/doljae/kotlinlogging/extensions/
│           ├── LoggerProcessor.kt         # Main KSP processor
│           └── LoggerProcessorProvider.kt # KSP provider
└── workload/           # Example/test module
    └── src/main/kotlin/
        └── [example classes demonstrating usage]
```

## Building from Source

### Prerequisites

- Java 21 or higher
- Kotlin 2.1.20 or higher

### Build Commands

```bash
# Clone the repository
git clone https://github.com/doljae/kotlin-logging-extensions.git
cd kotlin-logging-extensions

# Build the project
./gradlew build

# Run tests
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Development

### Running the Example

The `workload` module contains example usage:

```bash
./gradlew :workload:run
```

### Code Style

This project uses ktlint for code formatting:

```bash
# Check code style
./gradlew ktlintCheck

# Format code
./gradlew ktlintFormat
```

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/new-feature`
3. Make your changes
4. Run tests: `./gradlew test`
5. Check code style: `./gradlew ktlintCheck`
6. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
7. Push to the branch: `git push origin feat/new-feature`
8. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0.txt) file for details.

## Related Projects

- [kotlin-logging](https://github.com/oshai/kotlin-logging) - Lightweight logging framework for Kotlin
- [Kotlin Symbol Processing (KSP)](https://github.com/google/ksp) - Kotlin's annotation processing framework

## Author

**Seokjae Lee** - [@doljae](https://github.com/doljae) 