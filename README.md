# Modkit

Modkit is a suite of Gradle plugins that simplifies the tooling for Minecraft mod development. You to describe your mod once, including its identity, target versions, dependencies, and metadata, and Modkit automatically generates builds for multiple loaders (such as Fabric and NeoForge) based on that single description.

**Status: beta.** Expect some churn before `1.0.0`. Found a bug or have feedback? [Open an issue](https://github.com/oliveryasuna/modkit/issues).

## Plugins

_Some plugins may not yet be implemented during the beta._

| Plugin         | Summary                                                                                                              |
|----------------|----------------------------------------------------------------------------------------------------------------------|
| `ci`           | Generates GitHub Actions workflows.                                                                                  |
| `core`         | Foundation shared by every other plugin.                                                                             |
| `datagen`      | Wires Minecraft data generation across loaders.                                                                      |
| `dependencies` | Unifies mod-dependency declaration, JiJ nesting, and repositories.                                                   |
| `loaders`      | Unifies mod loader-specific tooling in a single model.                                                               |
| `metadata`     | Generates mod metadata (e.g., `fabric.mod.json`).                                                                    |
| `mixins`       | Registers mixin configs, provides the refmap convention, and publishes config names to the shared manifest registry. |
| `multiversion` | Stonecutter integration.                                                                                             |
| `publish`      | Simplifies publishing to Modrinth, CurseForge, GitHub, and Discord.                                                  |
| `run`          | Unifies mod-loader specific run configurations in a single model                                                     |
| `scaffold`     | Generates new projects that use Modkit.                                                                              |
| `testing`      | Sets up JUnit and wires GameTest runs.                                                                               |

## License

Modkit is licensed under Apache 2.0. [Full license text](./LICENSE).
