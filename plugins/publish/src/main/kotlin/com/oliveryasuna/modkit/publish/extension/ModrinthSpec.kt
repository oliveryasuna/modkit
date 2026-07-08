package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.provider.Property

public abstract class ModrinthSpec {

    public abstract val projectId: Property<String>

    public abstract val token: Property<String>

    public abstract val enabled: Property<Boolean>

}
