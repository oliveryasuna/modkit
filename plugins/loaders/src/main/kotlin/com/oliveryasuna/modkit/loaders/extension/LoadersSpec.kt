package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Nested

public abstract class LoadersSpec {

    @get:Nested
    public abstract val fabric: FabricSpec

    @get:Nested
    public abstract val neoforge: NeoForgeSpec

    @get:Nested
    public abstract val mappings: MappingsSpec

    public abstract val accessWideners: ConfigurableFileCollection

    public fun fabric(action: Action<in FabricSpec>) {
        action.execute(fabric)
    }

    public fun neoforge(action: Action<in NeoForgeSpec>) {
        action.execute(neoforge)
    }

    public fun mappings(action: Action<in MappingsSpec>) {
        action.execute(mappings)
    }

}
