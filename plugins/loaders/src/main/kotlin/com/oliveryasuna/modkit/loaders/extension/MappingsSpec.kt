package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

/**
 * Mappings settings for the build: the base namespace and optional
 * Parchment.
 */
public abstract class MappingsSpec {

    /** Base mapping namespace; defaults to [MappingsScheme.MOJMAP] */
    public abstract val scheme: Property<MappingsScheme>

    /**
     * Parchment version.
     *
     * Parchment is layered on only when this is set.
     */
    public abstract val parchment: Property<String>

}
