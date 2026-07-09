package com.oliveryasuna.modkit.ci.extension

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class CiSpec {

    public abstract val provider: Property<String>
    public abstract val matrixFromTargets: Property<Boolean>
    public abstract val java: Property<Int>
    public abstract val cache: Property<Boolean>
    public abstract val publishOnTag: Property<Boolean>

    @get:Nested
    public abstract val secrets: SecretsSpec

    public fun secrets(action: Action<in SecretsSpec>) {
        action.execute(secrets)
    }

}
