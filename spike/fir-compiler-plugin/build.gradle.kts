plugins {
    kotlin("jvm") version "2.4.10"
}

repositories { mavenCentral() }

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.10")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
    jvmToolchain(17)
}
