package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

final class CanvasContextMenuRenderer {
    private CanvasContextMenuRenderer() {
    }

    static void renderCanvasContextMenu(CanvasViewport canvasViewport, TabletUiState state) {
        if (!state.contextMenuOpen) {
            state.contextMenuRows = 0;
            state.contextMenuScroll = 0;
            state.contextMenuScrollMax = 0;
            return;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextMenuScroll = 0;
            state.contextMenuScrollMax = 0;
            state.contextDeleteConfirmKey = "";
            return;
        }
        state.contextMenuRows = actions.size();

        int menuW = CanvasContextMenuSupport.contextMenuWidth(actions, canvasViewport.getSize().width);
        int maxVisibleRows = CanvasContextMenuSupport.maxContextVisibleRows(canvasViewport);
        int visibleRows = Math.max(1, Math.min(actions.size(), maxVisibleRows));
        state.contextMenuScrollMax = Math.max(0, actions.size() - visibleRows);
        state.contextMenuScroll = ScrollController.clamp(state.contextMenuScroll, state.contextMenuScrollMax);
        int menuH = ContextMenuPanel.heightForRows(visibleRows);
        int menuX = Math.max(4, Math.min(state.contextMenuX, canvasViewport.getSize().width - menuW - 4));
        int menuY = Math.max(4, Math.min(state.contextMenuY, canvasViewport.getSize().height - menuH - 4));
        state.contextMenuX = menuX;
        state.contextMenuY = menuY;
        state.contextMenuWidthPx = menuW;
        state.contextMenuHeightPx = menuH;

        WidgetGroup menu = ContextMenuPanel.build(menuX, menuY, menuW, actions, state.contextMenuScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                state.contextMenuOpen = false;
                state.contextMenuRows = 0;
                state.contextMenuScroll = 0;
                state.contextMenuScrollMax = 0;
                state.contextDeleteConfirmKey = "";
            }
        });
        canvasViewport.addWidget(menu);
    }
}
