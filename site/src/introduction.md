---
title: Introduction
---

# Introduction

Modkit is a suite of Gradle plugins that lets your build Minecraft mods targeting **multiple loaders**, across **multiple Minecraft versions**, from **one codebase**, and with **one build configuration**.

## The problem it solves

Shipping a mod on more than one loader or Minecraft version requires different build configurations for each loader and version. This is time-consuming and error-prone.

- **`N` loaders, `N` of everything.** Each loader wants different manifests, different mixin wiring, different dependency configurations, different resource paths, different run tasks, different datagen setups. Keeping them in sync by hand is tedious and error-prone.
- **Multiple Minecraft versions multiply the work.** Different versions need different mappings, toolchains, and dependency versions, and often source that only compiles against one version. Managing that usually means copy-pasted subprojects or a source preprocessor you have to set up yourself (e.g., [Stonecutter](https://stonecutter.kikugie.dev/)).
- **The best tools don't talk to each other.** Loom, ModDevGradle, Stonecutter, etc. each solve a particular problem. But who has to glue them together? You do, for every loader and version and project you want to support (and it's not always easy).
- **Boilerplate everywhere.** Mapping and Parchment, access widenerss vs access transformers, refmaps, run configs, datagen setups, Modrinth/CurseForge/GitHub uploads, a CI matrix... none of it is your mod's actual logic, and yet you have to manage it all.

Modkit's goal is simple: **you configure your mod once, and it just builds**.

## How it works

Modkit **wraps, it doesn't replace.** Under the hood, it uses the best tools for the job:

- [ForgeGradle](https://github.com/minecraftforge/forgegradle) for Forge.
- [Fabric Loom](https://github.com/fabricmc/fabric-loom) for Fabric.
- [ModDevGradle](https://github.com/neoforged/ModDevGradle) for NeoForge.
- [Quilt Loom](https://github.com/QuiltMC/quilt-loom) for Quilt.
- [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version support.

Your mod code stays the same. Modkit is a built-time convenience layer over those tools.

## How it compares

This section is intended to give you a sense of how Modkit compares to other tools in the ecosystem. It is not exhaustive, and we highly recommend exploring other tools in the ecosystem to see which one fits your needs best.

- **Architectury** gives you a *runtime* abstraction. You write your code against its API. Modkit is **build-time only**, which means you can absolutely use Architectury's API in your mod code, or keep your code native to each loader. They solve different layers.
- **Architectury Loom** is a fork of Fabric Loom that adds Forge, NeoForge, and Quilt support, so a single Loom-based toolchain can build for multiple loaders. It *replaces* your toolchain; Modkit **wraps** the best mainstream tools instead, rather than routing every loader through one forked plugin.
- **Modstitch** targets a similar "one build, many platforms" goal. Modkit covers a wider range of use cases, and is a bit less opinionated about how you want to structure your project.
- **Stonecutter** handles multi-version source preprocessing and subproject expansion. Modkit **uses** Stonecutter under the hood, and adds the single-DSL layer to glue everything together.

## Where to go next

<!-- TODO: Where to go next. -->
