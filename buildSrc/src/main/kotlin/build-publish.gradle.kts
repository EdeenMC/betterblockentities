plugins {
    id("maven-publish")
}

publishing {
    repositories {
        val isReleaseBuild = project.hasProperty("build.release")
        val edeenMCMavenUsername: String? by project
        val edeenMCMavenPassword: String? by project

        maven {
            name = "EdeenMC"
            url = uri("https://maven.edeenmc.net".let {
                if (isReleaseBuild) "$it/releases" else "$it/snapshots"
            })

            credentials {
                username = edeenMCMavenUsername
                password = edeenMCMavenPassword
            }
        }
    }
}