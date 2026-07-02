package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.modal.ModalLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedSkinState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;

final class TabletHomeComposer {
    private TabletHomeComposer() {
    }

    static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        UiThemeManager.activeThemeName();
        TabletUiState state = new TabletUiState();
        state.root.currentApp = "home";
        TabletUiFactory.setActiveTabletState(state);
        int safeRootW = Math.max(1, rootWidth);
        int safeRootH = Math.max(1, rootHeight);

        TabletRootWidget root = new TabletRootWidget(0, 0, safeRootW, safeRootH, state);
        root.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalLayerWidget(0, 0, safeRootW, safeRootH, state, () -> refresh[0].run());
        WidgetGroup homePanel = new TabletHomeOverviewPanel(0, 0, safeRootW, safeRootH);

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            Widget inner = SkinEditManager.findWidgetByKey(root, "home_inner");
            if (inner != null) inner.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            IGuiTexture rootOverrideTex = TabletRootWidget.resolveRootFill(state);
            if (rootOverrideTex != null && inner != null) {
                inner.setBackground(rootOverrideTex);
            }
            SkinAnchorRegistry.register("root", root);
            SkinAnchorRegistry.register("home_inner", homePanel);
            SkinEditManager.reapplyOverrides(state, root);
        };
        TabletUiFactory.setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidget(homePanel);
        root.addWidget(modalLayer);
        readPersistedUiState(state);
        readPersistedSkinState(state);
        SkinEditManager.reapplyOverrides(state, root);
        return root;
    }
}
