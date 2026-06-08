plugins {
    java
    // Mojang-mapped NMS access, compile-checked against the target Minecraft version.
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    // `./gradlew runServer` spins up a real Paper server with the plugin loaded.
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "io.github.anitect"
version = "0.1.0-SNAPSHOT"
description = "Reroll a professional villager's trades in place, gated by cooldown and/or cost."

java {
    // MC 26.1+ requires Java 25. foojay (see settings.gradle.kts) provisions it if absent.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    // Brings the full Paper API + Mojang-mapped server internals for the target version.
    paperweight.paperDevBundle("26.1.2.build.+")
}

tasks {
    compileJava {
        options.release = 25
        options.encoding = "UTF-8"
    }

    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to project.version.toString())
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

// Paper 1.20.5+ runs on Mojang mappings at runtime, so ship a Mojang-mapped jar (no reobfuscation).
paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
