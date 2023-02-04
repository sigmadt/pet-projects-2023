buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    }
}


rootProject.name = "java-2-2022"

include("common", "client", "server", "torrent")
