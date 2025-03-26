plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.31")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
