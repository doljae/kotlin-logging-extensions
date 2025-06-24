import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
    id("org.jlleitschuh.gradle.ktlint")
}

group = project.property("project.group") as String
version = project.property("project.version") as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.21-2.0.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Only sign when publishing to Maven Central, not for GitHub Packages
    if (System.getenv("GITHUB_TOKEN") == null) {
        signAllPublications()
    }

    coordinates(
        groupId = group.toString(),
        artifactId = project.property("project.artifactId") as String,
        version = version.toString(),
    )

    pom {
        name.set(project.property("project.artifactId") as String)
        description.set(project.property("project.description") as String)
        inceptionYear.set("2025")
        url.set(project.property("project.url") as String)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("doljae")
                name.set("Seokjae Lee")
                url.set("https://github.com/doljae/")
            }
        }
        scm {
            url.set(project.property("project.scm.url") as String)
            connection.set(project.property("project.scm.connection") as String)
            developerConnection.set(project.property("project.scm.developerConnection") as String)
        }
    }
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/other/#github-packages-example
publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/doljae/${project.property("project.artifactId")}")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: project.findProperty("githubPackagesUsername") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("githubPackagesPassword") as String?
            }
        }
    }
}
