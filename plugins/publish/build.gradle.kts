import org.gradle.plugin.compatibility.compatibility

plugins {
    // Pure model/config module → stays on the repo-wide Java 17 target.
    id("modkit.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("publish") {
            id = "com.oliveryasuna.modkit.publish"
            implementationClass = "com.oliveryasuna.modkit.publish.ModkitPublishPlugin"
            displayName = "Modkit Publish Plugin"
            description = "Publishes Modkit artifacts to Modrinth, CurseForge, GitHub, and Discord from the Modkit model."
            tags.set(listOf("modkit", "minecraft", "publishing"))
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
    implementation(project(":libraries:common"))

    runtimeOnly(project(":plugins:core"))

    // Wrapped publishing tooling. Bundled so the aggregate publish task and its
    // per-destination config can be driven from the shared model; kept internal
    // (no upstream types in publish's public API).
    implementation(libs.mod.publish.plugin)
}
