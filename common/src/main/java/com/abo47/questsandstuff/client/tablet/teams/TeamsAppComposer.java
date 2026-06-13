package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.shell.TabletShellBootstrap;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootWidth;

public final class TeamsAppComposer {
    private static final int HOME_BTN_SIZE = 10;

    private TeamsAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletShellBootstrap.prepare(player);
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        root.setBackground(fullScreenMode
                ? Surfaces.transparent()
                : Surfaces.transparentBorder(ModColors.BORDER_BASE));

        WidgetGroup rootFill = new WidgetGroup(0, 0, initialRootW, initialRootH);
        rootFill.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));

        Runnable[] refresh = new Runnable[1];
        WidgetGroup[] splitterRef = new WidgetGroup[1];
        TeamsSplitterWidget[] splitterInstance = new TeamsSplitterWidget[1];

        int leftW = state.teams.leftPanelWidth;
        int bodyH = chapterHeight(state);
        WidgetGroup leftPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, leftW, bodyH);
        WidgetGroup[] leftPanelRef = new WidgetGroup[]{leftPanel};

        int rightX = SplitPanelLayout.rightPanelX(BODY_X, leftW);
        int rightW = initialRootW - rightX - ROOT_PAD_X;
        WidgetGroup rightPanel = SplitPanelLayout.leftPanel(rightX, BODY_Y, rightW, bodyH);
        WidgetGroup[] rightPanelRef = new WidgetGroup[]{rightPanel};

        ButtonWidget homeBtn = new ButtonWidget(0, 0, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.openTabletUiFromItem(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        homeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        refresh[0] = () -> {
            root.setBackground(fullScreenMode
                    ? Surfaces.transparent()
                    : Surfaces.transparentBorder(ModColors.BORDER_BASE));

            int currentRootW = rootWidth(state);
            int currentRootH = rootHeight(state);
            int currentBodyH = chapterHeight(state);
            root.setSize(currentRootW, currentRootH);
            rootFill.setSize(currentRootW, currentRootH);

            int currentLeftW = splitterInstance[0] != null ? splitterInstance[0].getLeftPanelWidth() : state.teams.leftPanelWidth;
            int currentRightX = SplitPanelLayout.rightPanelX(BODY_X, currentLeftW);
            int currentRightW = currentRootW - currentRightX - ROOT_PAD_X;

            leftPanelRef[0].setSelfPosition(BODY_X, BODY_Y);
            leftPanelRef[0].setSize(currentLeftW, currentBodyH);

            rightPanelRef[0].setSelfPosition(currentRightX, BODY_Y);
            rightPanelRef[0].setSize(currentRightW, currentBodyH);

            if (splitterRef[0] != null) {
                int splitterX = SplitPanelLayout.splitterX(BODY_X, currentLeftW);
                splitterRef[0].setSelfPosition(splitterX, BODY_Y);
                splitterRef[0].setSize(SPLITTER_W, currentBodyH);
            }

            int homeBtnX = currentRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int homeBtnY = ROOT_PAD_Y + ((currentRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(homeBtnX, homeBtnY);
        };

        root.setRefresher(refresh[0]);

        TeamsSplitterWidget splitter = new TeamsSplitterWidget(BODY_X, state, refresh[0]);
        splitterInstance[0] = splitter;
        splitterRef[0] = splitter;

        root.addWidgets(rootFill, leftPanel, splitter, rightPanel);
        refresh[0].run();
        return root;
    }
}
