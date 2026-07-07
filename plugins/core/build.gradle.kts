plugins {
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("core") {
            id = "com.oliveryasuna.modkit.core"
            implementationClass = "com.oliveryasuna.modkit.core.ModkitCorePlugin"
            displayName = "Modkit Core Plugin"
            description = "Common functionality for all other Modkit plugins."
            tags.set(listOf("modkit", "core"))
        }
    }
}

dependencies {
    // Shared library. `api` so consumers of this plugin get it transitively;
    // the library publishes as its own Maven artifact.
    api(project(":libraries:common"))
}
