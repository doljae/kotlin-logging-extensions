# Kotlin Logging Extensions

[![CI](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.doljae/kotlin-logging-extensions.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.doljae/kotlin-logging-extensions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![KSP](https://img.shields.io/badge/KSP-2.1.21--2.0.2-purple.svg)](https://github.com/google/ksp)

**Automatic logger generation for Kotlin classes using KSP.** Write `log.info { }` in any class without boilerplate!

## 🚀 Quick Start

### What It Does
Automatically generates logger extensions for every Kotlin class during compilation. No manual logger declarations needed - just use `log` directly in any class.

```kotlin
// ❌ Before: Manual logger in every class
class UserService {
    private val log = KotlinLogging.logger {}  // Boilerplate!
    
    fun createUser() {
        log.info { "Creating user" }
    }
}

// ✅ After: Just use log directly
class UserService {
    fun createUser() {
        log.info { "Creating user" }  // Auto-generated!
    }
}
```

### How to Use

**Step 1: Add Dependencies**

Add to your `build.gradle.kts`:
```kotlin
plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
}
```

**Step 2: Use `log` in Any Class**
```kotlin
class OrderProcessor {
    fun processOrder(id: String) {
        log.info { "Processing order: $id" }
        
        try {
            // Business logic here
            log.debug { "Order processed successfully" }
        } catch (e: Exception) {
            log.error(e) { "Failed to process order: $id" }
        }
    }
}
```

That's it! The logger is automatically available with the class name (`OrderProcessor` in this example).

## ⚠️ Version Compatibility

> **Important**: This project is highly dependent on Kotlin and KSP versions due to its nature as a symbol processing library.

### 🔧 Required Versions
- **Kotlin**: `2.1.21`
- **KSP**: `2.1.21-2.0.2`

### 📋 Before Using
Please ensure your project uses the **exact same versions** as shown above. Version mismatches may cause:
- Compilation failures
- Missing log property generation
- Runtime issues

### 🔍 How to Check Your Versions
```kotlin
// In your build.gradle.kts, check:
plugins {
    kotlin("jvm") version "2.1.21"  // ← Should match exactly
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"  // ← Should match exactly
}
```

### 🚀 Upgrade if Needed
If your versions don't match, consider upgrading:
```kotlin
plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}
```

### 💡 Future Improvements
We're working on improving version compatibility in future releases. Have ideas or suggestions? 
**We'd love to hear from you!** Please open an issue in the [Issues tab](https://github.com/doljae/kotlin-logging-extensions/issues).

## ✨ Features

- **🔧 Zero Boilerplate**: No logger declarations needed - just use `log.info { }`
- **⚡ Compile-time Generation**: Uses KSP for compile-time safety with zero runtime overhead  
- **📦 Package-aware Naming**: Logger names automatically match fully qualified class names
- **🏗️ kotlin-logging Integration**: Works seamlessly with the standard kotlin-logging library
- **🎯 Works Everywhere**: Compatible with any package depth and class structure

## 📦 Installation

### Maven Central (Recommended)
```kotlin
plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
}
```

### GitHub Packages (Alternative)
For development or specific use cases, you can also use GitHub Packages:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/doljae/kotlin-logging-extensions")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}
```

**Note**: GitHub Packages requires authentication. Set environment variables:
```bash
export GITHUB_USERNAME="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"
```

## 📝 Examples

### Real-world Usage Examples
Check out [`workload/src/main/kotlin/examples/`](workload/src/main/kotlin/examples/) for complete examples:

- **[UserService.kt](workload/src/main/kotlin/examples/UserService.kt)** - User management with validation
- **[OrderProcessor.kt](workload/src/main/kotlin/examples/OrderProcessor.kt)** - Order processing with performance logging  
- **[DataRepository.kt](workload/src/main/kotlin/examples/DataRepository.kt)** - Database operations with different log levels
- **[PaymentServiceImpl.kt](workload/src/main/kotlin/examples/enterprise/service/impl/PaymentServiceImpl.kt)** - Deep package structure

Run the examples:
```bash
./gradlew :workload:run
```

### Different Log Levels
```kotlin
class ApiController {
    fun handleRequest(request: Request) {
        log.debug { "Received request: ${request.id}" }
        log.info { "Processing ${request.type} request" }
        log.warn { "Rate limit approaching for user ${request.userId}" }
        log.error { "Request failed: ${request.id}" }
    }
}
```

## 🔧 How It Works

Uses Kotlin Symbol Processing (KSP) to automatically generate extension properties for each class:

```kotlin
// For class com.example.UserService, generates:
val UserService.log: KLogger
    get() = KotlinLogging.logger("com.example.UserService")
```

- **Compile-time**: No runtime performance impact
- **Type-safe**: Full IDE support and auto-completion
- **Standard logging**: Uses kotlin-logging underneath

## 💡 Why This Project?

**Problem**: Kotlin developers miss Java's Lombok `@Slf4j` simplicity. Current solutions require either:
- Top-level logger declarations (violates "one class per file")  
- Manual logger in every class (repetitive boilerplate)

**Solution**: Automatic logger generation that "just works" - inspired by Lombok's elegance, built with Kotlin's KSP power.

## 🛠️ Development

### Build from Source
```bash
git clone https://github.com/doljae/kotlin-logging-extensions.git
cd kotlin-logging-extensions
./gradlew build
```

### Run Tests
```bash
./gradlew test
./gradlew ktlintCheck
```

### 🚀 Creating a Release

For maintainers: this project uses **KSP-style versioning** with **staged Maven Central releases**.

```bash
# Quick release (recommended)
./scripts/release.sh

# Or manually
git tag v2.1.21-0.0.1  # KOTLIN_VERSION-LIB_VERSION
git push origin v2.1.21-0.0.1
```

This automatically:
- ✅ Runs tests
- 📤 Uploads to Maven Central staging  
- 🏷️ Creates GitHub Release with notes
- ⚠️ **Manual step**: Go to [Sonatype](https://oss.sonatype.org/) to publish

**Version Format**: `KOTLIN_VERSION-LIB_VERSION` (e.g., `2.1.21-0.0.1`)

**Setup Required**: [GitHub Secrets Configuration](docs/SECRETS_SETUP.md) - Required for Maven Central publishing

## 🤝 Contributing

1. Fork and create a feature branch
2. Make your changes with tests
3. Follow [Conventional Commits](https://www.conventionalcommits.org/)
4. Open a Pull Request

## 📄 License

Apache License 2.0 - see [LICENSE](LICENSE) file.

## 🔗 Related Projects

- [kotlin-logging](https://github.com/oshai/kotlin-logging) - The logging framework this project extends
- [KSP](https://github.com/google/ksp) - Kotlin Symbol Processing framework

---

⭐ **If this helps you, please star the repo!** ⭐

**Author**: [Seokjae Lee](https://github.com/doljae) | **Blog**: [doljae.tistory.com](https://doljae.tistory.com) 