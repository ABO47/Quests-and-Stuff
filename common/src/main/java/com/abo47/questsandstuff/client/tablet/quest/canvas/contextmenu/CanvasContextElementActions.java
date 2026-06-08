package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.model.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;

final class CanvasContextElementActions {
    private CanvasContextElementActions() {
    }

    static void addImageActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.IMAGE || state.contextCanvasImageId.isBlank()) {
            return;
        }
        CanvasImageLayer contextImage = CanvasLayerMutations.findCanvasImage(state, selectedGroup, state.contextCanvasImageId);
        if (contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_recipe_card"), "recipe", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_recipe_card group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_entity group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
            EntityIconControls.addEntityVariantAndMotionActions(
                    actions,
                    state,
                    contextImage.asset(),
                    ModalTargets.canvasImage(selectedGroup, state.contextCanvasImageId),
                    () -> ContextMenuState.close(state),
                    () -> {
                        EntityMotionEditor.openMainCanvas(state, selectedGroup, state.contextCanvasImageId, state.contextMenuX, state.contextMenuY);
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_motion group={} image={}", selectedGroup, state.contextCanvasImageId);
                    },
                    canvasViewport::refresh
            );
        } else if (contextImage != null && (ModelAssetPreviewRenderer.isItemAsset(contextImage.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(contextImage.asset()))) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_item_model group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && ModelAssetPreviewRenderer.isBlockModelAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_block_model group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        }
        if (contextImage != null && CanvasTransformGizmo.supports(contextImage.asset())
                && CanvasSelectionActions.totalCanvasSelectionCount(state) == 1
                && CanvasSelectionActions.isImageSelected(state, contextImage.id())) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(actions, state, () -> {
                CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, selectedGroup, state.contextCanvasImageId);
                if (image == null) {
                    return;
                }
                CanvasLayerMutations.putCanvasImage(state, selectedGroup, image.withCenteredPivot());
                state.canvasSelection.setPrimaryImageId(image.id());
                state.canvasSelection.imageIds().clear();
                state.canvasSelection.imageIds().add(image.id());
                state.canvasSelection.setPrimaryTextId("");
                state.canvasSelection.textIds().clear();
            }, canvasViewport::refresh);
        }
        if (CanvasGridFitController.canFitImageToGrid(state, selectedGroup, state.contextCanvasImageId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitImageToGrid(state, selectedGroup, state.contextCanvasImageId);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=image id={} changed={}", state.contextCanvasImageId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.imageKey(state.contextCanvasImageId), "image", state.contextCanvasImageId);
    }

    static void addTextActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        if (state.contextMenuTarget != ContextMenuTarget.TEXT || state.contextCanvasTextId.isBlank()) {
            return;
        }
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.edit_text"), "rename", ModColors.INTERACTIVE, () -> {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, selectedGroup, state.contextCanvasTextId);
            TextEditSession.beginMainCanvas(state, state.contextCanvasTextId, text == null ? "" : text.text());
            state.canvasSelection.setPrimaryTextId(state.contextCanvasTextId);
            canvasViewport.setFocus(true);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_text id={}", state.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, false, () -> {
            TextStyleSession.openMainCanvas(state, state.contextCanvasTextId);
            state.canvasSelection.setPrimaryTextId(state.contextCanvasTextId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=text_style id={}", state.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        if (CanvasGridFitController.canFitTextToGrid(state, selectedGroup, state.contextCanvasTextId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitTextToGrid(state, selectedGroup, state.contextCanvasTextId);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=text id={} changed={}", state.contextCanvasTextId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.textKey(state.contextCanvasTextId), "text", state.contextCanvasTextId);
    }

    private static void addLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, String layerKey, String targetName, String targetId) {
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                if ("image".equals(targetName)) {
                    CanvasLayerMutations.moveImageLayer(state, selectedGroup, targetId, true);
                } else {
                    CanvasLayerMutations.moveTextLayer(state, selectedGroup, targetId, true);
                }
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                if ("image".equals(targetName)) {
                    CanvasLayerMutations.moveImageLayer(state, selectedGroup, targetId, false);
                } else {
                    CanvasLayerMutations.moveTextLayer(state, selectedGroup, targetId, false);
                }
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
    }
}
