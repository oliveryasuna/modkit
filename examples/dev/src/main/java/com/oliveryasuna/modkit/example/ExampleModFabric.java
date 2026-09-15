package com.oliveryasuna.modkit.example;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ExampleModFabric implements ModInitializer {

    public static final String MOD_ID = "modkit_example_dev";

    private static final ResourceKey<Item> EXAMPLE_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "example_item"));
    private static final Item EXAMPLE_ITEM = Registry.register(BuiltInRegistries.ITEM, EXAMPLE_ITEM_KEY, new Item(new Item.Properties().setId(EXAMPLE_ITEM_KEY)));

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(EXAMPLE_ITEM));
    }
}
