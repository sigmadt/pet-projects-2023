plugins {
    java
    application
}

group = "ru.sigmadt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.beust:jcommander:1.82")
    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.google.guava:guava:19.0")
}


application {
    mainClass.set("ru.itmo.sd.game.Application")
}

tasks.register<Jar>("uberJar") {
    dependsOn(configurations.runtimeClasspath)

    archiveClassifier.set("uber")
    manifest.attributes["Main-Class"] = application.mainClass.get()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    from(
        configurations
            .runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map(::zipTree)
    )
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
