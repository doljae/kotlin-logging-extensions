plugins {
    kotlin("jvm") version "2.1.10"
    id("com.google.devtools.ksp") version "2.1.10-1.0.31"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":processor"))
    ksp(project(":processor"))
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
