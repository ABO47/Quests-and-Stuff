package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
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
        String sourceId = state.contextMenu.contextEdgeSource;
        String targetId = state.contextMenu.contextEdgeTarget;
        boolean isEcEdge = ConnectionRenderer.isEcId(state, selectedGroup, sourceId)
                || ConnectionRenderer.isEcId(state, selectedGroup, targetId);
        if (isEcEdge) {
            addEcEdgeActions(actions, canvasViewport, state, player, selectedGroup, sourceId, targetId);
        } else {
            addQuestEdgeActions(actions, canvasViewport, state, player, selectedGroup, sourceId, targetId);
        }
        addConnectionLayerActions(actions, canvasViewport, state, selectedGroup);
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addQuestEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup, String sourceId, String targetId) {
        boolean direct = CanvasRenderer.isConnectionDirect(state, selectedGroup, sourceId, targetId);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.runConnectionModeAction(player, targetId, sourceId, direct);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_connection_mode source={} target={} direct={}", sourceId, targetId, !direct);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
            int color = CanvasRenderer.connectionColor(state, selectedGroup, sourceId, targetId);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedGroup, sourceId, targetId), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_color source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        boolean hidden = CanvasRenderer.isConnectionHidden(state, selectedGroup, sourceId, targetId);
        actions.add(new ContextAction(hidden ? CanvasContextMenuController.tr("ui.questsandstuff.context.show_connection") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_connection"), hidden ? "eye" : "eye-off", hidden ? ModColors.INTERACTIVE : ModColors.WARNING, () -> {
            EditorCommandClient.runConnectionHiddenAction(player, targetId, sourceId, !hidden);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_hidden source={} target={} hidden={}", sourceId, targetId, !hidden);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openConnectionTexturePicker(state, selectedGroup, sourceId, targetId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_texture source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        if (!ConnectionRenderer.connectionTexture(state, selectedGroup, sourceId, targetId).isBlank()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.runConnectionTextureAction(player, targetId, sourceId, "");
                ConnectionRenderer.setConnectionTexture(state, selectedGroup, sourceId, targetId, "");
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_connection_texture source={} target={}", sourceId, targetId);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addEcEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup, String sourceId, String targetId) {
        boolean direct = ConnectionRenderer.ecIsConnectionDirect(state, selectedGroup, sourceId, targetId);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", ModColors.INTERACTIVE, () -> {
            EditorCommandClient.runEcConnectionModeAction(player, state, sourceId, targetId, !direct);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_ec_connection_mode source={} target={} direct={}", sourceId, targetId, !direct);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
            int color = ConnectionRenderer.ecConnectionColor(state, selectedGroup, sourceId, targetId);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedGroup, sourceId, targetId), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=ec_connection_color source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openConnectionTexturePicker(state, selectedGroup, sourceId, targetId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=ec_connection_texture source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        if (!ConnectionRenderer.ecConnectionTexture(state, selectedGroup, sourceId, targetId).isBlank()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), "delete", ModColors.WARNING, () -> {
                EditorCommandClient.runEcConnectionTextureAction(state, sourceId, targetId, "");
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_ec_connection_texture source={} target={}", sourceId, targetId);
                canvasViewport.refresh();
            }));
        }
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
