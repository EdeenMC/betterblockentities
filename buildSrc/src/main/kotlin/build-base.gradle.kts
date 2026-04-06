plugins {
    id("java-library")
    id("idea")
}

//set build group and build version string
group = "net.edeenmc"
version = BuildTools.createVersionString(project)

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

//replace version placeholder inside "fabric.mod.json" with our built version string
tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version.toString()
            )
        )
    }
}