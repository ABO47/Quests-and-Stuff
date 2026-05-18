package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
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
        if (state.canvasZoom != 1.0f) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_zoom"), "reset_zoom", ModColors.INTERACTIVE, () -> {
                state.canvasZoom = 1.0f;
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_zoom");
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.copy"), "copy", ModColors.INTERACTIVE, () -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=copy target={} selected={}", state.contextMenuTarget, state.selectedQuestIds.size());
                canvasViewport.refresh();
            }));
        }
        if (!selectedGroup.isBlank() && CanvasClipboardController.hasClipboardContent(state)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.paste"), "paste", ModColors.SUCCESS, () -> {
                CanvasContextMenuSupport.pasteClipboard(player, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=paste target={}", state.contextMenuTarget);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            boolean confirmingDelete = deleteKey.equals(state.contextDeleteConfirmKey);
            String deleteFallback = CanvasContextMenuController.tr("ui.questsandstuff.context.delete");
            String deleteLabel = pendingDeleteLabel(state, deleteKey, deleteFallback);
            actions.add(new ContextAction(deleteLabel, "delete", ModColors.ERROR, confirmingDelete, () -> {
                if (!confirmDeleteClick(state, deleteKey)) {
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=delete_arm target={}", state.contextMenuTarget);
                    canvasViewport.refresh();
                    return;
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=delete_confirm target={}", state.contextMenuTarget);
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }
}
