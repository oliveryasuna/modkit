package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.core.extension.GameTarget
import com.oliveryasuna.modkit.core.extension.ModLoader
import com.oliveryasuna.modkit.core.extension.ModkitExtension

/**
 * Picks the one target a base actually builds.
 *
 * A single project builds a single variant: exactly one enabled target that
 * declares the active loader. Zero is a misconfiguration, and more than one
 * means the build should have gone through the multiversion plugin instead.
 * Both bases ask this the same way, so the rule lives here once.
 */
internal object ActiveTarget {

    fun resolve(
        model: ModkitExtension,
        loader: ModLoader
    ): GameTarget {
        val name = loader.name.lowercase()
        val matching = model.targets.filter { it.enabled.get() && loader in it.loaders.get() }

        require(matching.isNotEmpty()) { "modkit.laoder=${name} but no enabled target declares the $name loader." }
        require(matching.size == 1) {
            "loaders builds one variant per project, but ${matching.size} enabled targets declare $name " +
                    "(${matching.joinToString { it.minecraftVersion }}). Use multiversion, or declare just one."
        }

        return matching.single()
    }

}
