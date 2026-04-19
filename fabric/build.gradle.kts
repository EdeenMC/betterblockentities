import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar
import org.gradle.api.file.DuplicatesStrategy

plugins {
    id("java-library")

    // loom
    id("fabric-loom") version BuildConfig.LOOM_VERSION

    // local build-source plugins
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
    mappings(loom.officialMojangMappings())

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")

    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")

    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    modImplementation("net.caffeinemc:sodium-fabric:${BuildConfig.SODIUM_VERSION}")

    fabricModule("fabric-renderer-api-v1")
}

tasks {
    //these become :
    // "rootDir/build/mod"
    // "rootDir/build/api"
    val modOutputDir = rootProject.layout.buildDirectory.dir("mod")
    val apiOutputDir = rootProject.layout.buildDirectory.dir("api")
    val licenseFile = rootProject.file("LICENSE")

    //dev jar
    named<Jar>("jar") {
        archiveBaseName.set(base.archivesName)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("dev")
        destinationDirectory.set(layout.buildDirectory.dir("devlibs"))

        from(mainSourceSet.output)
        from(licenseFile)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    //mod jar : remap still needed on versions below 26.1
    named<RemapJarTask>("remapJar") {
        archiveBaseName.set(base.archivesName)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        destinationDirectory.set(modOutputDir)

        dependsOn(named("jar"))
    }

    //api jar
    register<Jar>("apiJar") {
        archiveBaseName.set(base.archivesName)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("api")
        destinationDirectory.set(apiOutputDir)

        from(apiSourceSet.output)
        from(licenseFile)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    //api-source jar
    register<Jar>("apiSourcesJar") {
        archiveBaseName.set(base.archivesName)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("api-sources")
        destinationDirectory.set(apiOutputDir)

        from(apiSourceSet.allSource)
        from(licenseFile)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    assemble {
        dependsOn(named("remapJar"))
        dependsOn(named("apiJar"))
        dependsOn(named("apiSourcesJar"))
    }
}

artifacts {
    add("archives", tasks.named("remapJar"))
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

            artifact(tasks.named("remapJar"))
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