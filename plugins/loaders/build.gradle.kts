plugins {
    // Loader-wrapping module → Java 21 bytecode target (Loom/MDG floor).
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
        }
    }
}

dependencies {
    implementation(project(":libraries:common"))
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))

    // Wrapped loader tooling. Bundled so the active base can be applied by id
    // and configured via its typed extension; kept internal (no upstream types
    // in loaders' public API). Only the base for `modkit.loader` is applied.
    implementation(libs.fabric.loom)
    implementation(libs.moddev.gradle)

    // AW->AT transpiler libraries (parse Fabric AW / model NeoForge AT).
    implementation(libs.access.widener)
    implementation(libs.accesstransformers)
}
