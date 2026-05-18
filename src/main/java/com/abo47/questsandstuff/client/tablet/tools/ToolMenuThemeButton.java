package com.abo47.questsandstuff.client.tablet.tools;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.tools.TabletToolButtons.addIcon;
import static com.abo47.questsandstuff.client.tablet.tools.TabletToolButtons.hit;

final class ToolMenuThemeButton {
    private ToolMenuThemeButton() {
    }

    static void add(WidgetGroup menu, TabletUiState state, Runnable refresh, int x, int y, int size, int border) {
        menu.addWidget(panel(x, y, size, size, withAlpha(ModColors.SURFACE_PANEL_ALT, 164), border));
        addIcon(menu, x, y, size, "themes", ModColors.INTERACTIVE);
        ButtonWidget hit = hit(x, y, size, new Component[]{Component.literal("Themes")}, () -> {
            ModalOpenActions.openThemePicker(state);
            state.toolsMenuOpen = false;
            state.questDetailsToolsOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] theme picker open");
            refresh.run();
        });
        menu.addWidget(hit);
    }
}
