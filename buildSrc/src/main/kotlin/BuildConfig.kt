import org.gradle.api.Project

object BuildConfig {
    //loom version for unobfuscated
    const val LOOM_VERSION: String = "1.15-SNAPSHOT"

    //fabric loader and api version
    const val FABRIC_LOADER_VERSION: String = "0.19.1"
    const val FABRIC_API_VERSION: String = "0.148.2+26.1.2"

    //minecraft version
    const val MINECRAFT_VERSION: String = "26.1.2"

    //sodium version (needs to vary between snapshot builds and releases)
    //because of the different artifact naming schemes
    const val SODIUM_VERSION: String = "0.9.0-beta.1+mc26.1.2"

    //iris version
    const val IRIS_VERSION: String = "1.10.9+26.1-fabric"

    //BBE mod version (remember to bump!!!)
    const val MOD_VERSION: String = "1.3.5"
}