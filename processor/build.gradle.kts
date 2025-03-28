import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = "io.github.doljae"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.20-1.0.32")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.doljae", "kotlin-logging-extensions", "0.0.1-SNAPSHOT")

    pom {
        name.set("kotlin-logging-extensions")
        description.set("Kotlin Logging Extensions")
        inceptionYear.set("2025")
        url.set("https://github.com/doljae/kotlin-logging-extensions")
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
            url.set("https://github.com/doljae/kotlin-logging-extensions/")
            connection.set("scm:git:git://github.com/doljae/kotlin-logging-extensions.git")
            developerConnection.set("scm:git:ssh://git@github.com/doljae/kotlin-logging-extensions.git")
        }
    }
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/other/#github-packages-example
publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/doljae/kotlin-logging-extensions")
            credentials(PasswordCredentials::class)
        }
    }
}
