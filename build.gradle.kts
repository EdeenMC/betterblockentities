plugins {
    id( "net.fabricmc.fabric-loom" )
    `maven-publish`
}

version = providers.gradleProperty( "mod_version" ).get()
group   = providers.gradleProperty( "maven_group" ).get()

base {
    archivesName.set(providers.gradleProperty("archives_base_name").orElse("bbe").get())
}

repositories {
    maven ( "https://maven.fabricmc.net/" )

    maven ( "https://maven.caffeinemc.net/releases" )
    maven ( "https://maven.caffeinemc.net/snapshots" )

    mavenCentral()
}

dependencies {
    minecraft ( "com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}" )

    compileOnly ( "net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}" )
    compileOnly ( "net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}" )

    implementation ( "net.caffeinemc:sodium-fabric:${providers.gradleProperty("sodium_version").get()}" )
}

tasks.processResources {
    inputs.property ( "version", project.version )
    filesMatching ( "fabric.mod.json" ) {
        expand ( "version" to project.version,
            "id" to providers.gradleProperty("archives_base_name").orElse("bbe").get() )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

tasks.named<Jar>( "jar" ) {
    archiveBaseName.set( providers.gradleProperty( "archives_base_name" ).get() )
    from( "LICENSE" ) {
        rename { "${it}_${providers.gradleProperty( "archives_base_name" ).get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>( "mavenJava" ) {
            from( components["java"] )
        }
    }
}