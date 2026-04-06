plugins {
    id("java-library")

    //loom (unobfuscated)
    id("net.fabricmc.fabric-loom") version BuildConfig.LOOM_VERSION

    //local build-source plugins
    id("build-base")
    id("build-publish")
}

//set "main" artifact name, we append the version string received via the "build-base" plugin later
base {
    archivesName.set("bbe-fabric")
}

//(main + api) source-sets
val mainSourceSet = sourceSets.named("main").get().apply {
    java.setSrcDirs(listOf("src/main/java", "src/api/java"))
    resources.setSrcDirs(listOf("src/main/resources", "src/api/resources"))
}

//api source-set
val apiSourceSet = sourceSets.create("api").apply {
    java.setSrcDirs(listOf("src/api/java"))
    resources.setSrcDirs(listOf("src/api/resources"))

    compileClasspath += mainSourceSet.compileClasspath
    runtimeClasspath += output + compileClasspath
}

//helper function for including a fabric api module as compileOnly
fun DependencyHandlerScope.fabricModule(name: String) {
    compileOnly(fabricApi.module(name, BuildConfig.FABRIC_API_VERSION))
}

//declare maven repositories
repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.caffeinemc.net/releases")
    maven("https://maven.caffeinemc.net/snapshots")
    mavenCentral()
}

//specify version specific dependencies from declared maven repositories
dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")

    implementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    implementation("net.caffeinemc:sodium-fabric:${BuildConfig.SODIUM_VERSION}")

    fabricModule("fabric-block-getter-api-v2")
    fabricModule("fabric-renderer-api-v1")
}

//per artifact actions
tasks.withType<Jar>().configureEach {
    //set base-name and our built version string
    archiveBaseName.set(base.archivesName)
    archiveVersion.set(project.version.toString())

    //include the license file into the artifact
    val licenseFile = rootProject.file("LICENSE")
    from(licenseFile)
}

tasks {
    //these become :
    // "rootDir/build/mod"
    // "rootDir/build/api"
    val modOutputDir = rootProject.layout.buildDirectory.dir("mod")
    val apiOutputDir = rootProject.layout.buildDirectory.dir("api")

    //mod jar
    jar {
        destinationDirectory.set(modOutputDir)
        from(mainSourceSet.output) //grab from combined api + main source-sets
    }

    //api jar
    register<Jar>("apiJar") {
        archiveClassifier.set("api")
        destinationDirectory.set(apiOutputDir)
        from(apiSourceSet.output)
    }

    //api-source jar
    register<Jar>("apiSourcesJar") {
        archiveClassifier.set("api-sources")
        destinationDirectory.set(apiOutputDir)
        from(apiSourceSet.allSource)
    }

    assemble {
        dependsOn(jar)
        dependsOn("apiJar")
        dependsOn("apiSourcesJar")
    }
}

artifacts {
    add("archives", tasks.named("jar"))
    add("archives", tasks.named("apiJar"))
    add("archives", tasks.named("apiSourcesJar"))
}

//maven publishing - maven setup/declaration happens in the "build-publish" plugin
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = base.archivesName.get()
            version = project.version.toString()

            from(components["java"])
        }

        create<MavenPublication>("mavenApi") {
            groupId = project.group.toString()
            artifactId = base.archivesName.get() + "-api"
            version = project.version.toString()

            artifact(tasks.named("apiJar")) {
                classifier = null
            }

            artifact(tasks.named("apiSourcesJar")) {
                classifier = "sources"
            }
        }
    }
}