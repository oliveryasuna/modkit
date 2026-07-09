plugins {
    // Loader-wrapping module → Java 21 bytecode target (Loom/MDG floor).
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        create("datagen") {
            id = "com.oliveryasuna.modkit.datagen"
            implementationClass = "com.oliveryasuna.modkit.datagen.ModkitDatagenPlugin"
            displayName = "Modkit Datagen Plugin"
            description = "Wires Minecraft data generation across Fabric Loom and ModDevGradle and routes generated output into resources."
            tags.set(listOf("modkit", "minecraft", "datagen"))
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))

    // Wrapped loader tooling. Bundled so this plugin can react to whichever
    // base is applied and configure its data generation via the typed
    // extension; kept internal (no upstream types in datagen's public API).
    // This plugin never applies a base — it only configures the one that the
    // loaders plugin applied.
    implementation(libs.fabric.loom)
    implementation(libs.moddev.gradle)
}
