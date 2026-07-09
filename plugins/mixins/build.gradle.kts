plugins {
    // Register-focused model module (no loader tooling) → stays on the
    // repo-wide Java 17 target.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("mixins") {
            id = "com.oliveryasuna.modkit.mixins"
            implementationClass = "com.oliveryasuna.modkit.mixins.ModkitMixinsPlugin"
            displayName = "Modkit Mixins Plugin"
            description = "Registers mixin configs, provides the refmap convention, and publishes config file names to the shared manifest registry."
            tags.set(listOf("modkit", "minecraft", "mixins"))
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))

    // Bytecode scanning for the `@Mixin` lint. Bundled so ASM types never leak
    // into the public API.
    implementation(libs.asm)
}
