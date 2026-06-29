package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.SkinEditOverlay;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;

final class TabletHomeComposer {
    private TabletHomeComposer() {
    }

    static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    static WidgetGroup create(Player player, int rootWidth, int rootHeight, boolean fullScreenMode) {
        UiThemeManager.activeThemeName();
        TabletUiState state = new TabletUiState();
        state.root.editorAvailable = player.hasPermissions(2);
        TabletUiFactory.setActiveTabletState(state);
        TabletUiFactory.setActiveTabletRefresh(() -> {});
        int safeRootW = Math.max(1, rootWidth);
        int safeRootH = Math.max(1, rootHeight);

        WidgetGroup root = new WidgetGroup(0, 0, safeRootW, safeRootH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                if (state.root.skinEditMode) {
                    SkinEditOverlay.draw(graphics, this, mouseX, mouseY);
                }
            }

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
                if (TabletClientHooks.toggleSkinEditMatches(keyCode, scanCode)) {
                    state.root.skinEditMode = !state.root.skinEditMode;
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        root.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));
        root.addWidget(new TabletHomeOverviewPanel(0, 0, safeRootW, safeRootH));
        return root;
    }
}
