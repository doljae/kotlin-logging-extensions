import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = project.property("project.group") as String
version = project.property("project.version") as String

repositories {
    mavenCentral()
}

dependencies {
    // compileOnly, so it is not published. See the `withDependencies` block below.
    compileOnly(kotlin("stdlib"))
}

// This artifact publishes ZERO dependencies on purpose.
//
// The Kotlin plugin adds kotlin-stdlib to `implementation`, which lands in the POM and the Gradle
// Module Metadata as a compile-scope dependency at whatever version built this project. A consumer
// on an older Kotlin then hits Gradle's "highest version wins" conflict resolution, gets our newer
// stdlib on its *compile* classpath, and fails with "Module was compiled with an incompatible
// version of Kotlin. The binary version of its metadata is 2.4.0, expected version is 2.0.0" —
// measured on a Kotlin 2.0.21 consumer, see issue #152.
//
// Dropping it is safe: @Log and @AutoLog are SOURCE-retention annotation classes with no members,
// so nothing of ours needs the stdlib at runtime, and any Kotlin consumer already has one.
configurations.named("implementation").configure {
    withDependencies {
        removeIf { it.group == "org.jetbrains.kotlin" && it.name == "kotlin-stdlib" }
    }
}

kotlin {
    // Deliberately lower than the processor's. This artifact is the only one that lands on a
    // consumer's *compile* classpath, so its floor decides which Kotlin versions can use the library
    // at all — see issue #152.
    jvmToolchain(17)

    compilerOptions {
        // The metadata version written here gates which compilers can read the artifact. It is a
        // separate axis from jvmTarget: a low JVM target does not help an older compiler parse
        // newer Kotlin metadata.
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

// Publishing config is duplicated from :processor rather than extracted. Sharing it needs either
// buildSrc or an `apply(from=)` script that loses the type-safe `mavenPublishing` accessor, and this
// block changes about once a year. Revisit if a third publishable module appears.
mavenPublishing {
    publishToMavenCentral(automaticRelease = false)

    if (System.getenv("GITHUB_TOKEN") == null) {
        signAllPublications()
    }

    coordinates(
        groupId = group.toString(),
        artifactId = project.property("project.annotations.artifactId") as String,
        version = version.toString(),
    )

    pom {
        name.set(project.property("project.annotations.artifactId") as String)
        description.set("Annotations for Kotlin Logging Extensions")
        inceptionYear.set("2025")
        url.set(project.property("project.url") as String)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("doljae")
                name.set("Seokjae Lee")
                url.set("https://github.com/doljae/")
            }
        }
        scm {
            url.set(project.property("project.scm.url") as String)
            connection.set(project.property("project.scm.connection") as String)
            developerConnection.set(project.property("project.scm.developerConnection") as String)
        }
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/doljae/${project.property("project.artifactId")}")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: project.findProperty("githubPackagesUsername") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("githubPackagesPassword") as String?
            }
        }
    }
}
