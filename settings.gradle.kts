rootProject.name = "bbe"

pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.fabricmc.net/") }
        gradlePluginPortal()
    }
}

include("fabric")