import org.gradle.plugin.compatibility.compatibility

plugins {
    // Bundles Stonecutter, whose published variants are compiled for Java 21,
    // so a Java 17 module cannot link them (variant matching rejects it). This
    // convention raises the bytecode target to 21 — same Java-21 floor as the
    // loader-wrapping modules, here driven by Stonecutter rather than Loom/MDG.
    id("modkit.loader-plugin-conventions")
}

gradlePlugin {
    plugins {
        // Project-side plugin: applied per generated node in build.gradle.kts.
        create("multiversion") {
            id = "com.oliveryasuna.modkit.multiversion"
            implementationClass = "com.oliveryasuna.modkit.multiversion.ModkitMultiversionPlugin"
            displayName = "Modkit Multiversion Plugin"
            description = "Per-version overrides over Stonecutter's source preprocessor, driven by the Modkit model."
            tags.set(listOf("modkit", "minecraft", "stonecutter", "multiversion"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
        // Settings-side plugin: applied in settings.gradle.kts to expand the
        // version x loader matrix into per-node subprojects via Stonecutter.
        create("multiversion-settings") {
            id = "com.oliveryasuna.modkit.multiversion.settings"
            implementationClass = "com.oliveryasuna.modkit.multiversion.ModkitMultiversionSettingsPlugin"
            displayName = "Modkit Multiversion Settings Plugin"
            description = "Expands the Modkit version x loader matrix into per-node Stonecutter subprojects (settings-time)."
            tags.set(listOf("modkit", "minecraft", "stonecutter", "multiversion"))
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

    // Wrapped multi-version tooling. Bundled so the settings plugin can apply
    // `dev.kikugie.stonecutter` by id and call its settings/project extensions,
    // without leaking Stonecutter types into Modkit's public API.
    implementation(libs.stonecutter)
}
