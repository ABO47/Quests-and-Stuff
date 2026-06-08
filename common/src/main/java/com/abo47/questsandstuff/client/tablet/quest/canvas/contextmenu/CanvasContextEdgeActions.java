package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
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
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.EDGE || state.contextMenu.contextEdgeSource.isBlank() || state.contextMenu.contextEdgeTarget.isBlank()) {
            return;
        }
        boolean direct = CanvasRenderer.isConnectionDirect(state, selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.runConnectionModeAction(player, state.contextMenu.contextEdgeTarget, state.contextMenu.contextEdgeSource, direct);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_connection_mode source={} target={} direct={}", state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, !direct);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
            int color = CanvasRenderer.connectionColor(state, selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_color source={} target={}", state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
            canvasViewport.refresh();
        }));
        boolean hidden = CanvasRenderer.isConnectionHidden(state, selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        actions.add(new ContextAction(hidden ? CanvasContextMenuController.tr("ui.questsandstuff.context.show_connection") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_connection"), hidden ? "eye" : "eye-off", hidden ? ModColors.INTERACTIVE : ModColors.WARNING, () -> {
            EditorCommandClient.runConnectionHiddenAction(player, state.contextMenu.contextEdgeTarget, state.contextMenu.contextEdgeSource, !hidden);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_hidden source={} target={} hidden={}", state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, !hidden);
            canvasViewport.refresh();
        }));
        addConnectionLayerActions(actions, canvasViewport, state, selectedGroup);
    }

    private static void addConnectionLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        String edgeId = CanvasRenderer.edgeKey(state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        String layerKey = CanvasLayerOrdering.connectionKey(edgeId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                CanvasLayerMutations.moveConnectionLayer(state, selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, true);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=connection id={}", edgeId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                CanvasLayerMutations.moveConnectionLayer(state, selectedGroup, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, false);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=connection id={}", edgeId);
                canvasViewport.refresh();
            }));
        }
    }
}
