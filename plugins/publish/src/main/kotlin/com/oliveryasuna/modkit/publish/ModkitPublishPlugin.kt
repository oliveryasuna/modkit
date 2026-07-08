package com.oliveryasuna.modkit.publish

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.plugin.activeLoader
import com.oliveryasuna.modkit.plugin.applyModkitCore
import com.oliveryasuna.modkit.plugin.registerBlock
import com.oliveryasuna.modkit.publish.extension.PublishSpec
import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar

public class ModkitPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // `publish` builds on the shared model — apply core first so `modkit`
        // exists, then bring in the upstream publishing plugin it wraps.
        val modkit = project.applyModkitCore()
        project.pluginManager.apply("me.modmuss50.mod-publish-plugin")

        val publish = modkit.registerBlock("publish", PublishSpec::class.java)

        publish.type.convention("stable")
        publish.dryRun.convention(false)
        publish.changelog.source.convention("git")

        // A destination is enabled by default only when the credentials needed
        // to reach it are present; resolved lazily so the DSL can still run.
        publish.modrinth.enabled.convention(bothPresent(publish.modrinth.projectId, publish.modrinth.token))
        publish.curseforge.enabled.convention(bothPresent(publish.curseforge.projectId, publish.curseforge.token))
        publish.github.enabled.convention(bothPresent(publish.github.repository, publish.github.token))
        publish.discord.enabled.convention(present(publish.discord.webhook))

        // The loader is chosen eagerly from `modkit.loader`; compatibility is
        // scoped to it. Absent -> empty compatibility -> publish is inert.
        val activeLoader = project.activeLoader()

        val minecraftVersions: Provider<List<String>> = project.provider {
            deriveCompat(modkit, activeLoader).minecraftVersions
        }
        // modLoaders is the active loader alone — derived directly (no eager
        // model read; targets are not populated at apply time).
        val modLoaders: List<String> =
            activeLoader?.let { listOf(it.name.lowercase()) } ?: emptyList()

        val mpp = project.extensions.getByType(ModPublishExtension::class.java)

        // Top-level publish options. Platforms inherit these by convention when
        // created, so they stay lazy even though destinations are wired later.
        mpp.dryRun.set(publish.dryRun)
        mpp.type.set(publish.type.map { ReleaseTypes.of(it) })
        mpp.version.set(modkit.version)
        mpp.modLoaders.set(modLoaders)
        mpp.changelog.set(Changelogs.provider(project, publish.changelog.source))

        wireArtifactFile(project, mpp)

        // Destinations depend on the finalized DSL (ids/tokens), so they are
        // created after evaluation once `enabled` can be resolved.
        project.afterEvaluate {
            configureDestinations(mpp, publish, minecraftVersions)
        }

        // User-facing entry point. Delegates to the upstream aggregate task;
        // network work, so never cached and never wired into `check`.
        project.tasks.register("modkitPublish") { task ->
            task.group = "publishing"
            task.description = "Publishes Modkit artifacts to the configured destinations."
            task.dependsOn("publishMods")
        }
    }

    private fun deriveCompat(modkit: ModkitExtension, activeLoader: McLoader?): PublishCompat.Compat =
        PublishCompat.derive(
            modkit.targets.map { target ->
                PublishCompat.TargetView(target.minecraftVersion, target.enabled.get(), target.loaders.get())
            },
            activeLoader
        )

    private fun wireArtifactFile(project: Project, mpp: ModPublishExtension) {
        // Standard `jar` output — the publishable artifact for NeoForge and any
        // plain-Java project.
        project.pluginManager.withPlugin("java-base") {
            val jar = project.tasks.named("jar", Jar::class.java)
            mpp.file.set(jar.flatMap { it.archiveFile })
        }

        // Fabric publishes the remapped jar; override the plain jar wiring.
        project.pluginManager.withPlugin("fabric-loom") {
            val remapJar = project.tasks.named("remapJar", Jar::class.java)
            mpp.file.set(remapJar.flatMap { it.archiveFile })
        }
    }

    private fun configureDestinations(
        mpp: ModPublishExtension,
        publish: PublishSpec,
        minecraftVersions: Provider<List<String>>
    ) {
        if(publish.modrinth.enabled.get()) {
            mpp.modrinth { modrinth ->
                modrinth.projectId.set(publish.modrinth.projectId)
                modrinth.accessToken.set(publish.modrinth.token)
                modrinth.minecraftVersions.set(minecraftVersions)
            }
        }

        if(publish.curseforge.enabled.get()) {
            mpp.curseforge { curseforge ->
                curseforge.projectId.set(publish.curseforge.projectId)
                curseforge.projectSlug.set(publish.curseforge.projectSlug)
                curseforge.accessToken.set(publish.curseforge.token)
                curseforge.minecraftVersions.set(minecraftVersions)
            }
        }

        if(publish.github.enabled.get()) {
            mpp.github { github ->
                github.repository.set(publish.github.repository)
                github.accessToken.set(publish.github.token)
            }
        }

        if(publish.discord.enabled.get()) {
            mpp.discord { discord ->
                discord.webhookUrl.set(publish.discord.webhook)
                discord.dryRun.set(publish.dryRun)
                discord.setPlatformsAllFrom(discord.project)
            }
        }
    }

    private companion object {

        private fun present(value: Provider<String>): Provider<Boolean> =
            value.map { it.isNotBlank() }.orElse(false)

        private fun bothPresent(a: Provider<String>, b: Provider<String>): Provider<Boolean> =
            present(a).zip(present(b)) { x, y -> x && y }

    }

}
