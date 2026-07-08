package com.oliveryasuna.modkit.metadata.extension

import org.gradle.api.provider.ListProperty

public abstract class EntrypointsSpec {

    public abstract val main: ListProperty<String>
    public abstract val client: ListProperty<String>

    public fun main(vararg fqcn: String) {
        main.addAll(*fqcn)
    }

    public fun client(vararg fqcn: String) {
        client.addAll(*fqcn)
    }

}
