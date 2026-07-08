package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.provider.Property

public abstract class GitHubSpec {

    public abstract val repository: Property<String>

    public abstract val token: Property<String>

    public abstract val draft: Property<Boolean>

    public abstract val enabled: Property<Boolean>

}
