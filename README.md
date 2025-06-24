# Kotlin Logging Extensions

[![CI](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![KSP](https://img.shields.io/badge/KSP-2.1.21--2.0.2-purple.svg)](https://github.com/google/ksp)

A Kotlin Symbol Processing (KSP) library that automatically generates logger extensions for Kotlin classes using [kotlin-logging](https://github.com/oshai/kotlin-logging).

> **Current Version**: 0.0.1  
> **Status**: Initial Release - Production Ready  
> **Compatibility**: Kotlin 2.1.21+, Java 21+

## 💡 Why This Project Exists

### The Problem with Current Kotlin Logging Approaches

While Kotlin allows multiple classes in a single file, this approach has several drawbacks in practice ([detailed analysis](https://doljae.tistory.com/347)):

- **IDE Navigation Issues**: Harder to locate specific classes in file trees and search results
- **Code Review Complexity**: Multiple classes in one file make pull requests harder to review
- **Version Control Conflicts**: Higher chance of merge conflicts when multiple developers modify the same file
- **Testing Structure Mismatch**: Inconsistency between source and test file organization
- **Package Structure Violation**: Breaks the logical relationship between package hierarchy and file structure
- **Readability Concerns**: Cognitive overhead when scanning through multiple class definitions

For maintainable code, the **one class per file** principle remains preferable.

**Java developers** migrating to Kotlin often miss **Lombok's `@Slf4j` annotation**, which seamlessly provides a `log` variable without any boilerplate. Unfortunately, **Lombok's annotation processing doesn't integrate well with Kotlin** due to differences in compilation phases.

### Current kotlin-logging Solutions and Their Limitations

[kotlin-logging](https://github.com/oshai/kotlin-logging) is the de facto logging standard for Kotlin JVM projects. However, the official approaches have trade-offs:

```kotlin
// 🚫 Top-level declaration: breaks "one class per file" principle
private val log = KotlinLogging.logger {}

class UserService {
    fun createUser() {
        log.info { "Creating user" } // Works, but violates file organization
    }
}
```

```kotlin
// 🚫 Traditional approach: verbose and repetitive
class UserService {
    private val log = KotlinLogging.logger {}  // Boilerplate in every class
    
    fun createUser() {
        log.info { "Creating user" }
    }
}
```

### Our Solution: Lombok-Style Simplicity with KSP

**Logging is cross-cutting concern** (AOP-suitable) that shouldn't clutter business logic. The **Lombok approach is elegant** - just use `log` without any declarations.

This project brings that simplicity to Kotlin:

```kotlin
// ✅ kotlin-logging-extensions: Clean and simple
class UserService {
    fun createUser() {
        log.info { "Creating user" } // No boilerplate needed!
    }
}
```

### Why KSP Over Compiler Plugins?

- **Compiler plugins** are version-dependent and require vendor support for maintenance
- **KSP** is officially supported by JetBrains and provides stable APIs
- **Goal**: Enable `log` variable usage in **every class** without any identifiers or declarations

### Philosophy

**Business logic should focus on business logic.** Infrastructure concerns like logging should be invisible and effortless. This project makes logging as natural as breathing - just use `log` and focus on what matters.

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Usage](#usage)
- [How It Works](#how-it-works)
- [Building from Source](#building-from-source)
- [Development](#development)
- [Testing](#testing)
- [Contributing](#contributing)
- [Security](#security)
- [License](#license)
- [Dependencies](#dependencies)
- [Related Projects](#related-projects)

## Quick Start

1. **Add the dependency** to your `build.gradle.kts`:
   ```kotlin
   dependencies {
       ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
       implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
       implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
   }
   ```

2. **Use the auto-generated logger** in any Kotlin class:
   ```kotlin
   class MyService {
       fun doSomething() {
           log.info { "Hello from auto-generated logger!" }
       }
   }
   ```

That's it! No manual logger declarations needed. 🎉

### 📁 **Explore Real Examples**

Check out the [`workload/`](workload/src/main/kotlin/examples/) directory for comprehensive usage examples:

- **[UserService.kt](workload/src/main/kotlin/examples/UserService.kt)** - User management with validation and error handling
- **[OrderProcessor.kt](workload/src/main/kotlin/examples/OrderProcessor.kt)** - Order processing with performance logging  
- **[DataRepository.kt](workload/src/main/kotlin/examples/DataRepository.kt)** - Database operations with different log levels
- **[PaymentServiceImpl.kt](workload/src/main/kotlin/examples/enterprise/service/impl/PaymentServiceImpl.kt)** - Deep package structure example
- **[Main.kt](workload/src/main/kotlin/examples/Main.kt)** - Complete demonstration

Run the examples:
```bash
./gradlew :workload:run
```

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
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

dependencies {
    // Kotlin logging dependencies
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    
    // kotlin-logging-extensions
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
}
```

### Repository Configuration

You have multiple options for downloading the dependency:

#### Option 1: Maven Central (Recommended - Coming Soon)
```kotlin
repositories {
    mavenCentral()
}
```

#### Option 2: GitHub Packages
```kotlin
repositories {
    mavenCentral()
    
    // GitHub Packages (requires authentication)
    maven {
        url = uri("https://maven.pkg.github.com/doljae/kotlin-logging-extensions")
        credentials {
            username = System.getenv("GITHUB_USERNAME") 
                ?: project.findProperty("githubPackagesUsername") as String?
            password = System.getenv("GITHUB_TOKEN") 
                ?: project.findProperty("githubPackagesPassword") as String?
        }
    }
}
```

### GitHub Packages Authentication

To use GitHub Packages, you need to configure authentication:

#### Method 1: Environment Variables (Recommended)
```bash
export GITHUB_USERNAME="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"
```

#### Method 2: gradle.properties
Add to your `~/.gradle/gradle.properties` or project's `gradle.properties`:
```properties
githubPackagesUsername=your-github-username
githubPackagesPassword=your-personal-access-token
```

#### Creating a GitHub Personal Access Token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token"
3. Select scopes: `read:packages`
4. Copy the generated token

**Note:** Keep your token secure and never commit it to version control!

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
├── processor/                          # 🔧 KSP processor implementation
│   ├── src/main/kotlin/
│   │   └── io/github/doljae/kotlinlogging/extensions/
│   │       ├── LoggerProcessor.kt      # Main KSP processor
│   │       └── LoggerProcessorProvider.kt # KSP provider
│   └── src/test/kotlin/                # Unit tests
└── workload/                           # 📚 Real-world examples
    └── src/main/kotlin/examples/
        ├── UserService.kt              # User management example
        ├── OrderProcessor.kt           # Order processing example
        ├── DataRepository.kt           # Database operations example
        ├── enterprise/service/impl/    # Deep package structure
        │   └── PaymentServiceImpl.kt   # Enterprise-style nested packages
        └── Main.kt                     # Complete demonstration
```

## Building from Source

### Prerequisites

- Java 21 or higher
- Kotlin 2.1.21 or higher

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

### Running the Examples

The `workload` module contains comprehensive real-world examples:

```bash
# Run all examples
./gradlew :workload:run

# Build and see generated code
./gradlew :workload:build
```

**Example Output:**
- User management operations with validation logging
- Order processing with performance metrics
- Database operations with different log levels
- Deep package structure compatibility (`examples.enterprise.service.impl`)
- Error handling and exception logging patterns

### Code Style

This project uses ktlint for code formatting:

```bash
# Check code style
./gradlew ktlintCheck

# Format code
./gradlew ktlintFormat
```

### Continuous Integration

This project uses GitHub Actions for automated:
- Building and testing on multiple platforms
- Code style validation with ktlint
- Security vulnerability scanning with Trivy
- Automated dependency updates

## Testing

The project includes comprehensive testing:

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run processor-specific tests
./gradlew :processor:test
```

### Test Structure

- **Unit Tests**: Basic functionality testing for the KSP processor
- **Integration Tests**: End-to-end testing with real Kotlin compilation
- **Style Tests**: Automated ktlint validation

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/new-feature`
3. Make your changes
4. Run tests: `./gradlew test`
5. Check code style: `./gradlew ktlintCheck`
6. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
7. Push to the branch: `git push origin feat/new-feature`
8. Open a Pull Request

## Security

We take security seriously. If you discover a security vulnerability, please follow our responsible disclosure process:

- **Do NOT** report security vulnerabilities through public GitHub issues
- Email security reports to: [seok9211@naver.com](mailto:seok9211@naver.com)
- See our [Security Policy](SECURITY.md) for detailed guidelines

### Security Features

- **Compile-time only**: No runtime dependencies or security risks
- **Minimal attack surface**: Simple code generation with minimal dependencies
- **No network access**: Processor doesn't make external connections
- **Automated security scanning**: Trivy vulnerability scanning in CI/CD

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Dependencies

This project maintains minimal dependencies to reduce security risks and conflicts:

### Core Dependencies

| Dependency | Version | Purpose | License |
|------------|---------|---------|---------|
| [Kotlin Symbol Processing (KSP)](https://github.com/google/ksp) | 2.1.21-2.0.2 | Compile-time code generation | Apache 2.0 |
| [kotlin-logging](https://github.com/oshai/kotlin-logging) | 7.0.7 | Runtime logging facade | Apache 2.0 |

### Development Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| ktlint | 1.5.0 | Code style enforcement |
| logback-classic | 1.5.18 | Logging implementation (examples) |

### Why These Dependencies?

- **KSP**: Required for compile-time code generation, officially supported by JetBrains
- **kotlin-logging**: Proven, lightweight logging facade with excellent Kotlin support
- **Minimal footprint**: Only essential dependencies to minimize version conflicts

## Related Projects

- [kotlin-logging](https://github.com/oshai/kotlin-logging) - Lightweight logging framework for Kotlin
- [Kotlin Symbol Processing (KSP)](https://github.com/google/ksp) - Kotlin's annotation processing framework

## Author

**Seokjae Lee** - [@doljae](https://github.com/doljae)
- Email: [seok9211@naver.com](mailto:seok9211@naver.com)  
- GitHub: [https://github.com/doljae](https://github.com/doljae)
- Blog: [https://doljae.tistory.com](https://doljae.tistory.com)

---

⭐ **If this project helps you, please consider giving it a star!** ⭐ 