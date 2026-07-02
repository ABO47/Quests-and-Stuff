package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasContextEdgeActions {
    private CanvasContextEdgeActions() {
    }

    static void addEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.EDGE || state.contextMenu.contextEdgeSource.isBlank() || state.contextMenu.contextEdgeTarget.isBlank()) {
            return;
        }
        String sourceId = state.contextMenu.contextEdgeSource;
        String targetId = state.contextMenu.contextEdgeTarget;
        boolean isEcEdge = ConnectionRenderer.isEcId(state, selectedChapter, sourceId)
                || ConnectionRenderer.isEcId(state, selectedChapter, targetId);
        if (isEcEdge) {
            addEcEdgeActions(actions, canvasViewport, state, player, selectedChapter, sourceId, targetId);
        } else {
            addQuestEdgeActions(actions, canvasViewport, state, player, selectedChapter, sourceId, targetId);
        }
        addConnectionLayerActions(actions, canvasViewport, state, selectedChapter);
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(player, state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addQuestEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter, String sourceId, String targetId) {
        boolean direct = CanvasRenderer.isConnectionDirect(state, selectedChapter, sourceId, targetId);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", TabletColors.INTERACTIVE, () -> {
            EditorCanvasCommandClient.runConnectionModeAction(player, targetId, sourceId, direct);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_connection_mode source={} target={} direct={}", sourceId, targetId, !direct);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", TabletColors.INTERACTIVE, () -> {
            int color = CanvasRenderer.connectionColor(state, selectedChapter, sourceId, targetId);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedChapter, sourceId, targetId), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_color source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        boolean hidden = CanvasRenderer.isConnectionHidden(state, selectedChapter, sourceId, targetId);
        actions.add(new ContextAction(hidden ? CanvasContextMenuController.tr("ui.questsandstuff.context.show_connection") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_connection"), hidden ? "eye" : "eye-off", hidden ? TabletColors.INTERACTIVE : TabletColors.WARNING, () -> {
            EditorCanvasCommandClient.runConnectionHiddenAction(player, targetId, sourceId, !hidden);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_hidden source={} target={} hidden={}", sourceId, targetId, !hidden);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> {
            ModalOpenActions.openConnectionTexturePicker(state, selectedChapter, sourceId, targetId);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=connection_texture source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        if (!ConnectionRenderer.connectionTexture(state, selectedChapter, sourceId, targetId).isBlank()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), "delete", TabletColors.WARNING, () -> {
                EditorCanvasCommandClient.runConnectionTextureAction(player, targetId, sourceId, "");
                ConnectionRenderer.setConnectionTexture(state, selectedChapter, sourceId, targetId, "");
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_connection_texture source={} target={}", sourceId, targetId);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addEcEdgeActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter, String sourceId, String targetId) {
        boolean direct = ConnectionRenderer.ecIsConnectionDirect(state, selectedChapter, sourceId, targetId);
        actions.add(new ContextAction(direct ? CanvasContextMenuController.tr("ui.questsandstuff.context.connection_grid") : CanvasContextMenuController.tr("ui.questsandstuff.context.connection_direct"), "connect", TabletColors.INTERACTIVE, () -> {
            EditorCanvasCommandClient.runEcConnectionModeAction(player, state, sourceId, targetId, !direct);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=toggle_ec_connection_mode source={} target={} direct={}", sourceId, targetId, !direct);
            canvasViewport.refresh();
        }));
        boolean hidden = ConnectionRenderer.isConnectionHidden(state, selectedChapter, sourceId, targetId);
        actions.add(new ContextAction(hidden ? CanvasContextMenuController.tr("ui.questsandstuff.context.show_connection") : CanvasContextMenuController.tr("ui.questsandstuff.context.hide_connection"), hidden ? "eye" : "eye-off", hidden ? TabletColors.INTERACTIVE : TabletColors.WARNING, () -> {
            EditorCanvasCommandClient.runEcConnectionHiddenAction(player, state, sourceId, targetId, !hidden);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=ec_connection_hidden source={} target={} hidden={}", sourceId, targetId, !hidden);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.connection_color"), "style_color", TabletColors.INTERACTIVE, () -> {
            int color = ConnectionRenderer.ecConnectionColor(state, selectedChapter, sourceId, targetId);
            ModalOpenActions.openColorPicker(state, ModalTargets.connection(selectedChapter, sourceId, targetId), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=ec_connection_color source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> {
            ModalOpenActions.openConnectionTexturePicker(state, selectedChapter, sourceId, targetId);
            ContextMenuController.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=ec_connection_texture source={} target={}", sourceId, targetId);
            canvasViewport.refresh();
        }));
        if (!ConnectionRenderer.ecConnectionTexture(state, selectedChapter, sourceId, targetId).isBlank()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_connection_texture"), "delete", TabletColors.WARNING, () -> {
                EditorCanvasCommandClient.runEcConnectionTextureAction(state, sourceId, targetId, "");
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_ec_connection_texture source={} target={}", sourceId, targetId);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addConnectionLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedChapter) {
        String edgeId = CanvasRenderer.edgeKey(state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget);
        String layerKey = CanvasLayerOrdering.connectionKey(edgeId);
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, true)) {
            actions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", TabletColors.INTERACTIVE, () -> {
                CanvasLayerMutations.moveConnectionLayer(state, selectedChapter, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, true);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=connection id={}", edgeId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedChapter, layerKey, false)) {
            actions.add(ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", TabletColors.TEXT_MUTED, () -> {
                CanvasLayerMutations.moveConnectionLayer(state, selectedChapter, state.contextMenu.contextEdgeSource, state.contextMenu.contextEdgeTarget, false);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=connection id={}", edgeId);
                canvasViewport.refresh();
            }));
        }
    }
}
