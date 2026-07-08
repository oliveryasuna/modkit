package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.provider.Property

public abstract class DiscordSpec {

    public abstract val webhook: Property<String>

    public abstract val enabled: Property<Boolean>

}
