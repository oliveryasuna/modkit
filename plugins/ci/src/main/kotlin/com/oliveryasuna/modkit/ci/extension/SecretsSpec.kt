package com.oliveryasuna.modkit.ci.extension

import org.gradle.api.provider.Property

public abstract class SecretsSpec {

    public abstract val modrinth: Property<String>
    public abstract val curseforge: Property<String>
    public abstract val github: Property<String>

}
