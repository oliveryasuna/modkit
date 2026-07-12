package com.oliveryasuna.modkit.testing

import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.testing.extension.TestingSpec
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
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
 *    server-side GameTest run: NeoForge's `gameTestServer` run, and Fabric's
 *    `gametest` run via Loom's `fabricApi.configureTests`. Both compose with
 *    the `run` plugin, which also targets the run named `gametest`. Client
 *    game tests (Fabric-only) are out of scope for this single-boolean toggle.
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
        // Fabric path defers).
        project.pluginManager.withPlugin("fabric-loom") {
            project.afterEvaluate {
                if(testing.gametest.getOrElse(false)) {
                    configureFabricGameTest(project)
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

    private fun configureFabricGameTest(project: Project) {
        val fabricApi = project.extensions.getByType(FabricApiExtension::class.java)
        // Loom's `configureTests` wires Minecraft's GameTest framework. We
        // enable only the server game tests (the `gametest` run, gathered into
        // `build`) to mirror NeoForge's server-side `gameTestServer` run and
        // keep `testing.gametest` a single boolean. Client game tests are left
        // off: they launch a real client and require accepting the Minecraft
        // EULA (Loom's `acceptGameTestEula`), which modkit will not do
        // implicitly. Leaving `createSourceSet` at its default keeps the tests
        // in the main source set (main `fabric.mod.json` entrypoint), so no
        // extra wiring is needed. Verified against fabric-loom 1.17.13: the
        // EULA is gated on the client run only, not the server `gametest` run.
        fabricApi.configureTests { settings ->
            settings.enableGameTests.set(true)
            settings.enableClientGameTests.set(false)
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
