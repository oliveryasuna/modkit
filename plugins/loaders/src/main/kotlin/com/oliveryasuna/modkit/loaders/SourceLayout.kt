package com.oliveryasuna.modkit.loaders

import com.oliveryasuna.modkit.plugin.ModkitProperties
import com.oliveryasuna.modkit.plugin.commonSourceSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

internal class SourceLayout private constructor(
    val commonSourceSet: String,
    val isSplitClient: Boolean
) {

    /** `true` when the common source set is the default `main`. */
    val isCommonMain: Boolean
        get() = commonSourceSet == ModkitProperties.DEFAULT_COMMON_SOURCE_SET

    /**
     * Resolves the common source set.
     *
     * Fails clearly if the named one is missing. `loaders` binds the mod to an
     * existing set; it never invents one.
     */
    fun requireCommonSourceSet(project: Project): SourceSet =
        project.sourceSets.findByName(commonSourceSet)
            ?: throw GradleException(
                "modkit.commonSourceSet = '${commonSourceSet}' but not such source set exists. " +
                        "Create it in your build script (e.g., sourceSets.create(\"${commonSourceSet}\")) or unset " +
                        "the property."
            )

    companion object {

        private const val SPLIT_CLIENT_PROPERTY: String = "modkit.splitClient"

        /**
         * Reads the layout off the project and rejects the one unsupported
         * combo.
         *
         * Loom's `splitEnvironmentSourceSets()` splits `main` specifically, so
         * a non-`main` common set plus split-client would leave the two
         * disagreeing about which set is "common". Fail up front instead of
         * building something broken.
         *
         * Read with [Project.findProperty] (like the other structural
         * properties) so a per-node extra property from the multiversion plugin
         * is picked up too, not just `-P` / `gradle.properties`.
         */
        fun read(project: Project): SourceLayout {
            val commonSourceSet = project.commonSourceSet()
            val splitClient = project.findProperty(SPLIT_CLIENT_PROPERTY)?.toString()?.toBoolean() ?: false

            if(commonSourceSet != ModkitProperties.DEFAULT_COMMON_SOURCE_SET && splitClient) {
                throw GradleException(
                    "modkit.commonSourceSet = '$commonSourceSet' cannot be combined with modkit.splitClient " +
                            "(split-client is anchored to the 'main' source set). Use one or the other.",
                )
            }

            return SourceLayout(commonSourceSet, splitClient)
        }
    }

}
