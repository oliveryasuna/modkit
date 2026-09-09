import org.gradle.plugin.compatibility.compatibility

plugins {
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
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    implementation(projects.libraries.pluginSupport)

    runtimeOnly(projects.plugins.core)

    implementation(libs.fabricLoom)
    implementation(libs.moddevGradle)

    testImplementation(projects.libraries.testUtil)
}
