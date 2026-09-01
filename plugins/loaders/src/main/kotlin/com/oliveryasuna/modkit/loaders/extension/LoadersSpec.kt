package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Nested

/**
 * The `modkit { loaders { } }` block: everything mloader-specific the model
 * itself stays out of.
 */
public abstract class LoadersSpec {

    @get:Nested
    public abstract val fabric: FabricSpec

    @get:Nested
    public abstract val neoforge: NeoForgeSpec

    @get:Nested
    public abstract val mappings: MappingsSpec

    /**
     * Access widener files. Fabric allows at most one; NeoForge merges them
     * into an AT.
     */
    public abstract val accessWideners: ConfigurableFileCollection

    public fun fabric(action: Action<in FabricSpec>): Unit = action.execute(fabric)

    public fun neoforge(action: Action<in NeoForgeSpec>): Unit = action.execute(neoforge)

    public fun mappings(action: Action<in MappingsSpec>): Unit = action.execute(mappings)

}
