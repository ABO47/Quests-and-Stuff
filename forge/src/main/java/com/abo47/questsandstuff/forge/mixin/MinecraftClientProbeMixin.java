package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.client.main.GameConfig;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.QuestsAndStuffMod;

@Mixin(Minecraft.class)
public abstract class MinecraftClientProbeMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At("TAIL"))
    private void questsandstuff$probeMixinApplication(GameConfig gameConfig, CallbackInfo ci) {
        QuestsAndStuffMod.LOGGER.info("[QnS:Lock] minecraft ctor mixin probe fired (mixins confirmed active)");
    }
}
