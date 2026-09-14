import org.gradle.api.Project

object BuildConfig {
    //loom version for unobfuscated
    const val LOOM_VERSION: String = "1.15-SNAPSHOT"

    //neoforge gradle plugin
    const val MODDEV_GRADLE_VERSION: String = "2.0.141"

    //neoforge
    const val NEOFORGE_VERSION: String = "26.2.0.0-beta"

    //fabric loader and api version
    const val FABRIC_LOADER_VERSION: String = "0.19.3"
    const val FABRIC_API_VERSION: String = "0.160.3+26.3"

    //minecraft version
    const val MINECRAFT_VERSION: String = "26.3-rc-1"

    //sodium version (needs to vary between snapshot builds and releases)
    //because of the different artifact naming schemes
    const val SODIUM_VERSION: String = "0.9.2-beta.2+mc26.3r1"

    //BBE mod version (remember to bump!!!)
    const val MOD_VERSION: String = "1.3.8-beta.1"
}