package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;

final class CanvasContextMenuRenderer {
    private CanvasContextMenuRenderer() {
    }

    static void renderCanvasContextMenu(CanvasViewport canvasViewport, TabletUiState state) {
        if (!ContextMenuState.isOpen(state)) {
            ContextMenuState.resetClosedMetrics(state);
            return;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            ContextMenuState.close(state);
            return;
        }
        int rowCount = ContextMenuPanel.rowActionCount(actions);

        int menuW = CanvasContextMenuSupport.contextMenuWidth(actions, canvasViewport.getSize().width);
        int maxVisibleRows = CanvasContextMenuSupport.maxContextVisibleRows(canvasViewport);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, maxVisibleRows);
        while (visibleRows > 0 && ContextMenuPanel.heightFor(actions, visibleRows) > canvasViewport.getSize().height - 8) {
            visibleRows--;
        }
        int scrollMax = Math.max(0, rowCount - visibleRows);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int menuX = ContextMenuPlacement.fitRightOrLeft(state.contextMenu.contextMenuAnchorX, canvasViewport.getSize().width, menuW);
        int menuY = ContextMenuPlacement.fitBelowOrAbove(state.contextMenu.contextMenuAnchorY, canvasViewport.getSize().height, menuH);
        ContextMenuState.setLayout(state, menuX, menuY, menuW, menuH, rowCount, scrollMax);

        canvasViewport.addWidget(TabletUiFactory.flatHitButton(0, 0, canvasViewport.getSize().width, canvasViewport.getSize().height, click -> close(state)));
        WidgetGroup menu = ContextMenuPanel.build(menuX, menuY, menuW, actions, state.contextMenu.contextMenuScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                close(state);
            }
        }, canvasViewport.getSize().width, canvasViewport.getSize().height, ContextMenuState.scrollState(state), canvasViewport::refresh);
        canvasViewport.addWidget(menu);
    }

    private static void close(TabletUiState state) {
        ContextMenuState.close(state);
    }
}
