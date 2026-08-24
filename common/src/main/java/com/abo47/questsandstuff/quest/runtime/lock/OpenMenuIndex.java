package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class OpenMenuIndex {
    private static final Map<AbstractContainerMenu, ServerPlayer> OPEN_MENUS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private OpenMenuIndex() {
    }

    public static void record(ServerPlayer player, AbstractContainerMenu menu) {
        if (player == null || menu == null) {
            return;
        }
        OPEN_MENUS.put(menu, player);
    }

    public static void unrecord(AbstractContainerMenu menu) {
        if (menu == null) {
            return;
        }
        if (OPEN_MENUS.remove(menu) != null) {
            QuestsAndStuffMod.debugLog("[QnS:Lock] open menu unrecorded ({})", menu.getClass().getSimpleName());
        }
    }

    public static Player resolveByGrid(Container grid) {
        if (grid == null) {
            return null;
        }
        synchronized (OPEN_MENUS) {
            for (Map.Entry<AbstractContainerMenu, ServerPlayer> entry : OPEN_MENUS.entrySet()) {
                for (var slot : entry.getKey().slots) {
                    if (slot != null && slot.container == grid) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static int trackedMenus() {
        return OPEN_MENUS.size();
    }

    public static void forEachTracked(java.util.function.BiConsumer<ServerPlayer, AbstractContainerMenu> consumer) {
        synchronized (OPEN_MENUS) {
            for (Map.Entry<AbstractContainerMenu, ServerPlayer> entry : OPEN_MENUS.entrySet()) {
                consumer.accept(entry.getValue(), entry.getKey());
            }
        }
    }
}
