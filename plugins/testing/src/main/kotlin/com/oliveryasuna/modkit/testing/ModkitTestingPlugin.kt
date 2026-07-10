package com.oliveryasuna.modkit.testing

import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.testing.extension.TestingSpec
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Sets up testing for a mod:
 *
 *  - **Pure-logic JUnit** — configures the JUnit Platform on the `test` task
 *    and adds JUnit 5 dependencies, so unit tests over your own logic run with
 *    no Minecraft classpath. Unconditional once a `java`/`java-base` plugin is
 *    present.
 *  - **GameTest** — when `testing.gametest` is `true`, wires the loader's
 *    GameTest run: NeoForge's `gameTestServer` run (composes with the `run`
 *    plugin, which also targets the run named `gametest`); Fabric has no
 *    GameTest run helper, so it warns and skips.
 *
 * This plugin never applies a loader base — it reacts to whichever one the
 * loaders plugin applied via `withPlugin`.
 */
public class ModkitTestingPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val modkit = project.applyModkitCore()
        val testing = modkit.registerBlock("testing", TestingSpec::class.java)

        testing.gametest.convention(false)

        // Pure-logic JUnit — no loader required. React to the `java` plugin
        // (not `java-base`): only `java` creates the `test` task and the
        // `testImplementation` configuration, and reacting to `java-base` fires
        // mid-`JavaPlugin.apply()` before those exist.
        project.pluginManager.withPlugin("java") {
            configureJUnitPlatform(project)
        }

        // GameTest. The `net.neoforged.moddev` hook fires from inside the
        // loaders plugin's afterEvaluate — i.e., after the
        // `modkit { testing { } }` DSL has run — so reading `gametest` directly
        // here sees the user's value.
        project.pluginManager.withPlugin("net.neoforged.moddev") {
            if(testing.gametest.getOrElse(false)) {
                configureNeoForgeGameTest(project)
            }
        }

        // Loom is applied eagerly at `plugins { }` time, before the DSL runs,
        // so the read must be deferred to afterEvaluate (same reason run's
        // Fabric path defers). Fabric has no GameTest run — warn and skip.
        project.pluginManager.withPlugin("fabric-loom") {
            project.afterEvaluate {
                if(testing.gametest.getOrElse(false)) {
                    project.logger.warn("modkit.testing: Fabric Loom has no GameTest run helper; `testing.gametest` is ignored on Fabric. Use NeoForge for GameTest runs.")
                }
            }
        }
    }

    private fun configureJUnitPlatform(project: Project) {
        project.dependencies.apply {
            add("testImplementation", platform("org.junit:junit-bom:$JUNIT_BOM_VERSION"))
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        }
        project.tasks.withType(Test::class.java).configureEach { test ->
            test.useJUnitPlatform()
        }
    }

    private fun configureNeoForgeGameTest(project: Project) {
        val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)
        // Same run name the `run` plugin uses, so applying both configures one
        // run. `gameTestServer` has no MDG helper — select it by type string.
        val run = neoForge.runs.maybeCreate("gametest")
        run.type.set("gameTestServer")
        run.ideName.set("Minecraft Game Test")
    }

    private companion object {
        // Matches the JUnit version Modkit itself builds against.
        const val JUNIT_BOM_VERSION = "6.1.1"
    }
}
