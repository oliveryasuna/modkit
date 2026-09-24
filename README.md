# Modkit

<a href="https://modkitmc.com/"><img alt="ghpages" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/ghpages_vector.svg"></a>
<!-- TODO: Plugin portal badge. -->
<a href="https://discord.com/invite/WzcXYYbcr7"><img alt="discord-singular" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/social/discord-singular_vector.svg"></a>
<a href="https://github.com/sponsors/oliveryasuna"><img alt="kofi-plural" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/donate/kofi-plural_vector.svg"></a>

Modkit is a suite of Gradle plugins that simplifies the tooling for Minecraft mod development. You describe your mod once, including its identity, target versions, dependencies, and metadata, and Modkit automatically generates builds for multiple loaders (such as Fabric and NeoForge) based on that single description.

**Status: beta.** Expect some churn before `1.0.0`. Found a bug or have feedback? [Open an issue](https://github.com/oliveryasuna/modkit/issues).

## Contributing

We are always looking for contributors! If you're interested in helping out, please check out our [contribution guidelines](./CONTRIBUTING.md).

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

## Contributors

<!-- readme: contributors -start -->
<table>
	<tbody>
		<tr>
            <td align="center">
                <a href="https://github.com/oliveryasuna">
                    <img src="https://avatars.githubusercontent.com/u/17092333?v=4" width="100;" alt="oliveryasuna"/>
                    <br />
                    <sub><b>Oliver Yasuna</b></sub>
                </a>
            </td>
		</tr>
	<tbody>
</table>
<!-- readme: contributors -end -->

## License

Modkit is licensed under Apache 2.0. [Full license text](./LICENSE).
