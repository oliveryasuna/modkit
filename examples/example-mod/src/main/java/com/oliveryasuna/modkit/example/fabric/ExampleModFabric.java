//? if fabric {
/*package com.oliveryasuna.modkit.example.fabric;

import com.oliveryasuna.modkit.example.ExampleMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/^*
 * Fabric entrypoint. Registers {@code example_item} and adds it to the
 * ingredients creative tab. Referenced from fabric.mod.json
 * {@code entrypoints.main} (wired by the metadata plugin).
 ^/
public final class ExampleModFabric implements ModInitializer {

    public static final Item EXAMPLE_ITEM = new Item(new Item.Properties());

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, ExampleMod.itemId(), EXAMPLE_ITEM);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(EXAMPLE_ITEM));

        ExampleMod.LOGGER.info("[{}] Fabric initializer ran -- registered {}", ExampleMod.MOD_ID, ExampleMod.ITEM_NAME);
    }

}
*///?}
