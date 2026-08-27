package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.controls.TwoFieldEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;

final class CanvasContextMenuRenderer {
    private CanvasContextMenuRenderer() {
    }

    static void renderCanvasContextMenu(CanvasViewport canvasViewport, TabletUiState state) {
        if (!ContextMenuController.isOpen(state)) {
            ContextMenuController.resetClosedMetrics(state);
            return;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            ContextMenuController.close(state);
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
        ContextMenuController.setLayout(state, menuX, menuY, menuW, menuH, rowCount, scrollMax);

        canvasViewport.addWidget(TabletUiFactory.flatHitButton(0, 0, canvasViewport.getSize().width, canvasViewport.getSize().height, click -> close(state)));
        WidgetGroup menu = ContextMenuPanel.build(menuX, menuY, menuW, actions, state.contextMenu.contextMenuScroll, visibleRows, TabletColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                close(state);
            }
        }, canvasViewport.getSize().width, canvasViewport.getSize().height, ContextMenuController.scrollState(state), canvasViewport::refresh);
        canvasViewport.addWidget(menu);

        if (state.contextMenu.modeEditorOpen) {
            int w = 240;
            int h = 116;
            int x = Math.max(4, (canvasViewport.getSizeWidth() - w) / 2);
            int y = Math.max(4, (canvasViewport.getSizeHeight() - h) / 2);
            String mode = state.contextMenu.modeEditorMode;
            boolean tile = "tile".equals(mode);
            String title = tile ? "ui.questsandstuff.skin.mode_tile_size" : "ui.questsandstuff.skin.mode_hrstretch";
            String leftKey = tile ? "ui.questsandstuff.skin.tile_size_w" : "ui.questsandstuff.skin.hrstretch_left";
            String rightKey = tile ? "ui.questsandstuff.skin.tile_size_h" : "ui.questsandstuff.skin.hrstretch_right";
            WidgetGroup popup = TwoFieldEditor.build(state, x, y, w, h, title, leftKey, rightKey,
                    state.contextMenu.modeEditorLeft, state.contextMenu.modeEditorRight,
                    (l, r) -> {
                        Player player = Minecraft.getInstance().player;
                        if (player != null) {
                            TabletUiFactory.runChapterAction(player, state, "set_canvas_background", state.contextMenu.modeEditorTarget,
                                    new SkinFillOverride(mode, l, r, state.contextMenu.modeEditorPath).encode(), 0);
                        }
                        state.contextMenu.modeEditorOpen = false;
                        canvasViewport.refresh();
                    },
                    () -> {
                        state.contextMenu.modeEditorOpen = false;
                        canvasViewport.refresh();
                    });
            canvasViewport.addWidget(popup);
        }
    }

    private static void close(TabletUiState state) {
        ContextMenuController.close(state);
    }
}
