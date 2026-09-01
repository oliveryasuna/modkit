package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

/** Fabric-specific versions under `modkit { loaders { fabric { } } }`. */
public abstract class FabricSpec {

    public abstract val loaderVersion: Property<String>

    public abstract val apiVersion: Property<String>

}
