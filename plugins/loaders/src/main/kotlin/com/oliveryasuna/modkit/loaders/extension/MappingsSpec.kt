package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

public abstract class MappingsSpec {

    public abstract val scheme: Property<String>  // "mojmap+parchment" | "yarn" | "mojmap"
    public abstract val parchment: Property<String>

}
