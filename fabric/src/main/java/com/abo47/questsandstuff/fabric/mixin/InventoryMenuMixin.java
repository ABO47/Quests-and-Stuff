package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedShiftCraft(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != 0) {
            return;
        }
        ItemStack result = ((InventoryMenu) (Object) this).slots.get(0).getItem();
        if (ItemLockEnforcement.isLocked(player, result)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
