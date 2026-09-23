---
title: Getting started
---

# Getting started

This walkthrough builds a minimal mod for a single loader and Minecraft version, so you can see the whole `modkit { }` model in action. Building the same source for another loader is a one-line change at the end.

<!-- TODO: Tip about Scaffold plugin. -->

## Prerequisites

- **JDK 21** to run Gradle.
- **Gradle 9+**. The wrapper in a generated project handles this for you.

## 1. Settings

Modkit's plugins and the loader tooling their wrap live on a few Maven repositories. Declare them in `settings.gradle.kts`, and add [foojay-resolver](https://github.com/gradle/foojay-resolver-convention) so Gradle can auto-provision the JDK your target Minecraft version requires:

::: code-group

```groovy [Groovy]
// settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://maven.neoforged.net/releases/' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://maven.fabricmc.net/' }
        maven { url 'https://maven.neoforged.net/releases/' }
    }
}

rootProject.name = 'mymod'
```

```kotlin [Kotlin]
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

rootProject.name = "mymod"
```

:::

## 2. The build script

Apply the [Loaders](./reference/loaders) plugin (the base every mod needs) plus whichever feature plugins you want. Here, that's [Metadata](./reference/metadata) to generate the manifest.  Then describe the mod in one `modkit { }` block:

::: code-group

```groovy-vue [Groovy]
// build.gradle
import com.oliveryasuna.modkit.core.extension.ModLoader

plugins {
    id 'com.oliveryasuna.modkit.loaders' version '{{ $modkitVersion }}'
    id 'com.oliveryasuna.modkit.metadata' version '{{ $modkitVersion }}'
}

modkit {
    modId = 'mymod'
    group = 'com.example'
    version = '1.0.0'
    license = 'MIT'

    minecraft('1.21.11') {
        loaders.add(ModLoader.FABRIC)
    }

    metadata {
        entrypoints {
            main('com.example.mymod.MyMod')
        }
    }
}
```

```kotlin-vue [Kotlin]
// build.gradle.kts
import com.oliveryasuna.modkit.core.extension.ModLoader

plugins {
    id("com.oliveryasuna.modkit.loaders") version "{{ $modkitVersion }}"
    id("com.oliveryasuna.modkit.metadata") version "{{ $modkitVersion }}"
}

modkit {
    modId = "mymod"
    group = "com.example"
    version = "1.0.0"
    license = "MIT"

    minecraft("1.21.11") {
        loaders.add(ModLoader.FABRIC)
    }

    metadata {
        entrypoints {
            main("com.example.mymod.MyMod")
        }
    }
}
```

:::

There's no hand-written manifests; [Metadata](./reference/metadata) generates them from the model.

## 3. Select the active loader

A single build compiles one  `(version, loader)` pair. Which loader is chosen is a Gradle property, so set it in `gradle.properties`:

```properties
# gradle.properties
modkit.loader=fabric
```

## 4. A mod entry point

Add the class you named as the entrypoint:

```java
// src/main/java/com/example/mymod/MyMod.java
package com.example.mymod;

import net.fabricmc.api.ModInitializer;

public class MyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Hello from mymod!");
    }
}
```

## 5. Build

```sh
./gradlew build
```

Gradle provisions the right JDK, Loom downloads Minecraft, [Metadata](./reference/metadata) writes `fabric.mod.json`, and you get a loadable jar in `build/libs/`.

::: tip There's an easier way
If you don't want want to have to build and manually copy the jar to your Minecraft instance, check out the [Run configurations](./guides/runs) guide.
:::

## Building for NeoForge

The same source and the same `build.gradle.kts` build for NeoForge. Declare the loader on the target and flip the property:

::: code-group

```groovy [Groovy]
minecraft('1.21.11') {
    loaders.add(ModLoader.FABRIC)
    loaders.add(ModLoader.NEOFORGE) // [!code focus]
}
```

```kotlin [Kotlin]
minecraft("1.21.11") {
    loaders.add(ModLoader.FABRIC)
    loaders.add(ModLoader.NEOFORGE) // [!code focus]
}
```

:::

You can either change `modkit.loader` in `gradle.properties`, or pass it as a command-line argument to the build command:

```sh
./gradlew build -Pmodkit.loader=neoforge
```

**The same applies for other loaders.**

::: tip Cross-loader support
For a real cross-loader mod, you'd guard loader-specific code. That's what [Multi-version builds](./concepts/multi-version) and source preprocessing are for.
:::

## Next steps

- [The Modkit model](./concepts/the-model): how the model and active-loader selection fit together.
- [The plugin suite](./concepts/the-plugin-suite): add mixins, dependencies, runs, publishing, and CI.
- [Multi-version builds](./concepts/multi-version): one codebase, many Minecraft versions.
