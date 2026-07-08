package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.provider.Property

public abstract class CurseForgeSpec {

    public abstract val projectId: Property<String>

    public abstract val projectSlug: Property<String>

    public abstract val token: Property<String>

    public abstract val enabled: Property<Boolean>

}
