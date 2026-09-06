import org.gradle.plugin.compatibility.compatibility

plugins {
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        create("metadata") {
            id = "com.oliveryasuna.modkit.metadata"
            implementationClass = "com.oliveryasuna.modkit.metadata.ModkitMetadataPlugin"
            displayName = "Modkit Metadata Plugin"
            description = "Generates fabric.mod.json and neoforge.mods.toml from the Modkit model."
            tags.set(listOf("modkit", "minecraft"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))
    implementation(project(":libraries:common"))

    runtimeOnly(project(":plugins:core"))

    implementation(libs.kotlinSemver)

    implementation(libs.bundles.nightconfig.all)
}
