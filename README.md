# Kotlin Logging Extensions

[![CI](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml/badge.svg)](https://github.com/doljae/kotlin-logging-extensions/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.doljae/kotlin-logging-extensions.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.doljae/kotlin-logging-extensions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0+-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![kotlin-logging](https://img.shields.io/badge/kotlin--logging-5.0.0+-green.svg)](https://github.com/oshai/kotlin-logging)
[![KSP](https://img.shields.io/badge/KSP-KSP1%20%7C%20KSP2-purple.svg)](https://github.com/google/ksp)

**Elegant [kotlin-logging](https://github.com/oshai/kotlin-logging) extensions for zero-boilerplate logger generation in
Kotlin classes using [KSP](https://github.com/google/ksp)**

**Write `log.info { }` in any class without boilerplate!**

## 🚀 Quick Start

### What It Does

Generates a `log` extension at compile time for every class in your project. No annotations, no
configuration — add the processor and `log` is there.

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
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:3.0.0")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
    implementation("ch.qos.logback:logback-classic:1.6.2") // Logger implementation required
}
```

Each source set is processed separately, so add `kspTest(...)` as well if your test classes use `log`:

```kotlin
dependencies {
    kspTest("io.github.doljae:kotlin-logging-extensions:3.0.0")
}
```

**Step 2: Generate the extensions**

```bash
./gradlew build
```

KSP runs ahead of the Kotlin compiler inside the same build, so `build` generates and compiles in one
pass. Run `./gradlew kspKotlin kspTestKotlin` instead if you only want the generated sources without
a full build.

Every class that already exists in a processed source set has a `log` once this finishes, so the call
sites you write next resolve straight away.

**Step 3: Use `log`**

Nothing to annotate, nothing to configure. The extension is already there, so just call it:

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

That's it! The logger is named after the fully qualified class name.

### How Generation Is Decided

The processor never looks at where you call `log`. On every build it walks the *class declarations*
in the source set and writes a `log` extension for each class that qualifies, whether or not that
class logs anything. A class that already declares its own `log` (on the class or on its companion)
is skipped, so a hand-written logger always wins.

Output lands in `build/generated/ksp/<source set>/kotlin`, one file per package named
`KotlinLoggingExtensions_<module>.kt`. The module suffix is what keeps `main` and `test` from
colliding when they share a package.

A class you write *after* the last build has no extension yet, so run Step 2 again and your IDE will
resolve `log` once it has indexed the new sources. That is an indexing lag, not a missing generation
step, and it is covered under
[Troubleshooting & IDE Support](#-troubleshooting--ide-support).

### Scoping Generation

By default every class gets a `log` extension. If you want to narrow that, set a mode in
`build.gradle.kts`.

**Limit to specific packages** — `PackageScan`:

```kotlin
ksp {
    arg("kotlinloggingextensions.mode", "PackageScan")
    arg("kotlinloggingextensions.targets", "com.example.service.*,com.example.repository.*")
}
```

Target rules:

- `com.example` — exact package match only
- `com.example.*` — matches `com.example` and all sub-packages
- Multiple targets: comma-separated (e.g., `com.example.*,com.other.*`)
- Invalid target entries are ignored with a warning
- If `kotlinloggingextensions.targets` is set without `mode`, package scanning is enabled automatically when at least one valid target exists

**Limit to explicitly marked classes** — `AnnotationOnly`:

```kotlin
ksp {
    arg("kotlinloggingextensions.mode", "AnnotationOnly")
}
```

```kotlin
dependencies {
    compileOnly("io.github.doljae:kotlin-logging-extensions-annotations:3.0.0") // for @Log
}
```

```kotlin
import io.github.doljae.kotlinlogging.extensions.Log

@Log
class OrderProcessor {
    fun processOrder(id: String) {
        log.info { "Processing order: $id" }
    }
}
```

`@Log` also works in `PackageScan` mode, where a class gets a logger if **either** condition
matches. Mode values are case/`_`/`-` insensitive: `All` (default), `PackageScan`, `AnnotationOnly`.

> **Only the annotations artifact belongs on the compile classpath.** The processor is wired in
> through `ksp(...)` and never needs to be visible to your compiler — putting it in `compileOnly`
> narrows which Kotlin versions can build against it.

## ✨ Features

- **🔧 Zero Boilerplate**: No logger declarations, no annotations, no configuration — just use `log.info { }`
- **🎚️ Scope It When You Want To**: Narrow generation to specific packages (`PackageScan`) or to explicitly marked classes (`AnnotationOnly`)
- **⚡ Compile-time Generation**: Uses KSP for compile-time safety with zero runtime overhead
- **📦 Package-aware Naming**: Logger names automatically match fully qualified class names
- **🏗️ kotlin-logging Integration**: Works seamlessly with the standard kotlin-logging library
- **🎯 Works Everywhere**: Compatible with any package depth and class structure

## 📋 Version Compatibility

### Requirements

| | |
|---|---|
| **JDK** | 17 or later |
| **Kotlin** | 2.0+ |
| **KSP** | matching your Kotlin version (KSP1 or KSP2) |
| **kotlin-logging** | 5.0.0+ |

**From `3.0.0` the library version no longer mirrors your Kotlin version.** Releases follow plain
SemVer, so the number describes the size of the change in *this library* — a major bump means
breaking changes here, not a new Kotlin line. Since
[KSP 2.3.0](https://github.com/google/ksp/releases/tag/2.3.0) KSP2 is a standalone tool built on the
stable compiler APIs rather than a compiler plugin, so one build of this processor serves a range of
Kotlin versions and there is nothing left for the version string to track.

The range below is measured, not assumed — each row is a standalone consumer project built against
the published `3.0.0` artifacts and run, asserting the generated `log` resolves and is named after
its class ([#152](https://github.com/doljae/kotlin-logging-extensions/issues/152)):

| Kotlin | KSP | Gradle |
|---|---|---|
| `2.0.21` | `2.0.21-1.0.28` | 8.8 |
| `2.1.21` | `2.1.21-2.0.2` | 8.11.1 |
| `2.2.21` | `2.2.21-2.0.4` | 8.14.3 |
| `2.3.21` | `2.3.10` | 9.0.0 |
| `2.4.10` | `2.3.11` | 9.7.0 |

Kotlin `2.0` is the floor because that is the metadata version the annotations artifact is compiled
against, not because anything below it was found to break.

Versions before `3.0.0` did pin one release per Kotlin version. That table is kept below for anyone
still on them.

<details>
<summary>Compatibility table for 2.x and earlier</summary>

| Library        | Kotlin   | KSP            |
|----------------|----------|----------------|
| `2.4.0` | `2.4.10` | `2.3.10` |
| `2.3.2` | `2.3.21` | `2.3.10` |
| `2.3.1` | `2.3.21` | `2.3.8` |
| `2.3.0`        | `2.3.0+` | `2.3.4+`       |
| `2.2.21-0.0.6` | `2.2.21` | `2.2.21-2.0.4` |
| `2.2.21-0.0.5` | `2.2.21` | `2.2.21-2.0.4` |
| `2.2.20-0.0.5` | `2.2.20` | `2.2.20-2.0.4` |
| `2.2.20-0.0.4` | `2.2.20` | `2.2.20-2.0.4` |
| `2.2.20-0.0.3` | `2.2.20` | `2.2.20-2.0.2` |
| `2.2.10-0.0.3` | `2.2.10` | `2.2.0-2.0.2`  |
| `2.2.0-0.0.3`  | `2.2.0`  | `2.2.0-2.0.2`  |
| `2.1.21-0.0.3` | `2.1.21` | `2.1.21-2.0.2` |
| `2.2.0-0.0.2`  | `2.2.0`  | `2.2.0-2.0.2`  |
| `2.1.21-0.0.1` | `2.1.21` | `2.1.21-2.0.2` |

</details>

### Upgrading to 3.0.0

`3.0.0` is a breaking release. Three things changed:

1. **`@AutoLog` is now `@Log`.** `@AutoLog` still works and still generates, but is deprecated and
   will be removed in a later major release. It is a real annotation rather than a `typealias` on
   purpose — KSP does not expand aliases when matching annotations, so an alias would silently
   generate nothing.
2. **The annotation moved to its own artifact.** Replace
   `compileOnly("io.github.doljae:kotlin-logging-extensions:…")` with
   `compileOnly("io.github.doljae:kotlin-logging-extensions-annotations:3.0.0")`. The `ksp(...)`
   coordinate is unchanged. If you are on the default `All` mode you use no annotation at all and can
   drop the `compileOnly` line entirely.
3. **Generation is project-wide by default.** Every eligible class gets a `log` extension without any
   annotation. Use `kotlinloggingextensions.mode` to narrow it — see
   [Scoping Generation](#scoping-generation).

`3.0.0` also starts warning about one option it has not yet removed:

4. **`kotlinloggingextensions.autoGeneratePackagePrefixes` is deprecated.** It still works. Replace it
   with `kotlinloggingextensions.targets`, appending `.*` to each entry
   (`com.example` → `com.example.*`); the build log prints the exact replacement value. It is removed
   in the next major release, and removal is worth acting on early: with no targets configured, mode
   resolution falls through to `All`, so a build that silently loses this option starts generating a
   logger for *every* class instead of failing.

```kotlin
plugins {
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:3.0.0")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4") // 5.0.0+

    // Only needed for AnnotationOnly / PackageScan modes, where you write @Log yourself.
    compileOnly("io.github.doljae:kotlin-logging-extensions-annotations:3.0.0")
}
```

**kotlin-logging compatibility**: This library requires kotlin-logging 5.0.0+ due to package name changes. Versions 5.x+
use `io.github.oshai.kotlinlogging` package, while older versions used `mu` package.

**Logger implementation**: If you're already using kotlin-logging in your project, no additional setup needed. For new
projects, add a logger implementation like [Logback](https://logback.qos.ch/)
or [Log4j2](https://logging.apache.org/log4j/2.x/).

## 📦 Installation

### Maven Central (Recommended)

```kotlin
plugins {
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.github.doljae:kotlin-logging-extensions:3.0.0")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
    implementation("ch.qos.logback:logback-classic:1.6.2")

    // Only needed if you write @Log yourself (AnnotationOnly / PackageScan modes).
    compileOnly("io.github.doljae:kotlin-logging-extensions-annotations:3.0.0")
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

**Solution**: Automatic logger generation that "just works" - inspired by Lombok's elegance, built with Kotlin's KSP
power.

## 🛠️ Troubleshooting & IDE Support

### IDE Symbols are Red
Since KSP generates code during compilation, your IDE might not immediately "see" the `log` property. To fix this:

1. **Build the project**: Run `./gradlew build` or `./gradlew kspKotlin`.
2. **Configure Source Sets**: Add the KSP generated directory to your Kotlin source sets in `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
```

3. **IntelliJ Integration**: If using IntelliJ IDEA, apply the `idea` plugin to mark these as generated source roots:

```kotlin
plugins {
    id("idea")
}

idea {
    module {
        val mainGenerated = file("build/generated/ksp/main/kotlin")
        val testGenerated = file("build/generated/ksp/test/kotlin")
        
        sourceDirs = sourceDirs + mainGenerated
        testSources.from(testGenerated)
        generatedSourceDirs = generatedSourceDirs + mainGenerated + testGenerated
    }
}
```

4. **Reload Gradle**: Click the "Reload All Gradle Projects" button in the Gradle tool window.

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

## 🤖 AI Agent Guide

Project instructions for Claude Code and Codex are in `CLAUDE.md` / `AGENTS.md` (identical).
Full context: [`docs/project-instructions.md`](docs/project-instructions.md).

## 🤝 Contributing

1. Fork and create a feature branch
2. Make your changes with tests
3. Follow [Conventional Commits](https://www.conventionalcommits.org/)
4. Open a Pull Request

## 📄 License

Apache License 2.0 - see [LICENSE](LICENSE) file.

---

⭐ **If this helps you, please star the repo!** ⭐
