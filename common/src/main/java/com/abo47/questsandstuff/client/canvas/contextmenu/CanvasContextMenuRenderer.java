package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
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
            state.contextQuestCompletionSoundMenuOpen = false;
            return;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextMenuScroll = 0;
            state.contextMenuScrollMax = 0;
            state.contextDeleteConfirmKey = "";
            state.contextQuestCompletionSoundMenuOpen = false;
            return;
        }
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        state.contextMenuRows = rowCount;

        int menuW = CanvasContextMenuSupport.contextMenuWidth(actions, canvasViewport.getSize().width);
        int maxVisibleRows = CanvasContextMenuSupport.maxContextVisibleRows(canvasViewport);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, maxVisibleRows);
        while (visibleRows > 0 && ContextMenuPanel.heightFor(actions, visibleRows) > canvasViewport.getSize().height - 8) {
            visibleRows--;
        }
        state.contextMenuScrollMax = Math.max(0, rowCount - visibleRows);
        state.contextMenuScroll = ScrollController.clamp(state.contextMenuScroll, state.contextMenuScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int menuX = ContextMenuPlacement.fitRightOrLeft(state.contextMenuAnchorX, canvasViewport.getSize().width, menuW);
        int menuY = ContextMenuPlacement.fitBelowOrAbove(state.contextMenuAnchorY, canvasViewport.getSize().height, menuH);
        state.contextMenuX = menuX;
        state.contextMenuY = menuY;
        state.contextMenuWidthPx = menuW;
        state.contextMenuHeightPx = menuH;

        canvasViewport.addWidget(TabletUiFactory.flatHitButton(0, 0, canvasViewport.getSize().width, canvasViewport.getSize().height, click -> close(state)));
        WidgetGroup menu = ContextMenuPanel.build(menuX, menuY, menuW, actions, state.contextMenuScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                close(state);
            }
        }, canvasViewport.getSize().width, canvasViewport.getSize().height);
        canvasViewport.addWidget(menu);
    }

    private static void close(TabletUiState state) {
        state.contextMenuOpen = false;
        state.contextMenuRows = 0;
        state.contextMenuScroll = 0;
        state.contextMenuScrollMax = 0;
        state.contextDeleteConfirmKey = "";
        state.contextQuestCompletionSoundMenuOpen = false;
    }
}
