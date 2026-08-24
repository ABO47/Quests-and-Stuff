package com.abo47.questsandstuff.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.abo47.questsandstuff.fabric.FabricQuestEventBridge;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockMenuGating;
import com.abo47.questsandstuff.quest.runtime.lock.OpenMenuIndex;

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

    @Inject(method = "initMenu", at = @At("TAIL"))
    private void questsandstuff$onMenuOpened(AbstractContainerMenu menu, int containerCounter, CallbackInfo callback) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        OpenMenuIndex.record(player, menu);
        ItemLockMenuGating.gateCraftingMenu(player, menu);
    }

    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void questsandstuff$onMenuClosed(CallbackInfo callback) {
        OpenMenuIndex.unrecord(((ServerPlayer) (Object) this).containerMenu);
    }
}
