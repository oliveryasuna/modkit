package com.oliveryasuna.modkit.loaders.extension

import org.gradle.api.provider.Property

public abstract class FabricSpec {

    public abstract val loaderVersion: Property<String>
    public abstract val apiVersion: Property<String>

}
