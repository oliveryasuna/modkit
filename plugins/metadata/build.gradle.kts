plugins {
    // Pure model/serialization module → stays on the repo-wide Java 17 target.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("metadata") {
            id = "com.oliveryasuna.modkit.metadata"
            implementationClass = "com.oliveryasuna.modkit.metadata.ModkitMetadataPlugin"
            displayName = "Modkit Metadata Plugin"
            description = "Generates fabric.mod.json and neoforge.mods.toml from the Modkit model."
            tags.set(listOf("modkit", "minecraft"))
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))
    implementation(project(":libraries:common"))

    runtimeOnly(project(":plugins:core"))

    // Manifest serialization. Bundled so the ordered, deterministic manifest
    // can be built and written without leaking NightConfig types into the
    // public API.
    implementation(libs.nightconfig.json)
    implementation(libs.nightconfig.toml)
}
