package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasContextSelectionActions {
    private CanvasContextSelectionActions() {
    }

    static void addSelectionActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.SELECTION || selectedGroup.isBlank()) {
            return;
        }
        if (CanvasRenderer.totalCanvasSelectionCount(state) > 1) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            addSelectionAlignmentActions(actions, canvasViewport, state, player);
            addSelectionLayerActions(actions, canvasViewport, state, selectedGroup);
            if (CanvasGridFitController.canFitSelectionToGrid(state, selectedGroup, canvasViewport.cardLookup())) {
                actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "grid", ModColors.INTERACTIVE, () -> {
                    boolean changed = CanvasGridFitController.fitSelectionToGrid(player, state, selectedGroup, canvasViewport.cardLookup());
                    state.contextDeleteConfirmKey = "";
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=selection count={} changed={}", CanvasRenderer.totalCanvasSelectionCount(state), changed);
                    canvasViewport.refresh();
                }));
            }
        }
        List<CanvasContextMenuController.EdgeRef> connectedEdges = CanvasContextMenuController.selectedConnectedEdges(state, selectedGroup);
        if (!connectedEdges.isEmpty()) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.selection_connection_color"), "style_color", ModColors.INTERACTIVE, () -> {
                CanvasContextMenuController.EdgeRef first = connectedEdges.get(0);
                int color = CanvasRenderer.connectionColor(state, selectedGroup, first.prerequisiteId(), first.questId());
                ModalOpenActions.openColorPicker(state, ModalTargets.connectionSelection(selectedGroup), color);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=patch_connection_colors group={} edges={}", selectedGroup, connectedEdges.size());
                canvasViewport.refresh();
            }));
        }
    }

    private static void addSelectionLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        CanvasSelectionSet selection = CanvasSelectionSet.current(state);
        if (selection.layerKeys().isEmpty()) {
            return;
        }
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
            CanvasRenderer.moveCanvasLayers(state, selectedGroup, selection.layerKeys(), true);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
            CanvasRenderer.moveCanvasLayers(state, selectedGroup, selection.layerKeys(), false);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target=selection count={}", selection.size());
            canvasViewport.refresh();
        }));
    }

    private static void addSelectionAlignmentActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player) {
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.align_horizontal_center"), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
            boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_horizontal_center target=selection count={} changed={}", CanvasRenderer.totalCanvasSelectionCount(state), changed);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.align_vertical_center"), "align-center-vertical", ModColors.INTERACTIVE, () -> {
            boolean changed = CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=align_vertical_center target=selection count={} changed={}", CanvasRenderer.totalCanvasSelectionCount(state), changed);
            canvasViewport.refresh();
        }));
    }
}
