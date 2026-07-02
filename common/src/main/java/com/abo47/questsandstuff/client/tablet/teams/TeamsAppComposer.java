package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.panel.ModalPanelRouter;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.shell.TabletShellBootstrap;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamCreatePacket;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

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

public final class TeamsAppComposer {
    private static final int HOME_BTN_SIZE = 10;
    private static final int CONTENT_INSET = 6;
    private static final int LIST_INSET = 9;
    private static final int GUTTER = 6;
    private static final int HEADER_LIST_GAP = 5;

    private TeamsAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletShellBootstrap.prepare(player);
        TabletClientHooks.rememberMainWindow();
        state.root.currentApp = "teams";
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        root.setBackground(fullScreenMode
                ? Surfaces.transparent()
                : Surfaces.transparentBorder(ModColors.BORDER_BASE));

        WidgetGroup rootFill = new WidgetGroup(0, 0, initialRootW, initialRootH);
        rootFill.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));

        int bodyH = chapterHeight(state);
        int bodyW = initialRootW - ROOT_PAD_X * 2;
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, bodyW, bodyH, state);

        int homeBtnX = initialRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((initialRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.openTabletUiHome(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        homeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        final int headerY = CONTENT_INSET;
        TeamsAppHeaderControls headers = TeamsAppHeaderControls.create(state, () -> refreshActiveTablet(), headerY, bodyW);

        int listY = headerY + HEADER_H + HEADER_LIST_GAP;
        int listH = Math.max(1, bodyH - listY - GUTTER);
        WidgetGroup memberListPanel = new WidgetGroup(LIST_INSET, listY, bodyW - LIST_INSET * 2, listH);
        memberListPanel.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        if (ClientTeamCache.INSTANCE.getTeam() == null) {
            ModNetwork.sendToServer(new C2STeamCreatePacket());
        }

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            int crw = rootWidth(state);
            int crh = rootHeight(state);
            root.setSize(crw, crh);

            int cbw = crw - ROOT_PAD_X * 2;
            int cbh = chapterHeight(state);
            mainPanel.setSize(cbw, cbh);

            int hbx = crw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int hby = ROOT_PAD_Y + ((crh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(hbx, hby);

            headers.layout(headerY, cbw);

            int clY = headerY + HEADER_H + HEADER_LIST_GAP;
            int clH = Math.max(1, cbh - clY - GUTTER);
            memberListPanel.setSize(cbw - LIST_INSET * 2, clH);
memberListPanel.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            mainPanel.setBackground((IGuiTexture) null);

            ClientTeamCache.JoinResult joinResult = ClientTeamCache.INSTANCE.takePendingJoinResult();
            if (joinResult != null) {
                state.teams.inviteCodeMessage = joinResult.message();
                state.teams.inviteCodeMessageSuccess = joinResult.success();
                if (joinResult.success()) {
                    state.teams.inviteCodeModalOpen = false;
                }
            }

            if (!ModalStateQueries.anyOpen(state) && !state.modal.modalWindowClosing) {
                modalLayer.clearAllWidgets();
                TeamsInviteCodeModal.rebuild(modalLayer, state, refresh[0], crw, crh);
                TeamsConfirmModal.rebuild(modalLayer, state, refresh[0], crw, crh);
            }
            ModalPanelRouter.rebuildChapterModal(modalLayer, state, player, refresh[0]);
            TeamsMemberCardRenderer.rebuildMemberList(memberListPanel, state, refresh[0]);
            SkinAnchorRegistry.register("root", root);
            SkinAnchorRegistry.register("teams_member_list", memberListPanel);
            SkinEditManager.reapplyOverrides(state, root);
        };

        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidgets(rootFill, mainPanel);
        headers.addTo(mainPanel);
        mainPanel.addWidget(memberListPanel);
        root.addWidget(modalLayer);

        refresh[0].run();
        return root;
    }
}
