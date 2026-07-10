package com.oliveryasuna.modkit.example.mixin;

import com.oliveryasuna.modkit.example.ExampleMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The visible proof the mod's mixin machinery loaded. Shared across both loaders
 * and both Minecraft versions -- {@code loadLevel()V} is mojmap-stable on 1.20.6
 * and 1.21.1, so no Stonecutter branch is needed here.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void modkit_example$onLoadLevel(final CallbackInfo ci) {
        ExampleMod.LOGGER.info(
                "[{}] loaded on {} for MC {} -- mixin active!",
                ExampleMod.MOD_ID,
                ExampleMod.LOADER,
                ExampleMod.MC_VERSION
        );
    }

}
