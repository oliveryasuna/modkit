package com.oliveryasuna.modkit.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic proof: no Minecraft classpath needed. Asserts the mod's identifier
 * constants are well-formed (valid ResourceLocation path characters, correct
 * namespace), which is real, breakable behaviour of the mod.
 */
final class ExampleModTest {

    @Test
    void modIdIsAValidNamespace() {
        assertTrue(
                ExampleMod.MOD_ID.matches("[a-z0-9_.-]+"),
                "modId must be a valid ResourceLocation namespace"
        );
        assertEquals("modkit_example", ExampleMod.MOD_ID);
    }

    @Test
    void itemNameIsAValidPath() {
        assertTrue(
                ExampleMod.ITEM_NAME.matches("[a-z0-9/._-]+"),
                "item name must be a valid ResourceLocation path"
        );
        assertEquals("example_item", ExampleMod.ITEM_NAME);
    }

}
