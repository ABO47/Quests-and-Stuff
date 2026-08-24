package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.quest.runtime.lock.possession.PossessionPolicy;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedPickup(Player player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity) (Object) this).getItem();
        if (PossessionPolicy.deniesPickup(player, stack)) {
            ((ItemEntity) (Object) this).setPickUpDelay(20);
            ci.cancel();
        }
    }
}
