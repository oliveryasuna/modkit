plugins {
    id("modkit.library-conventions")
}

dependencies {
    compileOnly(gradleApi())

    api(projects.libraries.coreApi)
}
