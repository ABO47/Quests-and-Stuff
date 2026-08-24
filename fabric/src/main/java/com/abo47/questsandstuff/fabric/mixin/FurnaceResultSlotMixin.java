package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedSmelt(Player player, ItemStack stack, CallbackInfo ci) {
        Slot slot = (Slot) (Object) this;
        Container container = slot.container;
        if (ItemLockEnforcement.blockResultTake(player, container, slot.getContainerSlot(), stack)) {
            ci.cancel();
        }
    }
}
