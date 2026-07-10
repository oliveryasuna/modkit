//? if neoforge {
package com.oliveryasuna.modkit.example.neoforge;

import com.oliveryasuna.modkit.example.ExampleMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge entrypoint. Discovered by the {@code @Mod} annotation (no manifest
 * entry). Registers {@code example_item} through a DeferredRegister on the mod
 * bus and adds it to the ingredients creative tab.
 */
@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ExampleMod.MOD_ID);

    public static final DeferredItem<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem(ExampleMod.ITEM_NAME, new Item.Properties());

    public ExampleModNeoForge(final IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);

        ExampleMod.LOGGER.info("[{}] NeoForge constructor ran -- registered {}", ExampleMod.MOD_ID, ExampleMod.ITEM_NAME);
    }

    private void addCreative(final BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(EXAMPLE_ITEM);
        }
    }

}
//?}
