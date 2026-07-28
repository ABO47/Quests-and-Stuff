package com.abo47.questsandstuff.client.tablet.ui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class IntegratedServerActions {
    private IntegratedServerActions() {
    }

    @FunctionalInterface
    public interface LocalAction {
        void run(ServerPlayer serverPlayer);
    }

    public static boolean canRunLocally(Player player) {
        return player instanceof ServerPlayer;
    }

    public static void run(Player player, LocalAction localAction, Runnable remoteAction) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (localAction != null) {
                localAction.run(serverPlayer);
            }
            return;
        }
        if (remoteAction != null) {
            remoteAction.run();
        }
    }
}
