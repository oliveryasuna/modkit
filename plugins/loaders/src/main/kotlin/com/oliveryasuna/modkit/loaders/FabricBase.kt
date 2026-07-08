package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.McLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension
import com.oliveryasuna.modkit.loaders.extension.LoadersSpec
import com.oliveryasuna.modkit.loaders.extension.MappingsScheme
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Applies and configures Fabric Loom for a FABRIC target, mapping the Modkit
 * model onto Loom's DSL. Kept internal — no Loom types leak into loaders'
 * public API.
 */
internal fun configureFabric(
    project: Project,
    modkit: ModkitExtension,
    loaders: LoadersSpec
) {
    project.pluginManager.apply("fabric-loom")
    project.addParchmentRepository()
    val loom = project.extensions.getByType(LoomGradleExtensionAPI::class.java)

    // Active Minecraft version: the single enabled target declaring FABRIC.
    // Resolved lazily — the model DSL is populated after this plugin applies.
    val minecraftVersion = project.provider {
        val fabric = modkit.targets.filter { target ->
            target.enabled.get() && McLoader.FABRIC in target.loaders.get()
        }
        require(fabric.isNotEmpty()) {
            "modkit.loader=fabric but no enabled target declares the fabric loader."
        }
        require(fabric.size == 1) {
            "loaders builds one variant per project, but ${fabric.size} enabled targets declare " +
            "fabric (${fabric.joinToString { it.minecraftVersion }}). Use multiversion or declare one."
        }
        fabric.single().minecraftVersion
    }

    // Mappings dependency, built lazily from the scheme + optional parchment.
    // Parchment is layered onto mojmap when a version is present.
    val mappings = project.provider {
        when(loaders.mappings.scheme.get()) {
            MappingsScheme.MOJMAP -> {
                val parchment = loaders.mappings.parchment.orNull
                if(parchment == null) {
                    loom.officialMojangMappings()
                } else {
                    loom.layered {
                        it.officialMojangMappings()
                        // Parchment data is published as a zip (no POM) -> the
                        // @zip extension is required for Loom to resolve it.
                        it.parchment("org.parchmentmc.data:parchment-${minecraftVersion.get()}:$parchment@zip")
                    }
                }
            }

            MappingsScheme.YARN -> throw GradleException("Yarn mappings are not yet wired for the Fabric base (no curated yarn version source). Use scheme = MOJMAP.")
        }
    }

    // Access widener — Loom takes a single file. Resolved lazily so the DSL is
    // populated; null (no widener) leaves the property unset.
    loom.accessWidenerPath.fileProvider(
        project.provider {
            val files = loaders.accessWideners.files
            require(files.size <= 1) {
                "Fabric Loom supports a single access widener, but ${files.size} were provided."
            }
            files.firstOrNull()
        }
    )

    with(project.dependencies) {
        addProvider("minecraft", minecraftVersion.map { "com.mojang:minecraft:$it" })
        addProvider("mappings", mappings)
        addProvider("modImplementation", loaders.fabric.loaderVersion.map { "net.fabricmc:fabric-loader:$it" })
        // fabric-api is optional — an absent apiVersion adds nothing.
        addProvider("modImplementation", loaders.fabric.apiVersion.map { "net.fabricmc.fabric-api:fabric-api:$it" })
    }
}
