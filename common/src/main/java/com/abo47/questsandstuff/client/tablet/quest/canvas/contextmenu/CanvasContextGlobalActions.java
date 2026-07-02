package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasContextGlobalActions {
    private CanvasContextGlobalActions() {
    }

    static void addGlobalActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.canvas.canvasZoom != 1.0f) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_zoom"), "reset_zoom", TabletColors.INTERACTIVE, () -> {
                CanvasCameraController.resetZoom(state, true);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_zoom");
                canvasViewport.refresh();
            }));
        }
        if (!selectedGroup.isBlank() && CanvasClipboardController.hasClipboardContent(state)) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.paste"), "paste", TabletColors.SUCCESS, () -> {
                CanvasContextMenuSupport.pasteClipboard(player, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=paste target={}", state.contextMenu.contextMenuTarget);
                canvasViewport.refresh();
            }));
        }
    }
}
