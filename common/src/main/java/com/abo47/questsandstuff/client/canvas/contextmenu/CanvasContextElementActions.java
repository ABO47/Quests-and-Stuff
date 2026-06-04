package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
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
        CanvasImageLayer contextImage = CanvasRenderer.findCanvasImage(state, selectedGroup, state.contextCanvasImageId);
        if (contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset())) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_recipe_card"), "recipe", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                state.contextDeleteConfirmKey = "";
                state.contextMenuOpen = false;
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_recipe_card group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset())) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                state.contextDeleteConfirmKey = "";
                state.contextMenuOpen = false;
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_entity group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
            EntityIconControls.addEntityVariantAndMotionActions(
                    actions,
                    state,
                    contextImage.asset(),
                    ModalTargets.canvasImage(selectedGroup, state.contextCanvasImageId),
                    () -> state.contextMenuOpen = false,
                    () -> {
                        EntityMotionEditor.openMainCanvas(state, selectedGroup, state.contextCanvasImageId, state.contextMenuX, state.contextMenuY);
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_motion group={} image={}", selectedGroup, state.contextCanvasImageId);
                    },
                    canvasViewport::refresh
            );
        } else if (contextImage != null && (CanvasModelPreviewRenderer.isItemAsset(contextImage.asset()) || CanvasModelPreviewRenderer.isItemTagAsset(contextImage.asset()))) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                state.contextDeleteConfirmKey = "";
                state.contextMenuOpen = false;
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_item_model group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && CanvasModelPreviewRenderer.isBlockModelAsset(contextImage.asset())) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedGroup, state.contextCanvasImageId), state.canvasImageLogicalX, state.canvasImageLogicalY);
                state.contextDeleteConfirmKey = "";
                state.contextMenuOpen = false;
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_block_model group={} image={}", selectedGroup, state.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        }
        if (contextImage != null && CanvasTransformGizmo.supports(contextImage.asset())
                && CanvasRenderer.totalCanvasSelectionCount(state) == 1
                && CanvasRenderer.isImageSelected(state, contextImage.id())) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(actions, state, () -> {
                CanvasImageLayer image = CanvasRenderer.findCanvasImage(state, selectedGroup, state.contextCanvasImageId);
                if (image == null) {
                    return;
                }
                CanvasRenderer.putCanvasImage(state, selectedGroup, image.withCenteredPivot());
                state.selectedCanvasImageId = image.id();
                state.selectedCanvasImageIds.clear();
                state.selectedCanvasImageIds.add(image.id());
                state.selectedCanvasTextId = "";
                state.selectedCanvasTextIds.clear();
            }, canvasViewport::refresh);
        }
        if (CanvasGridFitController.canFitImageToGrid(state, selectedGroup, state.contextCanvasImageId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitImageToGrid(state, selectedGroup, state.contextCanvasImageId);
                state.contextDeleteConfirmKey = "";
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
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.edit_text"), "rename", ModColors.INTERACTIVE, () -> {
            CanvasTextLayer text = CanvasRenderer.findCanvasText(state, selectedGroup, state.contextCanvasTextId);
            state.canvasTextEditOpen = true;
            state.canvasTextEditTarget = state.contextCanvasTextId;
            state.canvasTextEditDraft = text == null ? "" : text.text();
            state.canvasTextEditCursor = state.canvasTextEditDraft.length();
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
            state.selectingCanvasTextRange = false;
            state.canvasTextMenuOpen = true;
            state.canvasTextMenuTarget = state.contextCanvasTextId;
            state.selectedCanvasTextId = state.contextCanvasTextId;
            canvasViewport.setFocus(true);
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_text id={}", state.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, false, () -> {
            state.canvasTextMenuOpen = true;
            state.canvasTextMenuTarget = state.contextCanvasTextId;
            state.selectedCanvasTextId = state.contextCanvasTextId;
            state.contextDeleteConfirmKey = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=text_style id={}", state.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        if (CanvasGridFitController.canFitTextToGrid(state, selectedGroup, state.contextCanvasTextId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitTextToGrid(state, selectedGroup, state.contextCanvasTextId);
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=text id={} changed={}", state.contextCanvasTextId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.textKey(state.contextCanvasTextId), "text", state.contextCanvasTextId);
    }

    private static void addLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, String layerKey, String targetName, String targetId) {
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                if ("image".equals(targetName)) {
                    CanvasRenderer.moveImageLayer(state, selectedGroup, targetId, true);
                } else {
                    CanvasRenderer.moveTextLayer(state, selectedGroup, targetId, true);
                }
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                if ("image".equals(targetName)) {
                    CanvasRenderer.moveImageLayer(state, selectedGroup, targetId, false);
                } else {
                    CanvasRenderer.moveTextLayer(state, selectedGroup, targetId, false);
                }
                state.contextDeleteConfirmKey = "";
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
    }
}
