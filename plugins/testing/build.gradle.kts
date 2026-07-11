import org.gradle.plugin.compatibility.compatibility

plugins {
    // Configures the NeoForge `gameTestServer` run via ModDevGradle's typed
    // extension, so it bundles MDG and inherits its Java 21 bytecode floor.
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        create("testing") {
            id = "com.oliveryasuna.modkit.testing"
            implementationClass = "com.oliveryasuna.modkit.testing.ModkitTestingPlugin"
            displayName = "Modkit Testing Plugin"
            description = "Sets up JUnit Platform for pure-logic tests and wires the NeoForge GameTest run."
            tags.set(listOf("modkit", "minecraft", "testing", "junit", "gametest"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

// The NeoForge GameTest functional test composes with the real `loaders` plugin
// (so MDG applies late, as in production) by `includeBuild`-ing this repo. Pass
// its root so the test can inject that include into its fixture.
tasks.named<Test>("functionalTest") {
    systemProperty("modkit.repoRoot", rootDir.absolutePath)
}

dependencies {
    implementation(project(":libraries:plugin-support"))

    runtimeOnly(project(":plugins:core"))

    // Wrapped loader tooling. Bundled so the plugin can select the NeoForge
    // `gameTestServer` run type via MDG's typed extension; kept internal (no
    // MDG types in testing's public API). Never applies a base — only
    // configures the one the loaders plugin applied.
    implementation(libs.moddev.gradle)
}
