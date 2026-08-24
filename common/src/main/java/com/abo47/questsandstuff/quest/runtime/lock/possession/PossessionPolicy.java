package com.abo47.questsandstuff.quest.runtime.lock.possession;

import java.util.function.BooleanSupplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

public final class PossessionPolicy {
    private static long lastDenyLogMs;

    private PossessionPolicy() {
    }

    public static boolean deniesUse(Player player, ItemStack stack) {
        return denies(player, stack, "use", QuestsAndStuffConfig::itemLockBlockUse);
    }

    public static boolean deniesPickup(Player player, ItemStack stack) {
        return denies(player, stack, "pickup", QuestsAndStuffConfig::itemLockBlockPickup);
    }

    public static boolean deniesEquip(Player player, ItemStack stack) {
        return denies(player, stack, "equip", QuestsAndStuffConfig::itemLockBlockEquip);
    }

    private static boolean denies(Player player, ItemStack stack, String action, BooleanSupplier enabled) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide) {
            return false;
        }
        if (stack == null || stack.isEmpty() || !enabled.getAsBoolean()) {
            return false;
        }
        if (player.getAbilities().instabuild || !ItemLockEnforcement.locksActive()) {
            return false;
        }
        boolean denied = ItemLockEnforcement.isLocked(serverPlayer, stack);
        if (denied) {
            logDeny(serverPlayer, stack, action);
        }
        return denied;
    }

    private static void logDeny(ServerPlayer player, ItemStack stack, String action) {
        long now = System.currentTimeMillis();
        if (now - lastDenyLogMs >= 2000L) {
            lastDenyLogMs = now;
            QuestsAndStuffMod.LOGGER.info(
                    "[QnS:Lock] denied {} of {} by {} (possession policy)",
                    action, stack.getItem(), player.getName().getString());
        }
    }
}
