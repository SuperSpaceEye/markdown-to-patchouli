import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `java`
    `java-gradle-plugin`
    `maven-publish`
}

base {
    version = "1.0.0"
    group = "com.ssblur.mtp"
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}


tasks {
    shadowJar {
        archiveClassifier.set("")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

gradlePlugin {
    plugins {
        create("mtp") {
            id = "com.ssblur.mtp"
            implementationClass = "com.ssblur.mtp.MarkdownToPatchouliPlugin"
            displayName = "Markdown to Patchouli"
            description = "A Gradle plugin for generating Patchouli books from Markdown documentation"
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = "com.ssblur.mtp"
            artifactId = "mtp"
            version = "1.0.0"
        }
    }
}

