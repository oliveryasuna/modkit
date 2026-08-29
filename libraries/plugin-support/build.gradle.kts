plugins {
    id("modkit.library-conventions")
}

dependencies {
    compileOnly(gradleApi())

    api(project(":libraries:core-api"))
}
