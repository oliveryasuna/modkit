package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.Property

public abstract class ValidationSpec {

    public abstract val failOnMissingIcon: Property<Boolean>
    public abstract val failOnInvalidSemver: Property<Boolean>
    public abstract val failOnUndeclaredMixinConfig: Property<Boolean>

}
