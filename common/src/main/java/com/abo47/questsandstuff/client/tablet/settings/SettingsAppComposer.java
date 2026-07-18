package com.abo47.questsandstuff.client.tablet.settings;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.bootstrap.TabletBootstrap;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.BODY_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.refreshActiveTablet;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.setActiveTabletState;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.rootWidth;

public final class SettingsAppComposer {
    private static final int HOME_BTN_SIZE = GRID_10;
    private static final int CONTENT_INSET = 6;
    private static final int TAB_H = GRID_20;
    private static final int TAB_GAP = GRID_4;
    private static final int TAB_ENLARGE = GRID_4;
    private static final int HEADER_LIST_GAP = 5;
    private static final int SEARCH_INSET = GRID_9;
    private static final int LIST_INSET = GRID_9;
    private static final int LIST_INNER_PAD = GRID_8;

    private SettingsAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletBootstrap.prepare(player);
        TabletLifecycle.rememberMainWindow();
        state.root.currentApp = "settings";
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        root.setBackground(fullScreenMode
                ? SurfaceFactory.transparent()
                : SurfaceFactory.transparentBorder(TabletColors.BORDER_BASE));

        int bodyH = chapterHeight(state);
        int bodyW = initialRootW - ROOT_PAD_X * 2;
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, bodyW, bodyH, state);

        int homeBtnX = initialRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((initialRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.subtleBorder()),
                cd -> TabletLifecycle.openTabletUiHome(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(GlowShaderHelper.hoverGlow());
        homeBtn.setClickedTexture(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        int headerTop = CONTENT_INSET;
        SettingsAppHeaderControls headers = SettingsAppHeaderControls.create(state, () -> refreshActiveTablet(), headerTop, bodyW);

        int searchY = headerTop + TAB_H + TAB_ENLARGE + TAB_GAP;
        int listY = searchY + HEADER_H + HEADER_LIST_GAP;
        int listH = Math.max(1, bodyH - listY - LIST_INSET);
        WidgetGroup optionsPanel = new WidgetGroup(SEARCH_INSET, listY, bodyW - SEARCH_INSET * 2, listH);
        optionsPanel.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            int crw = rootWidth(state);
            int crh = rootHeight(state);
            root.setSize(crw, crh);
            TabletRootWidget.refreshRootBackground(root, state);

            int cbw = crw - ROOT_PAD_X * 2;
            int cbh = chapterHeight(state);
            mainPanel.setSize(cbw, cbh);

            int hbx = crw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int hby = ROOT_PAD_Y + ((crh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(hbx, hby);
            homeBtn.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.subtleBorder()));

            int cSearchY = CONTENT_INSET + TAB_H + TAB_ENLARGE + TAB_GAP;
            int cListY = cSearchY + HEADER_H + HEADER_LIST_GAP;
            int cListH = Math.max(1, cbh - cListY - LIST_INSET);
            optionsPanel.setSize(cbw - SEARCH_INSET * 2, cListH);
            optionsPanel.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
            mainPanel.setBackground(IGuiTexture.EMPTY);

            headers.layout(CONTENT_INSET, cbw);
            SettingsOptionsPanelRenderer.rebuildOptions(optionsPanel, state, refresh[0], LIST_INNER_PAD, LIST_INNER_PAD, cbw - LIST_INSET * 2 - LIST_INNER_PAD * 2, cListH - LIST_INNER_PAD * 2);

            SkinAnchorRegistry.register("root", root);
            SkinAnchorRegistry.register("home_btn", homeBtn);
            SkinAnchorRegistry.register("settings_main_panel", mainPanel);
            SkinAnchorRegistry.register("settings_options", optionsPanel);
            SkinAnchorRegistry.register("settings_header_search", headers.searchField());
            SkinAnchorRegistry.register("settings_tab_layer", headers.tabLayer());
            SkinEditManager.reapplyOverrides(state, root);
        };

        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidget(mainPanel);
        headers.addTo(mainPanel);
        mainPanel.addWidget(optionsPanel);
        root.addWidget(modalLayer);

        refresh[0].run();
        return root;
    }
}
