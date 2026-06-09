package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.confirmDeleteClick;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.pendingDeleteLabel;

final class CanvasContextGlobalActions {
    private CanvasContextGlobalActions() {
    }

    static void addGlobalActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.canvas.canvasZoom != 1.0f) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_zoom"), "reset_zoom", ModColors.INTERACTIVE, () -> {
                CanvasCameraController.resetZoom(state, true);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_zoom");
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.copy"), "copy", ModColors.INTERACTIVE, true, true, () -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=copy target={} selected={}", state.contextMenu.contextMenuTarget, state.canvas.canvasSelection.questIds().size());
                canvasViewport.refresh();
            }));
        }
        if (!selectedGroup.isBlank() && CanvasClipboardController.hasClipboardContent(state)) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.paste"), "paste", ModColors.SUCCESS, () -> {
                CanvasContextMenuSupport.pasteClipboard(player, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=paste target={}", state.contextMenu.contextMenuTarget);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            boolean confirmingDelete = deleteKey.equals(state.contextMenu.contextDeleteConfirmKey);
            String deleteFallback = CanvasContextMenuController.tr("ui.questsandstuff.context.delete");
            String deleteLabel = pendingDeleteLabel(state, deleteKey, deleteFallback);
            actions.add(new ContextAction(deleteLabel, "delete", ModColors.ERROR, confirmingDelete, true, () -> {
                if (!confirmDeleteClick(state, deleteKey)) {
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=delete_arm target={}", state.contextMenu.contextMenuTarget);
                    canvasViewport.refresh();
                    return;
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=delete_confirm target={}", state.contextMenu.contextMenuTarget);
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }
}
