package com.oliveryasuna.modkit.core.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class ModkitExtension {

    public abstract val modId: Property<String>
    public abstract val group: Property<String>
    public abstract val version: Property<String>
    public abstract val displayName: Property<String>
    public abstract val description: Property<String>
    public abstract val license: Property<String>
    public abstract val authors: ListProperty<String>

    @get:Nested
    public abstract val urls: ModkitUrls

    @get:Nested
    public abstract val jvm: ModkitJvm

    public abstract val targets: NamedDomainObjectContainer<McTarget>

    public fun urls(action: Action<in ModkitUrls>) {
        action.execute(urls)
    }

    public fun jvm(action: Action<in ModkitJvm>) {
        action.execute(jvm)
    }

    public fun targets(action: Action<in NamedDomainObjectContainer<McTarget>>) {
        action.execute(targets)
    }

    public fun minecraft(
        version: String,
        action: Action<in McTarget> = Action {}
    ) {
        targets.create(version, action)
    }

}
