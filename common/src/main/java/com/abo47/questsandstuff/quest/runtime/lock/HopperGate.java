package com.abo47.questsandstuff.quest.runtime.lock;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public final class HopperGate {
    private HopperGate() {
    }

    public static boolean machineOutputLocked(Level level, Container destination, ItemStack stack) {
        if (!ItemLockEnforcement.locksActive()
                || stack == null
                || stack.isEmpty()
                || level == null
                || level.isClientSide
                || destination == null) {
            return false;
        }
        return destination instanceof AbstractFurnaceBlockEntity
                && ItemLockEnforcement.cookingOutputLockedDefinition(level, stack);
    }

    public static boolean itemLocked(Level level, ItemStack stack) {
        if (!ItemLockEnforcement.locksActive()
                || stack == null
                || stack.isEmpty()
                || level == null
                || level.isClientSide) {
            return false;
        }
        return ItemLockEnforcement.lockedDefinitionExists(level, stack);
    }
}
