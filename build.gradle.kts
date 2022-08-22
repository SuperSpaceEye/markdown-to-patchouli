plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
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
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

gradlePlugin {
    plugins {
        create("mtp") {
            id = "com.ssblur.mtp"
            implementationClass = "com.ssblur.mtp.MarkdownToPatchouliPlugin"
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = "com.ssblur.mtp"
            artifactId = "mtp"
            version = "1.0"
        }
    }
}

