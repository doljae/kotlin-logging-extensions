plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "io.github.doljae"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/doljae/kotlin-logging-extensions")
        credentials {
            username = "NEED_TO_FILL"
            password = "NEED_TO_FILL"
        }
    }
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content {
            includeGroup("io.github.doljae")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    ksp("io.github.doljae:kotlin-logging-extensions:0.0.1-SNAPSHOT") {
        isChanging = true
    }
    implementation("io.github.doljae:kotlin-logging-extensions:0.0.1-SNAPSHOT") {
        isChanging = true
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
