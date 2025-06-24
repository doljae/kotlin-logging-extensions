plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = project.property("project.group") as String
version = project.property("project.version") as String

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

    ksp("${project.property("project.group")}:${project.property("project.artifactId")}:${project.property("project.version")}") {
        isChanging = true
    }
    implementation("${project.property("project.group")}:${project.property("project.artifactId")}:${project.property("project.version")}") {
        isChanging = true
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
