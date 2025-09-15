# Kotlin Logging Extensions

[![CI](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.doljae/kotlin-logging-extensions.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.doljae/kotlin-logging-extensions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![kotlin-logging](https://img.shields.io/badge/kotlin--logging-5.0.0+-green.svg)](https://github.com/oshai/kotlin-logging)
[![KSP](https://img.shields.io/badge/KSP-2.2.20--2.0.3-purple.svg)](https://github.com/google/ksp)

**Elegant [kotlin-logging](https://github.com/oshai/kotlin-logging) extensions for zero-boilerplate logger generation in Kotlin classes using [KSP](https://github.com/google/ksp)**

**Write `log.info { }` in any class without boilerplate!**

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
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.19") // Logger implementation required
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

**Step 3: Generate Logger Code**
After writing your code, run KSP to generate the logger extensions:
```bash
./gradlew kspKotlin kspTestKotlin
```
This will generate the `log` property and resolve any compilation errors in your IDE.

That's it! The logger is automatically available with the class name (`OrderProcessor` in this example).

## ✨ Features

- **🔧 Zero Boilerplate**: No logger declarations needed - just use `log.info { }`
- **⚡ Compile-time Generation**: Uses KSP for compile-time safety with zero runtime overhead  
- **📦 Package-aware Naming**: Logger names automatically match fully qualified class names
- **🏗️ kotlin-logging Integration**: Works seamlessly with the standard kotlin-logging library
- **🎯 Works Everywhere**: Compatible with any package depth and class structure

## 📋 Version Compatibility

**Choose the library version that matches your project's Kotlin version.** Our versioning follows the `KOTLIN_VERSION-LIBRARY_VERSION` pattern (same as KSP).

| Library | Kotlin | KSP |
|---------|--------|-----|
| `2.2.20-0.0.4` | `2.2.20` | `2.2.20-2.0.3` |
| `2.2.20-0.0.3` | `2.2.20` | `2.2.20-2.0.2` |
| `2.2.10-0.0.3` | `2.2.10` | `2.2.0-2.0.2` |
| `2.2.0-0.0.3` | `2.2.0` | `2.2.0-2.0.2` |
| `2.1.21-0.0.3` | `2.1.21` | `2.1.21-2.0.2` |
| `2.2.0-0.0.2` | `2.2.0` | `2.2.0-2.0.2` |
| `2.1.21-0.0.1` | `2.1.21` | `2.1.21-2.0.2` |

### How to Use
1. **Check your Kotlin version** in `build.gradle.kts`
2. **Pick the matching library version** from the table above
3. **Use the exact KSP version** shown in the table

```kotlin
// For Kotlin 2.2.20 projects:
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13") // 5.0.0+
}
```

**kotlin-logging compatibility**: This library requires kotlin-logging 5.0.0+ due to package name changes. Versions 5.x+ use `io.github.oshai.kotlinlogging` package, while older versions used `mu` package.

**Logger implementation**: If you're already using kotlin-logging in your project, no additional setup needed. For new projects, add a logger implementation like [Logback](https://logback.qos.ch/) or [Log4j2](https://logging.apache.org/log4j/2.x/).

## 📦 Installation

### Maven Central (Recommended)
```kotlin
plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.doljae:kotlin-logging-extensions:2.2.20-0.0.4")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.19")
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

---

⭐ **If this helps you, please star the repo!** ⭐ 
