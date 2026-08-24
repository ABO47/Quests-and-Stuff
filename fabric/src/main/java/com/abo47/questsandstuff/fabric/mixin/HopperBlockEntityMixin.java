package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Inject(method = "tryMoveInItem", at = @At("HEAD"), cancellable = true)
    private static void questsandstuff$blockLockedHopperTransfer(
            Container source,
            Container destination,
            ItemStack stack,
            int slot,
            Direction direction,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!ItemLockEnforcement.locksActive()
                || stack == null
                || stack.isEmpty()
                || destination == null) {
            return;
        }
        Level level = levelOf(source, destination);
        if (level == null) {
            return;
        }
        boolean denied = destination instanceof AbstractFurnaceBlockEntity
                ? ItemLockEnforcement.cookingOutputLockedDefinition(level, stack)
                || ItemLockEnforcement.lockedDefinitionExists(level, stack)
                : ItemLockEnforcement.lockedDefinitionExists(level, stack);
        if (denied) {
            QuestsAndStuffMod.debugLog("[QnS:Lock] blocked hopper transfer of {}", stack.getItem());
            cir.setReturnValue(stack);
        }
    }

    private static Level levelOf(Container source, Container destination) {
        if (source instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return blockEntity.getLevel();
        }
        if (destination instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return blockEntity.getLevel();
        }
        return null;
    }
}
