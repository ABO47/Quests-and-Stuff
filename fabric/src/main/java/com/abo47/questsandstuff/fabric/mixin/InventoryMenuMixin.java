package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @Shadow
    private Player owner;

    @Shadow
    private ResultContainer resultSlots;

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

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void questsandstuff$hideLockedResult(Container ignored, CallbackInfo ci) {
        ItemStack result = this.resultSlots.getItem(0);
        if (!result.isEmpty() && ItemLockEnforcement.isLocked(this.owner, result)) {
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] suppressed inventory-grid output of {}", result.getItem());
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}
