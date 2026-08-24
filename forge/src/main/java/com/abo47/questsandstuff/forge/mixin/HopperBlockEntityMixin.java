package com.abo47.questsandstuff.forge.mixin;

import java.util.UUID;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Unique
    private static final String OWNER_TAG = "qns_owner";

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
        if (level == null || !transferDenied(level, source, destination, stack)) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:Lock] blocked hopper transfer of {}", stack.getItem());
        cir.setReturnValue(stack);
    }

    private static boolean transferDenied(Level level, Container source, Container destination, ItemStack stack) {
        boolean lockedForOwner = false;
        UUID owner = ownerOf(source, destination);
        if (owner != null) {
            ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
            lockedForOwner = ownerPlayer != null && ItemLockEnforcement.isLocked(ownerPlayer, stack);
        }
        if (lockedForOwner) {
            return true;
        }
        if (destination instanceof AbstractFurnaceBlockEntity) {
            return ItemLockEnforcement.cookingOutputLockedDefinition(level, stack)
                    || ItemLockEnforcement.lockedDefinitionExists(level, stack);
        }
        return ItemLockEnforcement.lockedDefinitionExists(level, stack);
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
}
