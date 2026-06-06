package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.PANEL_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.PANEL_INSET;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;

final class TabletHomeComposer {
    private static final int DESIGN_W = ROOT_W;
    private static final int DESIGN_H = ROOT_H;
    private static final int SCREEN_MARGIN = 12;

    private TabletHomeComposer() {
    }

    static WidgetGroup create(Player player) {
        int[] size = fittedTabletSize(ROOT_W, ROOT_H, true);
        return create(player, size[0], size[1], false);
    }

    static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        UiThemeManager.activeThemeName();
        TabletUiFactory.setActiveTabletState(null);
        TabletUiFactory.setActiveTabletRefresh(null);
        int safeRootW = Math.max(1, rootWidth);
        int safeRootH = Math.max(1, rootHeight);
        int[] windowSize = fittedTabletSize(safeRootW, safeRootH, false);
        int windowX = Math.max(0, (safeRootW - windowSize[0]) / 2);
        int windowY = Math.max(0, (safeRootH - windowSize[1]) / 2);

        WidgetGroup root = new WidgetGroup(0, 0, safeRootW, safeRootH) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (TabletClientHooks.openQuestsUiMatches(keyCode, scanCode)) {
                    TabletClientHooks.openQuestsUiFromCurrentScreen();
                    return true;
                }
                if (TabletClientHooks.openUiMatches(keyCode, scanCode)) {
                    TabletClientHooks.closeTabletUi(null, true, "home_keybind");
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    TabletClientHooks.closeTabletUi(null, false, "home_escape");
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        root.setBackground(Surfaces.fill(0x00000000));
        root.addWidget(new TabletHomeOverviewPanel(windowX, windowY, windowSize[0], windowSize[1]));
        return root;
    }

    static int targetRootWidth(int screenWidth, int screenHeight) {
        return fittedTabletSize(screenWidth, screenHeight, true)[0];
    }

    static int targetRootHeight(int screenWidth, int screenHeight) {
        return fittedTabletSize(screenWidth, screenHeight, true)[1];
    }

    static int contentInset(int windowWidth) {
        return PANEL_INSET;
    }

    static int statusBarHeight(int windowHeight) {
        return HEADER_H;
    }

    static int contentGap() {
        return PANEL_GAP;
    }

    private static int[] fittedTabletSize(int screenWidth, int screenHeight, boolean reserveScreenMargin) {
        int margin = reserveScreenMargin ? fitMargin(screenWidth, screenHeight) : 0;
        int availableW = Math.max(1, screenWidth);
        int availableH = Math.max(1, screenHeight);
        if (margin > 0) {
            availableW = Math.max(1, availableW - margin * 2);
            availableH = Math.max(1, availableH - margin * 2);
        }
        double scale = Math.min(1.0, Math.min((double) availableW / DESIGN_W, (double) availableH / DESIGN_H));
        int width = Math.max(1, (int) Math.round(DESIGN_W * scale));
        int height = Math.max(1, (int) Math.round(DESIGN_H * scale));
        return new int[]{Math.min(width, availableW), Math.min(height, availableH)};
    }

    private static int fitMargin(int screenWidth, int screenHeight) {
        int shortest = Math.min(Math.max(1, screenWidth), Math.max(1, screenHeight));
        return Math.min(SCREEN_MARGIN, Math.max(0, (shortest - 1) / 2));
    }
}
