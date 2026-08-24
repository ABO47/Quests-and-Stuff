package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.HopperGate;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Unique
    private static long lastBlockLogMs;

        @Inject(method = "tryMoveInItem", at = @At("HEAD"), cancellable = true)
    private static void questsandstuff$blockLockedHopperMerge(
            Container source,
            Container destination,
            ItemStack stack,
            int slot,
            Direction direction,
            CallbackInfoReturnable<ItemStack> cir) {
        if (questsandstuff$hopperDenied(source, destination, stack)) {
            cir.setReturnValue(stack);
        }
    }

    @Inject(method = "canTakeItemFromContainer", at = @At("HEAD"), cancellable = true)
    private static void questsandstuff$blockLockedHopperTake(
            Container source,
            Container destination,
            ItemStack stack,
            int slot,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir) {
        if (questsandstuff$hopperDenied(source, destination, stack)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canPlaceItemInContainer", at = @At("HEAD"), cancellable = true)
    private static void questsandstuff$blockLockedHopperPlace(
            Container destination,
            ItemStack stack,
            int slot,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir) {
        if (questsandstuff$hopperDenied(destination, destination, stack)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean questsandstuff$hopperDenied(Container source, Container destination, ItemStack stack) {
        Level level = questsandstuff$levelOf(source, destination);
        if (level == null) {
            return false;
        }
        boolean denied = HopperGate.machineOutputLocked(level, destination, stack)
                || HopperGate.itemLocked(level, stack);
        if (denied) {
            logBlocked(stack);
        }
        return denied;
    }

    @Unique
    private static void logBlocked(ItemStack stack) {
        long now = System.currentTimeMillis();
        if (now - lastBlockLogMs >= 2000L) {
            lastBlockLogMs = now;
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] blocked hopper transfer of {}", stack.getItem());
        }
    }

    private static Level questsandstuff$levelOf(Container source, Container destination) {
        if (source instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return blockEntity.getLevel();
        }
        if (destination instanceof BlockEntity blockEntity && blockEntity.getLevel() != null) {
            return blockEntity.getLevel();
        }
        return null;
    }
}
