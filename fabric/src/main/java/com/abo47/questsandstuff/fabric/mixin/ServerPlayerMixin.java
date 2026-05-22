package com.abo47.questsandstuff.fabric.mixin;

import com.abo47.questsandstuff.fabric.FabricQuestEventBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "awardStat", at = @At("HEAD"))
    private void questsandstuff$awardStat(Stat<?> stat, int amount, CallbackInfo callback) {
        FabricQuestEventBridge.onAwardStat((ServerPlayer) (Object) this, stat, amount);
    }

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"))
    private void questsandstuff$giveExperiencePoints(int amount, CallbackInfo callback) {
        FabricQuestEventBridge.onXp((ServerPlayer) (Object) this, amount);
    }
}