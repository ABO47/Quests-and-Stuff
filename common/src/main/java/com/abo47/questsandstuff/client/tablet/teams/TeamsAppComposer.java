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

        int bodyH = chapterHeight(state);
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, initialRootW - BODY_X - ROOT_PAD_X, bodyH);

        int homeBtnX = initialRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((initialRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.openTabletUiFromItem(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        homeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        root.addWidgets(rootFill, mainPanel);
        return root;
    }
}
