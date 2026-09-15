import org.gradle.api.Project

object BuildConfig {
    //loom version for unobfuscated
    const val LOOM_VERSION: String = "1.16-SNAPSHOT"

    //neoforge gradle plugin
    const val MODDEV_GRADLE_VERSION: String = "2.0.141"

    //neoforge
    const val NEOFORGE_VERSION: String = "21.1.228"
    const val PARCHMENT_VERSION: String = "2024.11.17"

    //fabric loader and api version
    const val FABRIC_LOADER_VERSION: String = "0.19.2"
    const val FABRIC_API_VERSION: String = "0.116.12+1.21.1"

    //minecraft version
    const val MINECRAFT_VERSION: String = "1.21.1"
    const val YARN_MAPPINGS: String = "1.21.1+build.3"

    //sodium version (needs to vary between snapshot builds and releases)
    //because of the different artifact naming schemes
    const val SODIUM_VERSION: String = "0.8.12+mc1.21.1"

    //BBE mod version (remember to bump!!!)
    const val MOD_VERSION: String = "1.3.4"
}