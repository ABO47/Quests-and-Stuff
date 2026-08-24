package com.abo47.questsandstuff.quest.runtime.lock;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class LockMenuRefresher {
    private LockMenuRefresher() {
    }

    public static void refreshOpenMenus() {
        OpenMenuIndex.forEachTracked((player, menu) -> refresh(player, menu));
    }

    private static void refresh(ServerPlayer player, AbstractContainerMenu menu) {
        try {
            if (menu instanceof CraftingMenu crafting && menu.slots.size() > 1) {
                Container grid = menu.slots.get(1).container;
                crafting.slotsChanged(grid);
            }
            menu.broadcastChanges();
        } catch (Exception error) {
            QuestsAndStuffMod.debugLog("[QnS:Lock] menu refresh skipped: {}", error.toString());
        }
    }
}
