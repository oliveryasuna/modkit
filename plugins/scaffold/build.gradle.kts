plugins {
    // Text-only generator: writes settings/build scripts and example sources,
    // never touching Loom/MDG/Stonecutter tooling → stays on the repo-wide
    // Java 17 target.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("scaffold") {
            id = "com.oliveryasuna.modkit.scaffold"
            implementationClass = "com.oliveryasuna.modkit.scaffold.ModkitScaffoldPlugin"
            displayName = "Modkit Scaffold Plugin"
            description = "Generates a working Modkit consumer project (settings, build script, example sources) for a chosen loader/version matrix."
            tags.set(listOf("modkit", "minecraft", "scaffold", "init"))
        }
    }
}

dependencies {
    implementation(project(":libraries:core-api"))
    implementation(project(":libraries:common"))
}

// The composite-build acceptance test injects `includeBuild(<modkit root>)`
// into the generated settings so the unpublished Modkit plugins resolve. Hand
// the repository root to the functional-test JVM.
tasks.named<Test>("functionalTest") {
    systemProperty("modkit.repoRoot", rootDir.absolutePath)
}
