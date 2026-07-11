import org.gradle.plugin.compatibility.compatibility

plugins {
    // Facade-only: routes by configuration *name* (modImplementation/include/
    // jarJar), never touching Loom/MDG types → stays on the repo-wide Java 17
    // target, and does not bundle the loader tooling.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("dependencies") {
            id = "com.oliveryasuna.modkit.dependencies"
            implementationClass = "com.oliveryasuna.modkit.dependencies.ModkitDependenciesPlugin"
            displayName = "Modkit Dependencies Plugin"
            description = "Unifies mod-dependency declaration, jar-in-jar nesting, and mod repositories across Fabric Loom and ModDevGradle."
            tags.set(listOf("modkit", "minecraft", "dependencies"))
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
