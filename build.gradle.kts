plugins {
    id("net.neoforged.moddev") version "2.0.107"
}

val minecraftVersion: String = "1.21.1"
val neoForgeVersion: String = "21.1.222"
val parchmentVersion: String = "2024.11.17"
val parchmentMinecraftVersion: String = "1.21.1"
val jeiVersion: String = "19.25.0.321"
val immersiveAircraftVersion: String = "1.4.6+1.21.1+neoforge"

val modId: String = "liquidfuels"
val modName: String = "Liquid Fuels"
val modVersion: String = "0.1.1"
val modGroupId: String = "com.g1739.liquidfuels"
val modAuthors: String = "交错次元"
val modCredits: String = "美术: witzig_heidi(鱼)"
val modLicense: String = "All Rights Reserved"
val modDescription: String = "Configuration-driven universal fluid fuel converter."

val localImmersiveAircraftJar = file("../local-deps/immersive_aircraft-1.4.6+1.21.1-neoforge.jar")
val useLocalImmersiveAircraft = localImmersiveAircraftJar.isFile

group = modGroupId
version = modVersion

base {
    archivesName.set("LiquidFuels-NeoForge-$minecraftVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.blamejared.com/")
    maven("https://maven.conczin.net/Artifacts")
}

neoForge {
    version = neoForgeVersion

    parchment {
        minecraftVersion.set(parchmentMinecraftVersion)
        mappingsVersion.set(parchmentVersion)
    }

    runs {
        configureEach {
            jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-ea")
        }
        register("client") {
            client()
            gameDirectory = file("run/client")
        }
        register("server") {
            server()
            gameDirectory = file("run/server")
            programArgument("--nogui")
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    compileOnly("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
    runtimeOnly("mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")

    if (useLocalImmersiveAircraft) {
        compileOnly(files(localImmersiveAircraftJar))
        runtimeOnly(files(localImmersiveAircraftJar))
    } else {
        compileOnly("net.conczin:immersive_aircraft:$immersiveAircraftVersion")
        runtimeOnly("net.conczin:immersive_aircraft:$immersiveAircraftVersion")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val replaceProperties = mapOf(
        "minecraft_version" to minecraftVersion,
        "neoforge_version" to neoForgeVersion,
        "immersive_aircraft_version" to immersiveAircraftVersion,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_credits" to modCredits,
        "mod_description" to modDescription
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Specification-Title" to modId,
            "Specification-Vendor" to modAuthors,
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to modAuthors,
            "MixinConfigs" to "$modId.mixins.json",
            "Bundle-License" to "All Rights Reserved"
        )
    }
}
