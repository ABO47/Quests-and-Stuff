package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public final class ItemLockEnforcement {
    private ItemLockEnforcement() {
    }

    public static boolean isLocked(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.level().isClientSide
                || stack == null
                || stack.isEmpty()
                || player.getAbilities().instabuild) {
            return false;
        }
        try {
            return QuestServiceRegistry.engine(serverPlayer.server).isItemLocked(serverPlayer, stack);
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] lock check failed", error);
            return false;
        }
    }

    public static void filterLoot(ObjectArrayList<ItemStack> drops, LootParams params) {
        if (drops == null || drops.isEmpty()) {
            return;
        }
        try {
            ServerPlayer player = params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer serverPlayer
                    ? serverPlayer
                    : null;
            ServerLevel level = params.getLevel();
            boolean creative = player != null && player.getAbilities().instabuild;
            if (!QuestServiceRegistry.engine(level.getServer()).itemLockIndexHasLocks()) {
                return;
            }
            Iterator<ItemStack> iterator = drops.iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (stack.isEmpty()) {
                    continue;
                }
                if (creative) {
                    continue;
                }
                boolean locked = player == null
                        ? QuestServiceRegistry.engine(level.getServer()).itemLockExists(stack)
                        : QuestServiceRegistry.engine(level.getServer()).isItemLocked(player, stack);
                if (locked) {
                    iterator.remove();
                }
            }
        } catch (Exception error) {
            QuestsAndStuffMod.LOGGER.debug("[QnS:Lock] loot filter failed", error);
        }
    }

    public static boolean blockResultTake(Player player, Container container, int slotIndex, ItemStack taken) {
        if (!isLocked(player, taken)) {
            return false;
        }
        ItemStack current = container.getItem(slotIndex);
        if (current.isEmpty()) {
            container.setItem(slotIndex, taken.copy());
        } else {
            current.grow(taken.getCount());
            container.setChanged();
        }
        return true;
    }
}
