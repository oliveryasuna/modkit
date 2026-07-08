package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.provider.Property

public abstract class ChangelogSpec {

    public abstract val source: Property<String>

}
