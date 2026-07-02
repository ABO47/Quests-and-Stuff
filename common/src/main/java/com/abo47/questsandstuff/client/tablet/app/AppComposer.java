package com.abo47.questsandstuff.client.tablet.app;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface AppComposer {
    WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode);
}
