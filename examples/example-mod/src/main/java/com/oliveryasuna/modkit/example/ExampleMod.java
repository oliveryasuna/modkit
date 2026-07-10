package com.oliveryasuna.modkit.example;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader- and version-agnostic core of the example mod: identifiers, the logger,
 * and the item id. Only the two {@code LOADER}/{@code MC_VERSION} constants are
 * Stonecutter-branched (they are the log-line proof); everything else is shared.
 */
public final class ExampleMod {

    public static final String MOD_ID = "modkit_example";

    public static final String ITEM_NAME = "example_item";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if fabric {
    /*public static final String LOADER = "Fabric";
    *///?} else {
    public static final String LOADER = "NeoForge";
    //?}

    public static final String MC_VERSION =
            //? if >=1.21 {
            /*"1.21.1";
            *///?} else
            "1.20.6";

    private ExampleMod() {
        throw new AssertionError("No com.oliveryasuna.modkit.example.ExampleMod instances for you!");
    }

    /**
     * The mod's item id. {@code ResourceLocation}'s construction differs between
     * 1.20.6 (public ctor) and 1.21.1 (static factory), so branch it.
     */
    public static ResourceLocation itemId() {
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(MOD_ID, ITEM_NAME);
        *///?} else
        return new ResourceLocation(MOD_ID, ITEM_NAME);
    }

}
