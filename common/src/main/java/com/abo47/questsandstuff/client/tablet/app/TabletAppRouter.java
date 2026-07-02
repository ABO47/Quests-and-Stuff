package com.abo47.questsandstuff.client.tablet.app;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

public final class TabletAppRouter {
    private TabletAppRouter() {
    }

    public static WidgetGroup create(String appId, Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        AppDescriptor app = TabletAppRegistry.get(appId);
        if (app == null) {
            return null;
        }
        return app.composer().create(player, rootWidth, rootHeight, fullScreenMode);
    }
}
