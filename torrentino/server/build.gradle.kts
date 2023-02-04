plugins {
    application
}

configure<JavaApplication> {
    mainClass.set("ru.itmo.java.Application")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":torrent"))
    implementation("commons-io:commons-io:2.7")
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


application {
    mainClass.set("ru.itmo.java.Application")
}

tasks.register<Jar>("uberJar") {
    dependsOn(configurations.runtimeClasspath)

    manifest.attributes["Main-Class"] = application.mainClass.get()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveBaseName.set("tracker")
    archiveVersion.set("")

    from(sourceSets.main.get().output)
    from(
        configurations
            .runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map(::zipTree)
    )

    doLast {
        copy {
            from(archiveFile)
            into(File(project.rootDir, "out"))
        }
    }
}