plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = project.property("project.group") as String
version = project.property("project.version") as String

repositories {
    mavenCentral()
    
    // Sonatype Central (primary) - for public releases
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content {
            includeGroup("io.github.doljae")
        }
    }
    
    // GitHub Packages (optional) - requires authentication
    // Configure using environment variables or gradle.properties:
    // - GITHUB_USERNAME / GITHUB_TOKEN (environment variables)
    // - githubPackagesUsername / githubPackagesPassword (gradle.properties)
    val githubUsername = System.getenv("GITHUB_USERNAME") 
        ?: project.findProperty("githubPackagesUsername") as String?
    val githubToken = System.getenv("GITHUB_TOKEN") 
        ?: project.findProperty("githubPackagesPassword") as String?
        
    if (githubUsername != null && githubToken != null) {
        maven {
            url = uri("https://maven.pkg.github.com/doljae/kotlin-logging-extensions")
            credentials {
                username = githubUsername
                password = githubToken
            }
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin logging dependencies
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // kotlin-logging-extensions
    val extensionsCoordinates = "${project.property("project.group")}:${project.property("project.artifactId")}:${project.property("project.version")}"
    ksp(extensionsCoordinates)
    implementation(extensionsCoordinates)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
