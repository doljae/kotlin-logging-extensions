import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

description = "kotlin-logging-extensions"

repositories {
    mavenCentral()
}

configure<KtlintExtension> {
    version.set("1.8.0")
}
