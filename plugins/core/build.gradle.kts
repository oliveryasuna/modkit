import org.gradle.plugin.compatibility.compatibility

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
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    api(projects.libraries.common)
    api(projects.libraries.coreApi)

    implementation(projects.libraries.pluginSupport)

    testImplementation(projects.libraries.testUtil)
}
