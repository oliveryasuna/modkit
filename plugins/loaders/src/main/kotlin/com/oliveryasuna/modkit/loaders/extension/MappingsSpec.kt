package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

public abstract class MappingsSpec {

    /** Base mapping namespace. Defaults to [MappingsScheme.MOJMAP]. */
    public abstract val scheme: Property<MappingsScheme>

    /** Parchment version (e.g. "2024.11.17"); parchment is layered when set. */
    public abstract val parchment: Property<String>

}
