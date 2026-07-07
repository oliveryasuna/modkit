package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property

public abstract class ModkitJvm {

    public abstract val toolchain: Property<Int>

}
