plugins {
    // Loader-wrapping module → Java 21 bytecode target (Loom/MDG floor).
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        create("run") {
            id = "com.oliveryasuna.modkit.run"
            implementationClass = "com.oliveryasuna.modkit.run.ModkitRunPlugin"
            displayName = "Modkit Run Plugin"
            description = "Unifies Fabric Loom and ModDevGradle run configurations behind a single Modkit DSL."
            tags.set(listOf("modkit", "minecraft", "run"))
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))

    // Wrapped loader tooling. Bundled so this plugin can react to whichever
    // base is applied and configure its run container via the typed extension;
    // kept internal (no upstream types in run's public API). This plugin never
    // applies a base — it only configures the one the loaders plugin applied.
    implementation(libs.fabric.loom)
    implementation(libs.moddev.gradle)
}
