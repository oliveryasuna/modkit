package com.oliveryasuna.modkit.test

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File

public abstract class FunctionalTestBase {

    @TempDir
    private lateinit var projectDir: File

    protected fun settings() {
        projectDir.resolve("settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
    }

    protected fun build(body: String) {
        projectDir.resolve("build.gradle.kts").writeText(body)
    }

    protected fun runner(vararg args: String): GradleRunner {
        writeCoverageProperties()

        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

        // Launched directly from the IDE, not by the Gradle `functionalTest`
        // task. Run the build in-process so the IDE's coverage agent can see
        // it.
        if(System.getProperty("modkit.jacocoAgentArg") == null) {
            runner.withDebug(true)
        }

        return runner
    }

    protected fun resolve(relative: String): File {
        return projectDir.resolve(relative)
    }

    /**
     * The plugin code we test runs in the forked TestKit daemon, not in this
     * JVM, so that is where the JaCoCo agent needs to be. When coverage is on,
     * `plugin-conventions` puts the agent's `-javaagent` argument in the
     * `modkit.jacocoAgentArg` system property. We copy it into the build's
     * `org.gradle.jvmargs` so the daemon starts with the agent attached.
     *
     * We also set `org.gradle.daemon=false`. That way each build gets a fresh
     * daemon that shuts down when the build ends, and JaCoCo flushes its data
     * on shutdown. Without it the daemon could still be alive, and its exec
     * file incomplete, when the report task runs.
     *
     * If the property is not set, coverage is off and this does nothing.
     */
    private fun writeCoverageProperties() {
        val agentArg = System.getProperty("modkit.jacocoAgentArg")?.takeIf { it.isNotBlank() } ?: return

        // `-javaagent` paths may contain backslashes on Windows; escape them for
        // the .properties format.
        val escaped = agentArg.replace("\\", "\\\\")
        projectDir.resolve("gradle.properties").writeText(
            """
            org.gradle.jvmargs=$escaped
            org.gradle.daemon=false
            """.trimIndent()
        )
    }

}
