//? if fabric {
/*package com.oliveryasuna.modkit.example.datagen;

import com.oliveryasuna.modkit.example.ExampleMod;
import com.oliveryasuna.modkit.example.fabric.ExampleModFabric;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

import java.util.concurrent.CompletableFuture;

/^*
 * Fabric {@link DataGeneratorEntrypoint}. Generates the {@code example_item}
 * model and its en_us lang entry. Registered via
 * {@code modkit { datagen { entrypoint.set("...ExampleDataGenerator") } }}, which
 * the datagen plugin folds into fabric.mod.json {@code entrypoints.fabric-datagen}.
 *
 * No recipe provider (deliberately) -- the 1.20.x/1.21.x recipe-provider API
 * diverged and that break is out of scope for this showcase.
 *
 * The model/lang classes live in {@code net.minecraft.data.models.*} on both
 * 1.20.6 and 1.21.1 (they only moved to {@code net.minecraft.client.data.models}
 * in 1.21.4), so no Stonecutter branch is needed here.
 ^/
public final class ExampleDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator) {
        final FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModelProvider::new);
        pack.addProvider(EnglishLanguageProvider::new);
    }

    private static final class ModelProvider extends FabricModelProvider {

        private ModelProvider(final FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(final BlockModelGenerators generators) {
            // No blocks.
        }

        @Override
        public void generateItemModels(final ItemModelGenerators generators) {
            generators.generateFlatItem(ExampleModFabric.EXAMPLE_ITEM, ModelTemplates.FLAT_ITEM);
        }

    }

    private static final class EnglishLanguageProvider extends FabricLanguageProvider {

        private EnglishLanguageProvider(final FabricDataOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
            super(output, "en_us", registries);
        }

        @Override
        public void generateTranslations(final HolderLookup.Provider registries, final TranslationBuilder builder) {
            builder.add(ExampleModFabric.EXAMPLE_ITEM, "Example Item");
        }

    }

}
*///?}
