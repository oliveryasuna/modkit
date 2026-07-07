package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

public abstract class McTarget @Inject constructor(
    public val name: String
) {

    public val minecraftVersion: String
        get() = name

    public abstract val loaders: SetProperty<String>  // "fabric", "neoforge", "forge", "quilt"
    public abstract val enabled: Property<Boolean>

}
