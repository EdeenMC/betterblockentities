rootProject.name = "betterblockentities"
pluginManagement {
	repositories {
		maven(
			"https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		gradlePluginPortal()
		mavenCentral()
	}
	plugins {
		id( "fabric-loom" ) version providers.gradleProperty( "loom_version" ).get()
	}
}