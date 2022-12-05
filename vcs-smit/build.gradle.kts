plugins {
    java
    application
}

group = "org.itmo.java"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("commons-codec:commons-codec:1.11") // утилиты для хеширования
    implementation("commons-io:commons-io:2.6") // утилиты для работы с IO
    implementation("commons-cli:commons-cli:1.4")
    implementation("com.beust:jcommander:1.82")
    implementation("org.json:json:20220320") // фреймворк для создания CLI
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

application {
    mainClass.set("ru.itmo.java.smit.Application")
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


tasks.test {
    useJUnitPlatform()
}
