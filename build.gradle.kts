plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

val archivesName = project.extra["archives_base_name"]!!
version = project.extra["mod_version"]!!
group = project.extra["maven_group"]!!

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
    implementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")
    // Meteor
    //implementation("meteordevelopment:meteor-client:${project.extra["minecraft_version"]}-SNAPSHOT")
    implementation("meteordevelopment:meteor-client:26.1.2-SNAPSHOT")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version"),
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 25
    }
}
