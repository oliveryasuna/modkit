package com.oliveryasuna.modkit.core.extension

import org.gradle.api.provider.Property

public abstract class ModkitUrls {

    public abstract val homepage: Property<String>
    public abstract val source: Property<String>
    public abstract val issues: Property<String>

}
