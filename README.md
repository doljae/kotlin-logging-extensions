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

### 🌐 kotlin-logging Compatibility
This project was developed and tested with **kotlin-logging-jvm**, but it should be compatible with other kotlin-logging variants:
- `kotlin-logging-js` for JavaScript/Node.js environments
- `kotlin-logging-linuxx64`, `kotlin-logging-mingwx64` for native targets
- Other platform-specific variants

> **Note**: While broader compatibility is expected due to the shared kotlin-logging API, only `kotlin-logging-jvm` has been thoroughly tested.

### ⚠️ Logger Implementation Required
**Important**: kotlin-logging is a facade that requires an actual logger implementation to function properly.

**You must also add one of these logger implementations:**
- **Logback** (recommended): `ch.qos.logback:logback-classic:1.4.12`
- **Log4j2**: `org.apache.logging.log4j:log4j-slf4j2-impl:2.21.1`
- **Simple**: `org.slf4j:slf4j-simple:2.0.9`

**Example with Logback:**
```kotlin
dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.12")  // ← Logger implementation
}
```

Without a logger implementation, your logs will not be printed or processed.

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