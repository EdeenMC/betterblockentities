import org.gradle.api.Project

object BuildConfig {
    //loom version for unobfuscated
    const val LOOM_VERSION: String = "1.14-SNAPSHOT"

    //fabric loader and api version
    const val FABRIC_LOADER_VERSION: String = "0.18.2"
    const val FABRIC_API_VERSION: String = "0.140.2+1.21.11"

    //minecraft version
    const val MINECRAFT_VERSION: String = "1.21.11"

    //sodium version (needs to vary between snapshot builds and releases)
    //because of the different artifact naming schemes
    const val SODIUM_VERSION: String = "0.8.7+mc1.21.11"

    //BBE mod version (remember to bump!!!)
    const val MOD_VERSION: String = "1.3.2"
}