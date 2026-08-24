package com.abo47.questsandstuff.client.quest.lock;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public final class ClientCrafterLookup {
    private ClientCrafterLookup() {
    }

    public static Player byGrid(Container grid) {
        if (grid == null) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.containerMenu == null) {
            return null;
        }
        for (var slot : player.containerMenu.slots) {
            if (slot != null && slot.container == grid) {
                return player;
            }
        }
        return null;
    }
}
