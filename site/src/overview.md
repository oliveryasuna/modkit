---
title: Overview
---

<img src="/modkit-banner-light.png" alt="Modkit" class="modkit-banner light-only">
<img src="/modkit-banner-dark.png" alt="Modkit" class="modkit-banner dark-only">

# Modkit

**One Gradle DSL for building multi-loader Minecraft mods from a single codebase.**

You describe your mod once in a `modkit { }` block. Modkit wraps the build tools the ecosystem already relies on and drives them for whatever loader and Minecraft version you're targeting. That way you don't have to wire each tool up by hand, and differently for every loader.

Modkit is open-source! Check out the [code](https://github.com/oliveryasuna/modkit).

## Start here

- [Introduction](./introduction): what Modkit is, the mod-dev pain it removes, and how it stacks up against the alternatives.
- [Getting started](./getting-started): a minimal single-loader mod, built end-to-end with Modkit.

## Learn the model

- [The Modkit model](./concepts/the-model): the shared `modkit { }` model and how the active loader gets picked.
- [The plugin suite](./concepts/the-plugin-suite): the modules, and the wrap-don't-reinvent thinking behind them.
- [Multi-version builds](./concepts/multi-version): building one codebase against many Minecraft versions with Stonecutter.

## Guides

- [Mod metadata](./guides/metadata): generate manifests (e.g., `fabric.mod.json`) from the model.
- [Mixins](./guides/mixins): register mixin configs and lint targets.
- [Dependencies](./guides/dependencies): `mod()`, jar-in-jar `nest()`, and mod repositories.
- [Run configurations](./guides/runs): unified client/server/data/gametest runs.
- [Data generation](./guides/datagen): wire datagen across both loaders.
- [Publishing](./guides/publishing): Modrinth, CurseForge, GitHub, Discord.
- [Continuous integration](./guides/ci): generate a version × loader CI matrix.
- [Testing](./guides/testing): pure-logic JUnit and NeoForge GameTest.
- [Multi-version](./guides/multi-version): set up a monorepo or single-mod version matrix.

## Reference

- [`modkit { }` (core)](./reference/core) and the per-plugin reference: [Loaders](./reference/loaders), [Metadata](./reference/metadata), [Mixins](./reference/mixins), [Dependencies](./reference/dependencies), [Run](./reference/run), [Datagen](./reference/datagen), [Publish](./reference/publish), [CI](./reference/ci), [Testing](./reference/testing), [Multiversion](./reference/multiversion), [Scaffold](./reference/scaffold).
