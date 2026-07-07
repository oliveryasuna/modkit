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
    api(project(":libraries:common"))
    api(project(":libraries:core-api"))
}
