package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasContextEdgeActions {
    private CanvasContextEdgeActions() {
    }

    static void addEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.EDGE || state.contextEdgeSource.isBlank() || state.contextEdgeTarget.isBlank()) {
            return;
        }
        boolean direct = CanvasRenderer.isConnectionDirect(state, selectedGroup, state.contextEdgeSource, state.contextEdgeTarget);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.runConnectionModeAction(player, state.contextEdgeTarget, state.contextEdgeSource, direct);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_connection_mode source={} target={} direct={}", state.contextEdgeSource, state.contextEdgeTarget, !direct);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
            int color = CanvasRenderer.connectionColor(state, selectedGroup, state.contextEdgeSource, state.contextEdgeTarget);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedGroup, state.contextEdgeSource, state.contextEdgeTarget), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_color source={} target={}", state.contextEdgeSource, state.contextEdgeTarget);
            canvasViewport.refresh();
        }));
        boolean hidden = CanvasRenderer.isConnectionHidden(state, selectedGroup, state.contextEdgeSource, state.contextEdgeTarget);
        actions.add(new ContextAction(hidden ? CanvasContextMenuController.tr("ui.questsandstuff.context.show_connection") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_connection"), hidden ? "eye" : "eye-off", hidden ? ModColors.INTERACTIVE : ModColors.WARNING, () -> {
            EditorCommandClient.runConnectionHiddenAction(player, state.contextEdgeTarget, state.contextEdgeSource, !hidden);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_hidden source={} target={} hidden={}", state.contextEdgeSource, state.contextEdgeTarget, !hidden);
            canvasViewport.refresh();
        }));
    }
}
