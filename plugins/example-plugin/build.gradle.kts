plugins {
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("example") {
            id = "com.oliveryasuna.modkit.example"
            implementationClass = "com.oliveryasuna.modkit.example.ExamplePlugin"
            displayName = "Modkit Example Plugin"
            description = "A minimal example plugin demonstrating the modkit module layout."
            tags.set(listOf("modkit", "example"))
        }
    }
}
