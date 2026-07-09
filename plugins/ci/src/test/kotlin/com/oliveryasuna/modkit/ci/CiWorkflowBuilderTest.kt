package com.oliveryasuna.modkit.ci

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CiWorkflowBuilderTest {

    private fun inputs(
        matrix: List<CiMatrixEntry> = listOf(
            CiMatrixEntry("1.21.1", "fabric"),
            CiMatrixEntry("1.21.1", "neoforge")
        ),
        java: Int = 21,
        cache: Boolean = true,
        publishOnTag: Boolean = true,
        publishApplied: Boolean = false,
        modrinthSecret: String = "MODRINTH_TOKEN",
        curseforgeSecret: String = "CURSEFORGE_TOKEN",
        githubSecret: String = "GITHUB_TOKEN"
    ): CiWorkflowInputs =
        CiWorkflowInputs(
            matrix = matrix,
            java = java,
            cache = cache,
            publishOnTag = publishOnTag,
            publishApplied = publishApplied,
            modrinthSecret = modrinthSecret,
            curseforgeSecret = curseforgeSecret,
            githubSecret = githubSecret
        )

    @Test
    fun `emits a build job with a matrix row per version and loader`() {
        val yaml = CiWorkflowBuilder.build(inputs())

        assertTrue(yaml.contains("loader: \"fabric\""), yaml)
        assertTrue(yaml.contains("loader: \"neoforge\""), yaml)
        assertTrue(yaml.contains("minecraft: \"1.21.1\""), yaml)
        assertTrue(yaml.contains("- run: ./gradlew build -Pmodkit.loader=\${{ matrix.loader }}"), yaml)
    }

    @Test
    fun `sorts matrix entries by version then loader`() {
        val yaml = CiWorkflowBuilder.build(
            inputs(
                matrix = listOf(
                    CiMatrixEntry("1.21.4", "neoforge"),
                    CiMatrixEntry("1.21.1", "neoforge"),
                    CiMatrixEntry("1.21.1", "fabric")
                )
            )
        )

        val fabric = yaml.indexOf("- minecraft: \"1.21.1\"\n            loader: \"fabric\"")
        val neo1211 = yaml.indexOf("- minecraft: \"1.21.1\"\n            loader: \"neoforge\"")
        val neo1214 = yaml.indexOf("- minecraft: \"1.21.4\"\n            loader: \"neoforge\"")

        assertTrue(fabric in 0 until neo1211, yaml)
        assertTrue(neo1211 in 0 until neo1214, yaml)
    }

    @Test
    fun `interpolates the java version quoted`() {
        val yaml = CiWorkflowBuilder.build(inputs(java = 17))

        assertTrue(yaml.contains("java-version: \"17\""), yaml)
    }

    @Test
    fun `always sets the CI env on the build job`() {
        val yaml = CiWorkflowBuilder.build(inputs())

        assertTrue(yaml.contains("      CI: \"true\""), yaml)
    }

    @Test
    fun `omits the setup-gradle step when cache is false`() {
        val yaml = CiWorkflowBuilder.build(inputs(cache = false))

        assertFalse(yaml.contains("gradle/actions/setup-gradle@v6"), yaml)
    }

    @Test
    fun `includes the setup-gradle step when cache is true`() {
        val yaml = CiWorkflowBuilder.build(inputs(cache = true))

        assertTrue(yaml.contains("- uses: gradle/actions/setup-gradle@v6"), yaml)
    }

    @Test
    fun `omits the publish job unless publishOnTag and publish is applied`() {
        assertFalse(CiWorkflowBuilder.build(inputs(publishOnTag = true, publishApplied = false)).contains("publish:"))
        assertFalse(CiWorkflowBuilder.build(inputs(publishOnTag = false, publishApplied = true)).contains("publish:"))
    }

    @Test
    fun `includes the publish job with secret names when enabled and applied`() {
        val yaml = CiWorkflowBuilder.build(
            inputs(
                publishOnTag = true,
                publishApplied = true,
                modrinthSecret = "MR",
                curseforgeSecret = "CF",
                githubSecret = "GH"
            )
        )

        assertTrue(yaml.contains("  publish:"), yaml)
        assertTrue(yaml.contains("    if: startsWith(github.ref, 'refs/tags/v')"), yaml)
        assertTrue(yaml.contains("      - run: ./gradlew modkitPublish"), yaml)
        assertTrue(yaml.contains("          MR: \${{ secrets.MR }}"), yaml)
        assertTrue(yaml.contains("          CF: \${{ secrets.CF }}"), yaml)
        assertTrue(yaml.contains("          GH: \${{ secrets.GH }}"), yaml)
    }

    @Test
    fun `is deterministic for identical input`() {
        val first = CiWorkflowBuilder.build(inputs(publishApplied = true))
        val second = CiWorkflowBuilder.build(inputs(publishApplied = true))

        assertEquals(first, second)
    }

    @Test
    fun `is deterministic regardless of matrix input order`() {
        val ordered = CiWorkflowBuilder.build(
            inputs(matrix = listOf(CiMatrixEntry("1.21.1", "fabric"), CiMatrixEntry("1.21.1", "neoforge")))
        )
        val shuffled = CiWorkflowBuilder.build(
            inputs(matrix = listOf(CiMatrixEntry("1.21.1", "neoforge"), CiMatrixEntry("1.21.1", "fabric")))
        )

        assertEquals(ordered, shuffled)
    }

}
