import org.gradle.plugin.compatibility.compatibility

plugins {
    // Pure workflow-generation module → stays on the repo-wide Java 17 target.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("ci") {
            id = "com.oliveryasuna.modkit.ci"
            implementationClass = "com.oliveryasuna.modkit.ci.ModkitCiPlugin"
            displayName = "Modkit CI Plugin"
            description = "Generates GitHub Actions workflows from the Modkit version x loader matrix."
            tags.set(listOf("modkit", "minecraft", "ci"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))
}
