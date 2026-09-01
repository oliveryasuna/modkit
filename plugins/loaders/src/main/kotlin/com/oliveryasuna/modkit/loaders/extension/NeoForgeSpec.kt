package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

/** NeoForge-specific versions udner `modkit { loaders { neoforge { } } }`. */
public abstract class NeoForgeSpec {

    public abstract val version: Property<String>

}
