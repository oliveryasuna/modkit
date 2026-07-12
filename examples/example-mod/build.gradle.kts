import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.dependencies.nest

plugins {
    // Already on the classpath via the settings plugin (same artifact) — no version.
    id("com.oliveryasuna.modkit.multiversion")
    id("com.oliveryasuna.modkit.loaders") version "0.2.0"
    id("com.oliveryasuna.modkit.metadata") version "0.2.0"
    id("com.oliveryasuna.modkit.mixins") version "0.2.0"
    id("com.oliveryasuna.modkit.dependencies") version "0.2.0"
    id("com.oliveryasuna.modkit.datagen") version "0.2.0"
    id("com.oliveryasuna.modkit.run") version "0.2.0"
    id("com.oliveryasuna.modkit.testing") version "0.2.0"
}

// The node supplies the Minecraft version and loader; derive both here.
val mc: String = stonecutter.current.version
val loaderName: String = stonecutter.current.project.substringAfterLast('-')
val loader: McLoader = if(loaderName == "fabric") McLoader.FABRIC else McLoader.NEOFORGE

modkit {
    modId.set("modkit_example")
    group.set("com.oliveryasuna.modkit.example")
    version.set("1.0.0")
    displayName.set("Modkit Example")
    description.set("A multi-version, multi-loader example mod built with Modkit.")
    license.set("MIT")

    minecraft(mc) {
        loaders.add(loader)
    }

    loaders {
        fabric {
            loaderVersion.set("0.16.14")
            apiVersion.set(if(mc == "1.21.1") "0.116.13+1.21.1" else "0.100.8+1.20.6")
        }
        neoforge {
            version.set(if(mc == "1.21.1") "21.1.235" else "20.6.139")
        }
    }

    metadata {
        environment.set("*")
        // Fabric-only; NeoForge discovers its @Mod class by annotation.
        entrypoints {
            main("com.oliveryasuna.modkit.example.fabric.ExampleModFabric")
        }
    }

    // Register the hand-authored src/main/resources/modkit_example.mixins.json.
    // The mixins plugin publishes its file name to the shared manifest registry,
    // and metadata folds it into fabric.mod.json "mixins" / neoforge.mods.toml.
    mixins {
        register("modkit_example") {
            pkg.set("com.oliveryasuna.modkit.example.mixin")
        }
    }

    // Fabric-only entrypoint FQCN; auto-published to fabric.mod.json
    // entrypoints.fabric-datagen. NeoForge discovers its @GatherDataEvent
    // handler via the mod bus, so no entry is needed there.
    datagen {
        entrypoint.set("com.oliveryasuna.modkit.example.datagen.ExampleDataGenerator")
    }
}

// Exercise the dependencies plugin's routing (Loom `include` / MDG `jarJar`)
// with a trivial jar-in-jar. Kept off by default to keep `build` hermetic;
// enable with -Pmodkit.example.nestDemo to actually resolve/nest it.
if(providers.gradleProperty("modkit.example.nestDemo").isPresent) {
    dependencies {
        nest("com.google.code.gson:gson:2.11.0")
    }
}
