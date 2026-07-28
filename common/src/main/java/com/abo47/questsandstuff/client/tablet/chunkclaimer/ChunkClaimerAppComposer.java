package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.bootstrap.TabletBootstrap;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.chunkclaim.C2SChunkClaimActionPacket;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors.BORDER_ACCENT;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors.BORDER_BASE;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors.SURFACE_BASE;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors.SURFACE_PANEL_ALT;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors.subtleBorder;
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

public final class ChunkClaimerAppComposer {
    private static final int HOME_BTN_SIZE = GRID_10;
    private static final int CONTENT_INSET = GRID_6;
    private static final int LIST_INSET = GRID_9;
    private static final int GUTTER = GRID_6;
    private static final int HEADER_LIST_GAP = GRID_5;

    private ChunkClaimerAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletBootstrap.prepare(player);
        TabletLifecycle.rememberMainWindow();
        state.root.currentApp = "chunkclaimer";
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int crw = rootWidth(state);
        int crh = rootHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, crw, crh, state);
        root.setBackground(fullScreenMode
                ? SurfaceFactory.transparent()
                : SurfaceFactory.transparentBorder(BORDER_BASE));

        int bodyH = chapterHeight(state);
        int bodyW = crw - ROOT_PAD_X * 2;
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, bodyW, bodyH, state);

        int homeBtnX = crw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((crh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(SURFACE_PANEL_ALT, subtleBorder()),
                cd -> TabletLifecycle.openTabletUiHome(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper.hoverGlow());
        homeBtn.setClickedTexture(SurfaceFactory.bordered(SURFACE_PANEL_ALT, BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        int headerY = CONTENT_INSET;
        ChunkClaimerHeaderControls headers = ChunkClaimerHeaderControls.create(state, () -> refreshActiveTablet(), headerY, bodyW);

        int listY = headerY + HEADER_H + HEADER_LIST_GAP;
        int listH = Math.max(1, bodyH - listY - GUTTER);
        WidgetGroup mapPanel = new WidgetGroup(LIST_INSET, listY, bodyW - LIST_INSET * 2, listH);
        mapPanel.setBackground(SurfaceFactory.bordered(SURFACE_BASE, BORDER_BASE));
        ChunkMapWidget map = new ChunkMapWidget(0, 0, mapPanel.getSizeWidth(), mapPanel.getSizeHeight(), state);
        mapPanel.addWidget(map);

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, crw, crh, state, () -> refresh[0].run());

        ModNetwork.sendToServer(requestSync(player));

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            int rcrw = rootWidth(state);
            int rcrh = rootHeight(state);
            root.setSize(rcrw, rcrh);
            TabletRootWidget.refreshRootBackground(root, state);

            int rcbw = rcrw - ROOT_PAD_X * 2;
            int rcbh = chapterHeight(state);
            mainPanel.setSize(rcbw, rcbh);

            int hbx = rcrw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int hby = ROOT_PAD_Y + ((rcrh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(hbx, hby);
            homeBtn.setBackground(SurfaceFactory.bordered(SURFACE_PANEL_ALT, subtleBorder()));

            mainPanel.setBackground((IGuiTexture) null);

            headers.layout(headerY, rcbw);
            headers.updateCount();

            int rListY = headerY + HEADER_H + HEADER_LIST_GAP;
            int rListH = Math.max(1, rcbh - rListY - GUTTER);
            mapPanel.setSize(rcbw - LIST_INSET * 2, rListH);
            map.setSize(mapPanel.getSizeWidth(), mapPanel.getSizeHeight());

            SkinAnchorRegistry.register("root", root);
            SkinAnchorRegistry.register("home_btn", homeBtn);
            SkinAnchorRegistry.register("chunkclaimer_main_panel", mainPanel);
            SkinAnchorRegistry.register("chunkclaimer_claim_btn", headers.claimBtn());
            SkinAnchorRegistry.register("chunkclaimer_force_btn", headers.forceBtn());
            SkinAnchorRegistry.register("chunkclaimer_grid_btn", headers.gridBtn());
            SkinAnchorRegistry.register("chunkclaimer_scan_btn", headers.scanBtn());
            SkinAnchorRegistry.register("chunkclaimer_opacity_btn", headers.opacityBtn());
            SkinAnchorRegistry.register("chunkclaimer_count_label", headers.countLabel());
            SkinAnchorRegistry.register("chunkclaimer_map", mapPanel);
            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            SkinEditManager.reapplyOverrides(state, root);
        };

        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidget(mainPanel);
        headers.addTo(mainPanel);
        mainPanel.addWidget(mapPanel);
        root.addWidget(modalLayer);

        refresh[0].run();
        return root;
    }

    private static C2SChunkClaimActionPacket requestSync(Player player) {
        var mcPlayer = Minecraft.getInstance().player;
        ResourceLocation dim;
        int cx;
        int cz;
        if (mcPlayer != null) {
            dim = mcPlayer.level().dimension().location();
            cx = mcPlayer.blockPosition().getX() >> 4;
            cz = mcPlayer.blockPosition().getZ() >> 4;
        } else {
            dim = new ResourceLocation("minecraft", "overworld");
            cx = 0;
            cz = 0;
        }
        return new C2SChunkClaimActionPacket(C2SChunkClaimActionPacket.Action.REQUEST, dim, cx, cz);
    }
}
