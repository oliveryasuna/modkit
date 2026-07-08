plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("baseConventions") {
            id = "modkit.base-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.BaseConventionsPlugin"
        }
        create("pluginConventions") {
            id = "modkit.plugin-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.PluginConventionsPlugin"
        }
        create("libraryConventions") {
            id = "modkit.library-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.LibraryConventionsPlugin"
        }
        create("loaderPluginConventions") {
            id = "modkit.loader-plugin-conventions"
            implementationClass = "com.oliveryasuna.modkit.conventions.LoaderPluginConventionsPlugin"
        }
    }
}

dependencies {
    // Make these plugins available to apply from the convention plugin.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plugin.publish.plugin)
}
