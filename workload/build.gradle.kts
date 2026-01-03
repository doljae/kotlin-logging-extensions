plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    application
}

group = project.property("project.group") as String
version = project.property("project.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-assertions-core:6.0.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Kotlin logging dependencies
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.14")
    implementation("ch.qos.logback:logback-classic:1.5.23")

    // kotlin-logging-extensions (using project dependency for development)
    ksp(project(":processor"))
    implementation(project(":processor"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("examples.MainKt")
}
