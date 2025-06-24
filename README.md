# Kotlin Logging Extensions

[![CI](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml)
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

**Step 1: Setup GitHub Packages Access**

Create a [GitHub Personal Access Token](https://github.com/settings/tokens) with `read:packages` scope, then set environment variables:
```bash
export GITHUB_USERNAME="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"
```

**Step 2: Configure Repository**

Add to your `build.gradle.kts`:
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

**Step 3: Add Dependencies**
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
}
```

**Step 4: Use `log` in Any Class**
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

> **💡 Coming Soon**: Maven Central support will eliminate Steps 1-2, making setup even simpler!

## ✨ Features

- **🔧 Zero Boilerplate**: No logger declarations needed - just use `log.info { }`
- **⚡ Compile-time Generation**: Uses KSP for compile-time safety with zero runtime overhead  
- **📦 Package-aware Naming**: Logger names automatically match fully qualified class names
- **🏗️ kotlin-logging Integration**: Works seamlessly with the standard kotlin-logging library
- **🎯 Works Everywhere**: Compatible with any package depth and class structure

## 📦 Installation

### Current: GitHub Packages
Follow the [Quick Start](#quick-start) guide above for complete setup instructions.

### Coming Soon: Maven Central  
```kotlin
repositories {
    mavenCentral()  // No authentication needed!
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
}
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