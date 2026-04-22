import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

description = "kotlin-logging-extensions"

repositories {
    mavenCentral()
}

configure<KtlintExtension> {
    version.set("1.8.0")
}
