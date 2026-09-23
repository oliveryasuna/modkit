---
title: The Modkit model
---

# The Modkit model

Everything Modkit does flows from one shared model: the `modkit { }` extension. Every plugin in the suite reads from it, so you describe your mod once and each plugin configures its slice of the build accordingly.

## One model, every plugin reads it

`modkit { }` holds the loader-independent facts about your mod: identity, targets, and nested blocks each feature plugin contributes.

::: code-group

```groovy [Groovy]
import com.oliveryasuna.modkit.core.extension.ModLoader

modkit {
    modId = 'mymod'
    group = 'com.example'
    version = '1.0.0'
    displayName = 'My Mod'
    description = 'Does a thing.'
    license = 'MIT'
    authors.add('You')

    urls {
        homepage = 'https://example.com/mymod'
        source = 'https://github.com/you/mymod'
        issues = 'https://github.com/you/mymod/issues'
    }

    minecraft('1.21.11') {
        loaders.add(ModLoader.FABRIC)
        loaders.add(ModLoader.NEOFORGE)
    }

    // Nested blocks contributed by feature plugins, when applied:
    metadata { /* ... */ }
    mixins { /* ... */ }
    run { /* ... */ }
}
```

```kotlin [Kotlin]
import com.oliveryasuna.modkit.core.extension.ModLoader

modkit {
    modId = "mymod"
    group = "com.example"
    version = "1.0.0"
    displayName = "My Mod"
    description = "Does a thing."
    license = "MIT"
    authors.add("You")

    urls {
        homepage = "https://example.com/mymod"
        source = "https://github.com/you/mymod"
        issues = "https://github.com/you/mymod/issues"
    }

    minecraft("1.21.11") {
        loaders.add(ModLoader.FABRIC)
        loaders.add(ModLoader.NEOFORGE)
    }

    // Nested blocks contributed by feature plugins, when applied:
    metadata { /* ... */ }
    mixins { /* ... */ }
    run { /* ... */ }
}
```

:::

`modId` is the only required field. The `metadata`, `mixins`, `run`, `datagen`, `publish`, and `ci` blocks only appear when their plugin is applied; each plugin attaches its own block to the root model. See [The plugin suite](./the-plugin-suite) for more details.

## Targets: versions and loaders

`minecraft(version) { loaders.add(...) }` declares a **target**: a Minecraft version and the loaders you support on it. Targets are the source of truth for the version × loader matrix that [CI](../guides/ci) and [Multi-version builds](./multi-version) expand.

## One build, one loader

A single Gradle invocation builds exactly **one** `(version, loader)` pair. This is because tooling such as Fabric and NeoForge can't share a build; they need different toolchains, source sets, and dependencies, and those are decided very early, before the `modkit { }` block has even been read.

So, the active loader isn't part of the lazy model. It's a **Gradle property**, `modkit.loader`, read eagerly when the build starts:

```properties
# gradle.properties
modkit.loader=fabric
```

You can also pass it as a command-line argument to the build command:

```sh
./gradlew build -Pmodkit.loader=neoforge
```

Each value applies the respective plugin and its toolchain (e.g., `fabric` applies Fabric Loom). With no property set, Modkit configures the model and diagnostics, but applies no loader base, which is enough to inspect the project but not to build a mod.

A couple of related structural switches work the same way for the same reason. `modkit.commonSourceSet` (which source set holds loader-common code) and `modkit.splitCLient` (whether to create a separate client source set) are properties because binding the mod to a source set has to happen before the loader finalizes its configuration, too early for the lazy model.

## From one loader to the whole matrix

<!-- TODO: From one loader to the whole matrix. -->
