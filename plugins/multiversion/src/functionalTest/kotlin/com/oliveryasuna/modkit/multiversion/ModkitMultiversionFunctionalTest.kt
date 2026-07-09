package com.oliveryasuna.modkit.multiversion

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

/**
 * Functional (Gradle TestKit) test for the `multiversion` module.
 *
 * The fixture is a monorepo of two Stonecutter trees (`:mods:modA`,
 * `:mods:modB`) built through the `modkitVersions { }` settings DSL. To keep the
 * test fast, the central build script does **not** apply any loader (no Loom /
 * MDG, no Minecraft download): it only applies the `multiversion` project plugin
 * and prints diagnostics. This exercises node expansion, the loader bridge
 * (`beforeProject` → `modkit.loader` extra property), the `onVersion(range)`
 * matcher, and configuration-cache compatibility of the settings hook.
 *
 * ## Settings-plugin injection
 * `GradleRunner.withPluginClasspath()` injects the plugin-under-test classpath
 * for **project** plugins applied via `plugins { }`, but a **settings** plugin
 * applied via `plugins { id(...) }` in `settings.gradle.kts` is not resolvable
 * from that injected classpath. So the fixture applies the settings plugin the
 * classic way: it reads the plugin-under-test classpath from the
 * `plugin-under-test-metadata.properties` file that TestKit generates, drops
 * every entry onto a `buildscript { dependencies { classpath(files(...)) } }`
 * in `settings.gradle.kts`, and `apply(plugin = "...")`s the settings plugin by
 * class. The same classpath is handed to `GradleRunner.withPluginClasspath(...)`
 * so the `com.oliveryasuna.modkit.multiversion` **project** plugin also resolves.
 */
class ModkitMultiversionFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    // --- Plugin-under-test classpath (for the settings-plugin injection) ---

    private val pluginClasspath: List<File> by lazy {
        val metadataUrl = javaClass.classLoader.getResource("plugin-under-test-metadata.properties")
                          ?: error("plugin-under-test-metadata.properties not found on the test classpath")
        val props = Properties().apply { metadataUrl.openStream().use { load(it) } }
        val implClasspath = props.getProperty("implementation-classpath")
                            ?: error("implementation-classpath missing from plugin-under-test-metadata.properties")
        implClasspath.split(File.pathSeparator).filter { it.isNotBlank() }.map { File(it) }
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath(pluginClasspath)
            .withArguments(*args)

    // --- Fixture ---

    private fun write(path: String, content: String) {
        val file = projectDir.resolve(path)
        file.parentFile.mkdirs()
        file.writeText(content.trimIndent())
    }

    /** Builds the full two-tree monorepo fixture in [projectDir]. */
    private fun writeFixture() {
        // gradle.properties: enable config cache off by default so the explicit
        // --configuration-cache flag in test 4 controls it. Parallel is fine.
        write(
            "gradle.properties",
            """
            org.gradle.jvmargs=-Xmx2G
            org.gradle.configuration-cache=false
            """
        )

        // settings.gradle.kts: repositories + inject the settings plugin classpath
        // via buildscript, then apply it by class and declare the matrix.
        val classpathFiles = pluginClasspath.joinToString(",\n            ") {
            "files(${quote(it.absolutePath)})"
        }
        write(
            "settings.gradle.kts",
            """
            import com.oliveryasuna.modkit.core.extension.McLoader

            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
            }

            dependencyResolutionManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
            }

            buildscript {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
                dependencies {
                    classpath(
                        files(
                            $classpathFiles
                        )
                    )
                }
            }

            apply(plugin = "com.oliveryasuna.modkit.multiversion.settings")

            configure<com.oliveryasuna.modkit.multiversion.settings.ModkitVersionsSettings> {
                mod(":mods:modA") {
                    version("1.21.1", McLoader.FABRIC, McLoader.NEOFORGE)
                }
                mod(":mods:modB") {
                    version("1.20.1", McLoader.FABRIC)
                }
            }

            rootProject.name = "consumer"
            """
        )

        // Controller (stonecutter.gradle.kts) per mod. Mirrors the spike: a
        // stonecutter block that sets the active node.
        write(
            "mods/modA/stonecutter.gradle.kts",
            """
            plugins {
                id("dev.kikugie.stonecutter")
            }

            stonecutter active "1.21.1-fabric"
            """
        )
        write(
            "mods/modB/stonecutter.gradle.kts",
            """
            plugins {
                id("dev.kikugie.stonecutter")
            }

            stonecutter active "1.20.1-fabric"
            """
        )

        // Shared central build script. Lives at mods/<mod>/build.gradle.kts and is
        // applied to every node of that tree. NO loader applied: just the
        // multiversion project plugin + diagnostics.
        val central =
            """
            plugins {
                id("com.oliveryasuna.modkit.multiversion")
            }

            modkit {
                modId.set("mymod")
                multiversion {
                    onVersion(">=1.21") {
                        property("matched2111", "yes")
                    }
                }
            }

            tasks.register("printInfo") {
                val node = stonecutter.current.project
                val loader = findProperty("modkit.loader")?.toString()
                val matched = findProperty("matched2111")?.toString()
                doLast {
                    println("NODE=" + node + " LOADER=" + loader + " MATCHED=" + matched)
                }
            }
            """
        write("mods/modA/build.gradle.kts", central)
        write("mods/modB/build.gradle.kts", central)
    }

    private fun quote(s: String): String = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    /**
     * Single-mod-at-root layout (non-monorepo): `root { }` instead of
     * `mod(path) { }`. Nodes register directly under the root (`:<node>`); the
     * central script + controller live at the repository root.
     */
    private fun writeRootFixture() {
        write(
            "gradle.properties",
            """
            org.gradle.jvmargs=-Xmx2G
            org.gradle.configuration-cache=false
            """
        )

        val classpathFiles = pluginClasspath.joinToString(",\n            ") {
            "files(${quote(it.absolutePath)})"
        }
        write(
            "settings.gradle.kts",
            """
            import com.oliveryasuna.modkit.core.extension.McLoader

            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
            }

            dependencyResolutionManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
            }

            buildscript {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                    maven("https://maven.fabricmc.net/")
                    maven("https://maven.neoforged.net/releases/")
                    maven("https://maven.kikugie.dev/releases")
                    maven("https://maven.kikugie.dev/snapshots")
                }
                dependencies {
                    classpath(
                        files(
                            $classpathFiles
                        )
                    )
                }
            }

            apply(plugin = "com.oliveryasuna.modkit.multiversion.settings")

            configure<com.oliveryasuna.modkit.multiversion.settings.ModkitVersionsSettings> {
                root {
                    version("1.21.1", McLoader.FABRIC, McLoader.NEOFORGE)
                }
            }

            rootProject.name = "consumer"
            """
        )

        // Root controller + central script live at the repository root.
        write(
            "stonecutter.gradle.kts",
            """
            plugins {
                id("dev.kikugie.stonecutter")
            }

            stonecutter active "1.21.1-fabric"
            """
        )
        write(
            "build.gradle.kts",
            """
            plugins {
                id("com.oliveryasuna.modkit.multiversion")
            }

            modkit {
                modId.set("mymod")
            }

            tasks.register("printInfo") {
                val node = stonecutter.current.project
                val loader = findProperty("modkit.loader")?.toString()
                doLast {
                    println("NODE=" + node + " LOADER=" + loader)
                }
            }
            """
        )
    }

    // --- Tests ---

    @Test
    fun `node expansion creates one subproject per version and loader`() {
        writeFixture()

        val result = runner("projects", "-q").build()

        assertTrue(result.output.contains(":mods:modA:1.21.1-fabric"), result.output)
        assertTrue(result.output.contains(":mods:modA:1.21.1-neoforge"), result.output)
        assertTrue(result.output.contains(":mods:modB:1.20.1-fabric"), result.output)
    }

    @Test
    fun `loader bridge sets the modkit-loader extra property per node`() {
        writeFixture()

        val fabric = runner(":mods:modA:1.21.1-fabric:printInfo", "-q").build()
        assertTrue(fabric.output.contains("LOADER=fabric"), fabric.output)

        val neoforge = runner(":mods:modA:1.21.1-neoforge:printInfo", "-q").build()
        assertTrue(neoforge.output.contains("LOADER=neoforge"), neoforge.output)
    }

    @Test
    fun `onVersion range only matches nodes inside the range`() {
        writeFixture()

        val matched = runner(":mods:modA:1.21.1-fabric:printInfo", "-q").build()
        assertTrue(matched.output.contains("MATCHED=yes"), matched.output)

        val notMatched = runner(":mods:modB:1.20.1-fabric:printInfo", "-q").build()
        assertTrue(notMatched.output.contains("MATCHED=null"), notMatched.output)
    }

    @Test
    fun `configuration cache is stored then reused across runs`() {
        writeFixture()

        val first = runner(":mods:modA:1.21.1-fabric:printInfo", "--configuration-cache").build()
        assertTrue(
            first.output.contains("Configuration cache entry stored."),
            "expected store on first run:\n" + first.output
        )

        val second = runner(":mods:modA:1.21.1-fabric:printInfo", "--configuration-cache").build()
        assertTrue(
            second.output.contains("Reusing configuration cache.") || second.output.contains("reused"),
            "expected reuse on second run:\n" + second.output
        )
    }

    @Test
    fun `root layout expands nodes under the root and bridges the loader`() {
        writeRootFixture()

        // Non-monorepo: nodes register directly under the root, not a subproject.
        val projects = runner("projects", "-q").build()
        assertTrue(projects.output.contains(":1.21.1-fabric"), projects.output)
        assertTrue(projects.output.contains(":1.21.1-neoforge"), projects.output)
        assertTrue(!projects.output.contains(":mods:"), projects.output)

        val neoforge = runner(":1.21.1-neoforge:printInfo", "-q").build()
        assertTrue(neoforge.output.contains("LOADER=neoforge"), neoforge.output)
    }
}
