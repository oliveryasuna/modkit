import org.gradle.plugin.compatibility.compatibility

plugins {
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        create("loaders") {
            id = "com.oliveryasuna.modkit.loaders"
            implementationClass = "com.oliveryasuna.modkit.loaders.ModkitLoadersPlugin"
            displayName = "Modkit Loaders Plugin"
            description = "Unifies Fabric Loom and ModDevGradle behind the Modkit model."
            tags.set(listOf("modkit", "minecraft", "fabric", "neoforge"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    implementation(projects.libraries.common)
    implementation(projects.libraries.pluginSupport)

    runtimeOnly(projects.plugins.core)

    // Wrapped loader tooling. Bundled so the active base can be applied by id
    // and configured via its typed extension; kept internal (no upstream types
    // in loaders' public API). Only the base for `modkit.loader` is applied.
    implementation(libs.fabricLoom)
    implementation(libs.moddevGradle)

    // AW->AT transpiler libraries (parse Fabric AW / model NeoForge AT).
    implementation(libs.accessWidener)
    implementation(libs.accesstransformers)
}
