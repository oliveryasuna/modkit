//? if neoforge {
package com.oliveryasuna.modkit.example.datagen;

import com.oliveryasuna.modkit.example.ExampleMod;
import com.oliveryasuna.modkit.example.neoforge.ExampleModNeoForge;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * NeoForge data generation. Registered on the mod event bus via
 * {@code @EventBusSubscriber}. Generates the {@code example_item} model
 * ({@link ItemModelProvider#basicItem}) and its en_us translation
 * ({@link LanguageProvider}).
 *
 * No recipe provider (deliberately) -- matches the Fabric side.
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ExampleDataGeneratorNeoForge {

    private ExampleDataGeneratorNeoForge() {
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        final ExistingFileHelper fileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModels(output, fileHelper));
        generator.addProvider(event.includeClient(), new EnglishLanguage(output));
    }

    private static final class ItemModels extends ItemModelProvider {

        private ItemModels(final PackOutput output, final ExistingFileHelper fileHelper) {
            super(output, ExampleMod.MOD_ID, fileHelper);
        }

        @Override
        protected void registerModels() {
            basicItem(ExampleModNeoForge.EXAMPLE_ITEM.get());
        }

    }

    private static final class EnglishLanguage extends LanguageProvider {

        private EnglishLanguage(final PackOutput output) {
            super(output, ExampleMod.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(ExampleModNeoForge.EXAMPLE_ITEM.get(), "Example Item");
        }

    }

}
//?}
