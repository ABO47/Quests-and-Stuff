package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedResult(Player player, ItemStack stack, CallbackInfo ci) {
        Slot slot = (Slot) (Object) this;
        Container container = slot.container;
        if (ItemLockEnforcement.blockResultTake(player, container, slot.getContainerSlot(), stack)) {
            ci.cancel();
        }
    }
}
