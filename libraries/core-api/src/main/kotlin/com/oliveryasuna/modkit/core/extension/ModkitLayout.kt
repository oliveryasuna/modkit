package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property

public abstract class ModkitLayout {

    public abstract val commonSourceSet: Property<String>

}
