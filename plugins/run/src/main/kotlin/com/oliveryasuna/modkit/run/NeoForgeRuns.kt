package com.oliveryasuna.modkit.run

import com.oliveryasuna.modkit.run.extension.RunConfig
import com.oliveryasuna.modkit.run.extension.RunSpec
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Project

/**
 * The unified run "kind", mapped onto a ModDevGradle run type. MDG exposes
 * `client()`/`server()`/`data()` helpers; the gametest server has no helper and
 * is selected by its type string directly.
 */
private enum class RunKind(val ideName: String) {

    CLIENT("Minecraft Client"),
    SERVER("Minecraft Server"),
    DATA("Minecraft Data"),
    GAMETEST("Minecraft Game Test");

    fun applyTo(model: RunModel) {
        when(this) {
            CLIENT -> model.client()
            SERVER -> model.server()
            DATA -> model.data()
            GAMETEST -> model.type.set("gameTestServer")
        }
    }

}

/**
 * Configures ModDevGradle runs from the unified model. Kept internal — no MDG
 * types leak into run's public API.
 *
 * MDG maps the unified config essentially one-to-one: it has native
 * system-property and environment-variable maps and a dev-login toggle. MDG is
 * applied by the loaders plugin inside its own `afterEvaluate`, so the
 * `net.neoforged.moddev` hook fires after the `modkit.run { }` DSL has run;
 * properties are wired lazily regardless.
 */
internal fun configureNeoForgeRuns(project: Project, run: RunSpec) {
    val neoForge = project.extensions.getByType(NeoForgeExtension::class.java)

    configureNeoForgeRun(project, neoForge, "client", run.client, RunKind.CLIENT)
    configureNeoForgeRun(project, neoForge, "server", run.server, RunKind.SERVER)
    configureNeoForgeRun(project, neoForge, "data", run.data, RunKind.DATA)
    configureNeoForgeRun(project, neoForge, "gametest", run.gametest, RunKind.GAMETEST)
}

private fun configureNeoForgeRun(
    project: Project,
    neoForge: NeoForgeExtension,
    name: String,
    config: RunConfig,
    kind: RunKind
) {
    if(!config.enabled.getOrElse(false)) return

    val model = neoForge.runs.maybeCreate(name)
    kind.applyTo(model)

    model.gameDirectory.set(config.gameDir.map { project.layout.projectDirectory.dir(it) })
    model.jvmArguments.addAll(config.jvmArgs)
    model.programArguments.addAll(config.programArgs)
    model.systemProperties.putAll(config.systemProperties)
    model.environment.putAll(config.environment)
    model.devLogin.set(config.auth)
    model.ideName.set(kind.ideName)
}
