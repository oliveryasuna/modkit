import com.oliveryasuna.modkit.core.extension.ModLoader

plugins {
    java
    id("com.oliveryasuna.modkit.loaders") version "0.10.0"
    id("com.oliveryasuna.modkit.metadata") version "0.10.0"
    id("com.oliveryasuna.modkit.run") version "0.10.0"
}

repositories {
    maven("https://maven.terraformersmc.com/")
}

modkit {
    modId = "modkit_example_dev"
    group = "com.oliveryasuna.modkit.example"
    version = "1.0.0"
    displayName = "Modkit Example Dev"
    description = "Dev example"
    license = "Apache-2.0"

    minecraft("1.21.11") {
        loaders.add(ModLoader.FABRIC)
    }

    loaders {
        fabric {
            loaderVersion = "0.19.5"
            apiVersion = "0.141.6+1.21.11"
        }
        neoforge {
            version = "21.11.45"
        }
    }

    metadata {
        environment = "*"
        entrypoints {
            main("com.oliveryasuna.modkit.example.ExampleModFabric")
        }
    }

    run {
        variants {
            register("modMenu") {
                appliesTo("client")
                mods("com.terraformersmc:modmenu:17.0.0")
            }
        }
    }
}
