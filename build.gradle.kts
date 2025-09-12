import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.2.20" apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

description = "kotlin-logging-extensions"

repositories {
    mavenCentral()
}

configure<KtlintExtension> {
    version.set("1.7.1")
}
