package com.abo47.questsandstuff.forge.mixin;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
    private static final String OWNER_TAG = "qns_owner";
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
        if (HopperGate.machineOutputLocked(level, destination, stack)) {
            logBlocked("machine-output", stack);
            return true;
        }
        if (!HopperGate.itemLocked(level, stack)) {
            return false;
        }
        UUID owner = ownerOf(source, destination);
        if (owner != null && ownerCanMove(level, owner, stack)) {
            return false;
        }
        logBlocked("item", stack);
        return true;
    }

    @Unique
    private static void logBlocked(String reason, ItemStack stack) {
        long now = System.currentTimeMillis();
        if (now - lastBlockLogMs >= 2000L) {
            lastBlockLogMs = now;
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] blocked hopper transfer of {} ({})", stack.getItem(), reason);
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

    private static UUID ownerOf(Container source, Container destination) {
        UUID fromSource = readStamp(source);
        return fromSource != null ? fromSource : readStamp(destination);
    }

    private static UUID readStamp(Container container) {
        if (container instanceof BlockEntity blockEntity
                && blockEntity.getPersistentData().hasUUID(OWNER_TAG)) {
            return blockEntity.getPersistentData().getUUID(OWNER_TAG);
        }
        return null;
    }

    private static boolean ownerCanMove(Level level, UUID ownerId, ItemStack stack) {
        if (level == null || level.getServer() == null) {
            return false;
        }
        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(ownerId);
        return ownerPlayer != null && !ItemLockEnforcement.isLocked(ownerPlayer, stack);
    }
}
