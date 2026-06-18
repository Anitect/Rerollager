plugins {
    java
    // Mojang-mapped NMS access, compile-checked against the target Minecraft version.
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    // `./gradlew runServer` spins up a real Paper server with the plugin loaded.
    id("xyz.jpenilla.run-paper") version "3.0.2"
    // Shade + relocate bStats into the plugin jar.
    id("com.gradleup.shadow") version "9.0.0"
}

group = "io.github.anitect"
version = "1.0.1"
description = "Reroll a professional villager's trades in place, gated by cooldown and/or cost."

java {
    // MC 26.1+ requires Java 25. foojay (see settings.gradle.kts) provisions it if absent.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    // Brings the full Paper API + Mojang-mapped server internals for the target version.
    paperweight.paperDevBundle("26.2.build.+")
    // Anonymous metrics; shaded + relocated into the jar (see shadowJar below).
    implementation("org.bstats:bstats-bukkit:3.1.0")
}

tasks {
    compileJava {
        options.release = 25
        options.encoding = "UTF-8"
    }

    // The default `jar` task writes the same Rerollager-<version>.jar as shadowJar
    // (classifier ""), so leaving it enabled lets an un-shaded jar race and clobber
    // the shaded one in build/libs. Disable it so only the shaded jar is ever produced.
    jar {
        enabled = false
    }

    shadowJar {
        // Replace the default jar so build/libs holds a single, shaded plugin jar.
        archiveClassifier.set("")
        relocate("org.bstats", "io.github.anitect.rerollager.bstats")
    }

    // Build and the test server should use the shaded jar.
    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xmx2G")
        pluginJars(shadowJar.flatMap { it.archiveFile })
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
