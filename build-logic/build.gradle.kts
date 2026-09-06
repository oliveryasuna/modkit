plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugin.kotlinGradle)
    implementation(libs.plugin.publishPlugin)
    implementation(libs.plugin.vanniktechMavenPublish)
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
