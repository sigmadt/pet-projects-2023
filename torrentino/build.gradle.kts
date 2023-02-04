import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.10"
    application
    id("com.google.protobuf") apply false
}

group = "org.itmo.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.google.protobuf:protobuf-java:3.19.0")
        implementation("org.jetbrains:annotations:23.0.0")
        implementation("com.beust:jcommander:1.82")
        testImplementation(platform("org.junit:junit-bom:5.8.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    sourceSets {
        main {
            java.setSrcDirs(listOf("src"))
            resources.setSrcDirs(listOf("resources"))
        }
        test {
            java.setSrcDirs(listOf("test"))
            resources.setSrcDirs(listOf("testResources"))
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    java.toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    val compileKotlin: KotlinCompile by tasks

    compileKotlin.kotlinOptions {
        freeCompilerArgs += "-Xuse-k2"
    }
}
