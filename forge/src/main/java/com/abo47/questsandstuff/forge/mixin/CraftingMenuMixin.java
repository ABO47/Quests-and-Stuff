package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @Shadow
    private Player player;

    @Shadow
    private ResultContainer resultSlots;

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedShiftCraft(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != 0) {
            return;
        }
        ItemStack result = ((CraftingMenu) (Object) this).slots.get(0).getItem();
        if (ItemLockEnforcement.isLocked(player, result)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void questsandstuff$hideLockedResult(Container container, CallbackInfo ci) {
        ItemStack result = this.resultSlots.getItem(0);
        if (!result.isEmpty() && ItemLockEnforcement.isLocked(this.player, result)) {
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] suppressed crafting-table output of {}", result.getItem());
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}
