package com.abo47.questsandstuff.client.tablet.app;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

@FunctionalInterface
public interface AppComposer {
    WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode);
}
