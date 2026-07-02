package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

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
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;


import java.util.List;

final class CanvasContextElementActions {
    private CanvasContextElementActions() {
    }

    static void addImageActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.IMAGE || state.contextMenu.contextCanvasImageId.isBlank()) {
            return;
        }
        CanvasImageLayer contextImage = CanvasLayerMutations.findCanvasImage(state, selectedGroup, state.contextMenu.contextCanvasImageId);
        if (contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_recipe_card"), "recipe", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeChange(selectedGroup, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_recipe_card group={} image={}", selectedGroup, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_entity"), "entity", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityChange(selectedGroup, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_entity group={} image={}", selectedGroup, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
            EntityIconControls.addEntityVariantAndMotionActions(
                    actions,
                    state,
                    contextImage.asset(),
                    ModalTargets.canvasImage(selectedGroup, state.contextMenu.contextCanvasImageId),
                    () -> ContextMenuState.close(state),
                    () -> {
                        state.questDetails.entityMotionEditorBatchImageIds = "";
                        EntityMotionEditor.openMainCanvas(state, selectedGroup, state.contextMenu.contextCanvasImageId, state.contextMenu.contextMenuX, state.contextMenu.contextMenuY);
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_entity_motion group={} image={}", selectedGroup, state.contextMenu.contextCanvasImageId);
                    },
                    canvasViewport::refresh
            );
        } else if (contextImage != null && (ModelAssetPreviewRenderer.isItemAsset(contextImage.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(contextImage.asset()))) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_item"), "icon", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemChange(selectedGroup, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_item_model group={} image={}", selectedGroup, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        } else if (contextImage != null && ModelAssetPreviewRenderer.isBlockModelAsset(contextImage.asset())) {
            actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.change_block"), "box", ModColors.INTERACTIVE, () -> {
                ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockChange(selectedGroup, state.contextMenu.contextCanvasImageId), state.canvas.canvasImageLogicalX, state.canvas.canvasImageLogicalY);
                ContextMenuState.clearDeleteConfirm(state);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_block_model group={} image={}", selectedGroup, state.contextMenu.contextCanvasImageId);
                canvasViewport.refresh();
            }));
        }
        if (contextImage != null && CanvasTransformGizmo.supports(contextImage.asset())
                && CanvasSelectionActions.totalCanvasSelectionCount(state) == 1
                && CanvasSelectionActions.isImageSelected(state, contextImage.id())) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, canvasViewport::refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(actions, state, () -> {
                CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, selectedGroup, state.contextMenu.contextCanvasImageId);
                if (image == null) {
                    return;
                }
                CanvasLayerMutations.putCanvasImage(state, selectedGroup, image.withCenteredPivot());
                state.canvas.canvasSelection.setPrimaryImageId(image.id());
                state.canvas.canvasSelection.imageIds().clear();
                state.canvas.canvasSelection.imageIds().add(image.id());
                state.canvas.canvasSelection.setPrimaryTextId("");
                state.canvas.canvasSelection.textIds().clear();
            }, canvasViewport::refresh);
        }
        if (CanvasGridFitController.canFitImageToGrid(state, selectedGroup, state.contextMenu.contextCanvasImageId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitImageToGrid(state, selectedGroup, state.contextMenu.contextCanvasImageId);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=image id={} changed={}", state.contextMenu.contextCanvasImageId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.imageKey(state.contextMenu.contextCanvasImageId), "image", state.contextMenu.contextCanvasImageId);
        addCopyAndDeleteActions(actions, canvasViewport, state);
    }

    static void addTextActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.TEXT || state.contextMenu.contextCanvasTextId.isBlank()) {
            return;
        }
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.edit_text"), "rename", ModColors.INTERACTIVE, () -> {
            CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, selectedGroup, state.contextMenu.contextCanvasTextId);
            TextEditSession.beginMainCanvas(state, state.contextMenu.contextCanvasTextId, text == null ? "" : text.text());
            state.canvas.canvasSelection.setPrimaryTextId(state.contextMenu.contextCanvasTextId);
            canvasViewport.setFocus(true);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=edit_text id={}", state.contextMenu.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, false, () -> {
            TextStyleSession.openMainCanvas(state, state.contextMenu.contextCanvasTextId);
            state.canvas.canvasSelection.setPrimaryTextId(state.contextMenu.contextCanvasTextId);
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=text_style id={}", state.contextMenu.contextCanvasTextId);
            canvasViewport.refresh();
        }));
        if (CanvasGridFitController.canFitTextToGrid(state, selectedGroup, state.contextMenu.contextCanvasTextId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitTextToGrid(state, selectedGroup, state.contextMenu.contextCanvasTextId);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=text id={} changed={}", state.contextMenu.contextCanvasTextId, changed);
                canvasViewport.refresh();
            }));
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.textKey(state.contextMenu.contextCanvasTextId), "text", state.contextMenu.contextCanvasTextId);
        addCopyAndDeleteActions(actions, canvasViewport, state);
    }

    static void addExclusiveChoiceActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.EXCLUSIVE_CHOICE || state.contextMenu.contextCanvasExclusiveChoiceId.isBlank()) {
            return;
        }
        addExclusiveChoiceConnectedQuestActions(actions, canvasViewport, state, selectedGroup);
        addExclusiveChoicePrerequisiteActions(actions, canvasViewport, state, selectedGroup);
        if (CanvasGridFitController.canFitExclusiveChoiceToGrid(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId)) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.fit_to_grid"), "fit_grid", ModColors.INTERACTIVE, () -> {
                boolean changed = CanvasGridFitController.fitExclusiveChoiceToGrid(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId);
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=fit_to_grid target=exclusive_choice id={} changed={}", state.contextMenu.contextCanvasExclusiveChoiceId, changed);
                canvasViewport.refresh();
            }));
        }
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec != null) {
            actions.add(ContextActions.action(
                    CanvasContextMenuController.tr("ui.questsandstuff.context.change_background"),
                    "background", ModColors.INTERACTIVE, () -> {
                        ModalOpenActions.openEcBackgroundPicker(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId, ec.background());
                        ContextMenuState.close(state);
                        QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_ec_background ec={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                        canvasViewport.refresh();
                    }
            ));
            if (!ec.background().isBlank()) {
                actions.add(ContextActions.action(
                        CanvasContextMenuController.tr("ui.questsandstuff.context.remove_background"),
                        "delete", ModColors.WARNING, () -> {
                            CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedGroup, ec.withBackground(""));
                            CanvasLayerMutations.persistCanvasExclusiveChoice(state, selectedGroup, ec.id());
                            ContextMenuState.clearDeleteConfirm(state);
                            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_ec_background ec={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                            canvasViewport.refresh();
                        }
                ));
            }
        }
        addLayerActions(actions, canvasViewport, state, selectedGroup, CanvasLayerOrdering.exclusiveChoiceKey(state.contextMenu.contextCanvasExclusiveChoiceId), "exclusive_choice", state.contextMenu.contextCanvasExclusiveChoiceId);
        addCopyAndDeleteActions(actions, canvasViewport, state);
    }

    private static void addExclusiveChoiceConnectedQuestActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec == null) {
            return;
        }
        actions.add(ContextActions.promoted(
                CanvasContextMenuController.tr("ui.questsandstuff.context.connect_to"),
                "connect", ModColors.SUCCESS, () -> {
                    state.canvas.connectEcId = state.contextMenu.contextCanvasExclusiveChoiceId;
                    ContextMenuState.close(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] canvas exclusive choice connect_to id={}", state.contextMenu.contextCanvasExclusiveChoiceId);
                    canvasViewport.refresh();
                }
        ));
    }

    private static void addExclusiveChoicePrerequisiteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, selectedGroup, state.contextMenu.contextCanvasExclusiveChoiceId);
        if (ec == null) {
            return;
        }
        int connectionCount = ec.connectionQuestIds().size() + ec.prerequisiteQuestIds().size();
        if (connectionCount <= 0) {
            return;
        }
        actions.add(ContextActions.action(CanvasContextMenuController.tr(QuestVocabulary.CONTEXT_PREREQUISITES_MANAGER), "share-2", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openPrerequisitesManagerForEc(state, ec.id());
            ContextMenuState.clearDeleteConfirm(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=prerequisites_manager ec={} connections={}", state.contextMenu.contextCanvasExclusiveChoiceId, connectionCount);
            canvasViewport.refresh();
        }));
    }

    private static void addCopyAndDeleteActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state) {
        if (CanvasContextMenuSupport.canCopyContext(canvasViewport, state)) {
            actions.add(ContextActions.copy(() -> {
                CanvasContextMenuSupport.copyContextToClipboard(canvasViewport, state);
                ContextMenuState.clearDeleteConfirm(state);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextDeleteController.canDeleteContext(state)) {
            String deleteKey = CanvasContextDeleteController.deleteConfirmKey(state);
            actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
                CanvasContextDeleteController.runDeleteAction(canvasViewport.player(), state);
                canvasViewport.refresh();
            }));
        }
    }

    private static void addLayerActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, String selectedGroup, String layerKey, String targetName, String targetId) {
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, true)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.bring_to_front"), "up", ModColors.INTERACTIVE, () -> {
                switch (targetName) {
                    case "image" -> CanvasLayerMutations.moveImageLayer(state, selectedGroup, targetId, true);
                    case "text" -> CanvasLayerMutations.moveTextLayer(state, selectedGroup, targetId, true);
                    case "exclusive_choice" -> CanvasLayerMutations.moveExclusiveChoiceLayer(state, selectedGroup, targetId, true);
                }
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=bring_to_front target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
        if (CanvasContextMenuSupport.canMoveLayer(canvasViewport, state, selectedGroup, layerKey, false)) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.send_to_back"), "down", ModColors.TEXT_MUTED, () -> {
                switch (targetName) {
                    case "image" -> CanvasLayerMutations.moveImageLayer(state, selectedGroup, targetId, false);
                    case "text" -> CanvasLayerMutations.moveTextLayer(state, selectedGroup, targetId, false);
                    case "exclusive_choice" -> CanvasLayerMutations.moveExclusiveChoiceLayer(state, selectedGroup, targetId, false);
                }
                ContextMenuState.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=send_to_back target={} id={}", targetName, targetId);
                canvasViewport.refresh();
            }));
        }
    }
}
