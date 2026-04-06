import org.gradle.api.Project

import BuildConfig.MINECRAFT_VERSION
import BuildConfig.MOD_VERSION

object BuildTools {
    //build version string {version} to append to base "artifact name" {archivesName}
    fun createVersionString(project: Project): String {
        val isReleaseBuild = project.hasProperty("build.release")
        val buildId = System.getenv("GITHUB_RUN_NUMBER")

        return buildString {
            if (isReleaseBuild) {
                append(MOD_VERSION)
            } else {
                append(MOD_VERSION.substringBefore('-'))
                append("-SNAPSHOT")
            }

            append("+mc")
            append(MINECRAFT_VERSION)

            if (!isReleaseBuild) {
                append(
                    when {
                        !buildId.isNullOrBlank() -> "-build.$buildId"
                        else -> "-local"
                    }
                )
            }
        }
    }
}