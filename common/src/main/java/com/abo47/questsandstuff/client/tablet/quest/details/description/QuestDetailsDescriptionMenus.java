package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasTextStyleMenu;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.model.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class QuestDetailsDescriptionMenus {
    private QuestDetailsDescriptionMenus() {
    }

    public static void keepTextStyleOpenForActiveEdit(TabletUiState state, QuestDetailsDescriptionModel model) {
        if (!QuestDetailsEditState.canEdit(state) || !state.canvasTextEditOpen
                || state.questDetailsTextEditTarget.isBlank()
                || !state.questDetailsTextEditTarget.equals(state.canvasTextEditTarget)
                || model.text(state.questDetailsTextEditTarget) == null) {
            return;
        }
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = state.questDetailsTextEditTarget;
    }

    public static void renderStyleMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int w, int h) {
        if ((!state.questDetailsTextStyleOpen && state.questDetailsTextFontSizeFieldTarget.isBlank()) || !QuestDetailsEditState.canEdit(state)) {
            resetStyleMenuBounds(state);
            return;
        }
        String target = resolvedTextStyleTarget(state, model);
        CanvasTextLayer text = target.isBlank() ? null : model.text(target);
        if (text == null) {
            state.questDetailsTextStyleOpen = false;
            state.questDetailsTextStyleTarget = "";
            resetStyleMenuBounds(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text style close target={} reason=missing_text", target);
            return;
        }
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = text.id();
        CanvasTextStyleMenu.renderQuestDetails(modal, state, text, x, y, w, h, state.questDetailsDescScroll, next -> {
            updateText(player, state, questId, model, next, Math.max(1, w - 1));
            state.questDetailsTextStyleOpen = true;
            state.questDetailsTextStyleTarget = next.id();
        }, () -> {
            state.questDetailsTextColorQuestId = questId;
            state.questDetailsTextColorTextId = text.id();
            ModalOpenActions.openColorPicker(state, ModalTargets.questDescText(questId, text.id()), CanvasRenderer.activeTextColor(state, text));
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text color open picker quest={} text={}", questId, text.id());
            refresh.run();
        }, refresh);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int viewportW, int viewportH) {
        String kind = state.questDetailsContextKind;
        if (!state.questDetailsContextOpen || kind == null || !kind.startsWith("desc") || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        switch (kind) {
            case "description" -> addDescriptionActions(actions, state, player, questId, model, x, y);
            case "desc_text" -> addTextActions(actions, state, player, questId, model);
            case "desc_image" -> addImageActions(actions, state, player, questId, model, refresh);
            case "desc_selection" -> addSelectionActions(actions, state, player, questId, model, viewportW, viewportH, refresh);
            default -> {
            }
        }
        if (actions.isEmpty()) {
            return;
        }
        int menuW = 124;
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int maxMenuH = Math.max(ContextMenuPanel.heightForRows(1), viewportH - 8);
        while (visibleRows > 1 && ContextMenuPanel.heightFor(actions, visibleRows) > maxMenuH) {
            visibleRows--;
        }
        state.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetailsContextScroll = ScrollController.clamp(state.questDetailsContextScroll, state.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int localX = localContextCoordinate(state.questDetailsContextAnchorX, x, viewportW);
        int localY = localContextCoordinate(state.questDetailsContextAnchorY, y, viewportH);
        int mx = ContextMenuPlacement.fitRightOrLeft(localX, viewportW, menuW);
        int my = ContextMenuPlacement.fitBelowOrAbove(localY, viewportH, menuH);
        state.questDetailsContextX = x + mx;
        state.questDetailsContextY = y + my;
        state.questDetailsContextW = menuW;
        state.questDetailsContextH = menuH;
        WidgetGroup canvasMenuLayer = new WidgetGroup(x, y, viewportW, viewportH);
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, actions, state.questDetailsContextScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientState.closeContext(state);
            }
            refresh.run();
        }, viewportW, viewportH, ScrollState.bind(
                () -> state.questDetailsContextScroll,
                value -> state.questDetailsContextScroll = ScrollController.clamp(value, state.questDetailsContextScrollMax),
                () -> state.contextMenuScrollDragging,
                dragging -> state.contextMenuScrollDragging = dragging
        ), refresh);
        canvasMenuLayer.addWidget(menu);
        modal.addWidget(canvasMenuLayer);
    }

    private static void addDescriptionActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, int x, int y) {
        List<ContextAction> addActions = new ArrayList<>();
        addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_TEXT_BOX), "text", ModColors.SUCCESS, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addTextAt(player, state, questId, model, x, y);
        }));
        addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_IMAGE), "image", ModColors.SUCCESS, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addImageAt(state, questId, x, y);
        }));
        addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_ENTITY), "entity", ModColors.SUCCESS, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addEntityAt(state, questId, x, y);
        }));
        addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_ITEM), "icon", ModColors.SUCCESS, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addItemAt(state, questId, x, y);
        }));
        addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_BLOCK), "add_block", ModColors.SUCCESS, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addBlockAt(state, questId, x, y);
        }));
        if (RecipeViewerIntegrations.hasAvailableViewer()) {
            addActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD_RECIPE_CARD), "recipe", ModColors.SUCCESS, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsDescriptionPanel.addRecipeCardAt(state, questId, x, y);
            }));
        }
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD), "add", ModColors.SUCCESS, addActions));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_BACKGROUND), "background", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            ModalOpenActions.openAssetPicker(state, ModalTargets.descBackground(questId), model.canvasBackground == null ? "" : model.canvasBackground);
        }));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_GRID_COLOR), "style_color", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            int color = TabletGridControls.defaultGridColor(state);
            ModalOpenActions.openColorPicker(state, ModalTargets.gridColor(), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=change_grid_color color={}", color);
        }));
        if (model.canvasBackground != null && !model.canvasBackground.isBlank() && !"default".equals(model.canvasBackground)) {
            String deleteKey = "quest_details_background:" + questId;
            actions.add(ContextActions.warningDelete(state, deleteKey, TabletVocabulary.text(QuestVocabulary.CONTEXT_REMOVE_BACKGROUND), () -> {
                model.canvasBackground = "default";
                QuestDetailsDescriptionModel.save(player, questId, model);
            }));
        }
        if (state.canvasClipboard.hasCanvasLayers()) {
            actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CONTEXT_PASTE), "paste", ModColors.SUCCESS, () -> {
                ContextMenuState.clearDeleteConfirm(state);
                QuestDetailsDescriptionClipboard.pasteAtContext(player, state, questId, model, x, y);
            }));
        }
    }

    private static void addTextActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model) {
        actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CONTEXT_EDIT_TEXT), "rename", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            CanvasTextLayer text = model.text(state.questDetailsContextId);
            state.questDetailsTextEditTarget = text == null ? "" : text.id();
            state.questDetailsTextEditDraft = text == null ? "" : text.text();
            state.canvasTextEditOpen = text != null;
            state.canvasTextEditTarget = text == null ? "" : text.id();
            state.canvasTextEditDraft = text == null ? "" : text.text();
            state.canvasTextEditCursor = state.canvasTextEditDraft.length();
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
            state.canvasTextMenuOpen = false;
            state.canvasTextMenuTarget = "";
            state.questDetailsTextStyleOpen = text != null;
            state.questDetailsTextStyleTarget = text == null ? "" : text.id();
        }));
        actions.add(new ContextAction(TabletVocabulary.text(QuestVocabulary.CONTEXT_TEXT_STYLE), "style", ModColors.INTERACTIVE, false, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            state.questDetailsTextStyleOpen = true;
            state.questDetailsTextStyleTarget = state.questDetailsContextId;
        }));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "fit_grid", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitTextToGrid(player, state, questId, model, state.questDetailsContextId);
        }));
        addOrderActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActions.copy(() -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionClipboard.copyText(state, model, state.questDetailsContextId);
        }));
        String deleteKey = "quest_details_text:" + questId + ":" + state.questDetailsContextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            model.removeText(state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void addImageActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, Runnable refresh) {
        CanvasImageLayer contextImage = model.image(state.questDetailsContextId);
        boolean entityImage = contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset());
        boolean itemImage = contextImage != null && (ModelAssetPreviewRenderer.isItemAsset(contextImage.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(contextImage.asset()));
        boolean blockImage = contextImage != null && ModelAssetPreviewRenderer.isBlockModelAsset(contextImage.asset());
        boolean recipeImage = contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset());
        actions.add(ContextActions.promoted(changeImageLabel(entityImage, itemImage, blockImage, recipeImage), changeImageIcon(entityImage, itemImage, blockImage, recipeImage), ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            if (entityImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descEntity(questId, state.questDetailsContextId));
            } else if (itemImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descItem(questId, state.questDetailsContextId));
            } else if (blockImage) {
                QuestDetailsWindow.openBlockPicker(state, ModalTargets.descBlock(questId, state.questDetailsContextId));
            } else if (recipeImage) {
                QuestDetailsWindow.openRecipePicker(state, ModalTargets.descRecipe(questId, state.questDetailsContextId));
            } else {
                QuestDetailsWindow.openAssetPicker(state, ModalTargets.descImage(questId, state.questDetailsContextId));
            }
        }));
        if (entityImage) {
            String entityId = EntityPreviewRenderer.entityId(contextImage.asset());
            if (EntityVariantCatalog.hasVariants(entityId)) {
                actions.add(ContextActions.changeVariant(() -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    String imageId = state.questDetailsContextId;
                    QuestDetailsTransientState.closeContext(state);
                    ModalOpenActions.openEntityVariantPicker(state, ModalTargets.questDetailsImage(questId, imageId), contextImage.asset());
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=change_entity_variant quest={} image={} entity={}", questId, imageId, entityId);
                }));
            }
            actions.add(ContextActions.editMotion(() -> {
                ContextMenuState.clearDeleteConfirm(state);
                EntityMotionEditor.openQuestDetails(state, questId, state.questDetailsContextId, state.questDetailsContextX, state.questDetailsContextY);
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=edit_entity_motion quest={} image={}", questId, state.questDetailsContextId);
            }));
        }
        if (contextImage != null && CanvasTransformGizmo.supports(contextImage.asset())
                && descriptionSelectionCount(state) == 1
                && QuestDetailsDescriptionSelectionState.selectedImageIds(state).contains(contextImage.id())) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(actions, state, () -> {
                CanvasImageLayer image = model.image(state.questDetailsContextId);
                if (image != null) {
                    model.putImage(image.withCenteredPivot());
                    QuestDetailsDescriptionModel.save(player, questId, model);
                    state.questDetailsSelectedImageId = image.id();
                    state.questDetailsSelectedImageIds.clear();
                    state.questDetailsSelectedImageIds.add(image.id());
                    state.questDetailsSelectedTextId = "";
                    state.questDetailsSelectedTextIds.clear();
                }
            }, refresh);
        }
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "fit_grid", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitImageToGrid(player, state, questId, model, state.questDetailsContextId);
        }));
        addOrderActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActions.copy(() -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionClipboard.copyImage(state, model, state.questDetailsContextId);
        }));
        String deleteKey = "quest_details_image:" + questId + ":" + state.questDetailsContextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            model.removeImage(state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static String changeImageLabel(boolean entityImage, boolean itemImage, boolean blockImage, boolean recipeImage) {
        if (entityImage) {
            return TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ENTITY);
        }
        if (itemImage) {
            return TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ITEM);
        }
        if (blockImage) {
            return TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_BLOCK);
        }
        if (recipeImage) {
            return TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_RECIPE_CARD);
        }
        return TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_IMAGE);
    }

    private static String changeImageIcon(boolean entityImage, boolean itemImage, boolean blockImage, boolean recipeImage) {
        if (entityImage) {
            return "entity";
        }
        if (itemImage) {
            return "icon";
        }
        if (blockImage) {
            return "box";
        }
        if (recipeImage) {
            return "recipe";
        }
        return "image";
    }

    private static void addSelectionActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, int viewportW, int viewportH, Runnable refresh) {
        if (selectionSupportsGizmo(state, model)) {
            CanvasTransformGizmoMenus.addModeActions(actions, state, refresh);
        }
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_ALIGN), "align-center-horizontal", ModColors.INTERACTIVE, List.of(
                ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ALIGN_HORIZONTAL_CENTER), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, true);
                }),
                ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_ALIGN_VERTICAL_CENTER), "align-center-vertical", ModColors.INTERACTIVE, () -> {
                    ContextMenuState.clearDeleteConfirm(state);
                    QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, false);
                })
        )));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "fit_grid", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitSelectionToGrid(player, state, questId, model);
        }));
        addOrderActions(actions, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, true);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, false);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActions.copy(() -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.copyDescriptionSelection(state, model);
        }));
        String deleteKey = "quest_details_selection:" + questId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            QuestDetailsDescriptionPanel.deleteDescriptionSelection(state, model);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static boolean selectionSupportsGizmo(TabletUiState state, QuestDetailsDescriptionModel model) {
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null && CanvasTransformGizmo.supports(image.asset())) {
                return true;
            }
        }
        return false;
    }

    private static void addOrderActions(List<ContextAction> actions, Runnable bringToFront, Runnable sendToBack) {
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_BRING_TO_FRONT), "up", ModColors.INTERACTIVE, bringToFront));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_SEND_TO_BACK), "down", ModColors.TEXT_MUTED, sendToBack));
    }

    private static int descriptionSelectionCount(TabletUiState state) {
        return QuestDetailsDescriptionSelectionState.selectedImageIds(state).size()
                + QuestDetailsDescriptionSelectionState.selectedTextIds(state).size();
    }

    private static void updateText(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, CanvasTextLayer next, int contentW) {
        next = QuestDetailsDescriptionLayout.fitAndClampText(state, next, contentW);
        model.putText(next);
        QuestDetailsDescriptionModel.preview(questId, model);
        QuestDetailsDescriptionModel.save(player, questId, model);
    }

    private static String resolvedTextStyleTarget(TabletUiState state, QuestDetailsDescriptionModel model) {
        String activeEdit = state.canvasTextEditOpen
                && !state.questDetailsTextEditTarget.isBlank()
                && state.questDetailsTextEditTarget.equals(state.canvasTextEditTarget)
                ? state.questDetailsTextEditTarget
                : "";
        String[] candidates = {
                state.questDetailsTextStyleTarget,
                state.questDetailsTextFontSizeFieldTarget,
                activeEdit,
                state.questDetailsSelectedTextId
        };
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank() && model.text(candidate) != null) {
                return candidate;
            }
        }
        for (String selected : state.questDetailsSelectedTextIds) {
            if (selected != null && !selected.isBlank() && model.text(selected) != null) {
                return selected;
            }
        }
        return "";
    }

    private static void resetStyleMenuBounds(TabletUiState state) {
        state.questDetailsTextStyleMenuX = 0;
        state.questDetailsTextStyleMenuY = 0;
        state.questDetailsTextStyleMenuW = 0;
        state.questDetailsTextStyleMenuH = 0;
    }

    private static int localContextCoordinate(int stored, int origin, int available) {
        int fromOwner = stored - origin;
        if (fromOwner >= 0 && fromOwner <= available) {
            return fromOwner;
        }
        if (stored >= 0 && stored <= available) {
            return stored;
        }
        return fromOwner;
    }

}
