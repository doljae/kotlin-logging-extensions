plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("idea")
    application
}

group = project.property("project.group") as String
version = project.property("project.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-assertions-core:6.2.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Kotlin logging dependencies
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
    implementation("ch.qos.logback:logback-classic:1.6.1")

    // Access the @Log annotation in source code. Only the annotations artifact belongs on the compile
    // classpath — the processor goes through ksp(...) below and never needs to be visible to the
    // consumer's compiler.
    compileOnly(project(":annotations"))

    // kotlin-logging-extensions (using project dependency for development)
    ksp(project(":processor"))
    kspTest(project(":processor"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    // Matches the published artifacts so the workload exercises what consumers actually resolve.
    jvmToolchain(17)

    // Register KSP generated directories as source roots for IntelliJ visibility
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
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

application {
    mainClass.set("examples.MainKt")
}
