package com.abo47.questsandstuff.client.tablet.quest.details.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPlacement;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasTextStyleMenu;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardAsset;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmoMenus;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.BackgroundModes;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class QuestDetailsDescriptionMenus {
    private QuestDetailsDescriptionMenus() {
    }

    public static void keepTextStyleOpenForActiveEdit(TabletUiState state, QuestDetailsDescriptionModel model) {
        if (!QuestDetailsEditController.canEdit(state)
                || !TextEditSession.isQuestDetailsEditing(state)
                || model.text(state.questDetails.questDetailsTextEditTarget) == null) {
            return;
        }
        TextStyleSession.openQuestDetails(state, state.questDetails.questDetailsTextEditTarget);
    }

    public static void renderStyleMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int w, int h) {
        if (!TextStyleSession.questDetailsOpenOrEditingFont(state) || !QuestDetailsEditController.canEdit(state)) {
            TextStyleSession.resetQuestDetailsBounds(state);
            return;
        }
        String target = resolvedTextStyleTarget(state, model);
        CanvasTextLayer text = target.isBlank() ? null : model.text(target);
        if (text == null) {
            TextStyleSession.closeQuestDetails(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text style close target={} reason=missing_text", target);
            return;
        }
        TextStyleSession.openQuestDetails(state, text.id());
        CanvasTextStyleMenu.renderQuestDetails(modal, state, text, x, y, w, h, state.questDetails.questDetailsDescScroll, next -> {
            updateText(player, state, questId, model, next, Math.max(1, w - 1));
            TextStyleSession.openQuestDetails(state, next.id());
        }, () -> {
            state.questDetails.questDetailsTextColorQuestId = questId;
            state.questDetails.questDetailsTextColorTextId = text.id();
            ModalOpenActions.openColorPicker(state, ModalTargets.questDescText(questId, text.id()), CanvasRenderer.activeTextColor(state, text));
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text color open picker quest={} text={}", questId, text.id());
            refresh.run();
        }, refresh);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int viewportW, int viewportH) {
        String kind = state.questDetails.questDetailsContextKind;
        if (!state.questDetails.questDetailsContextOpen || kind == null || !kind.startsWith("desc") || !QuestDetailsEditController.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        int viewportOriginX = state.questDetails.questDetailsViewportOriginX;
        int viewportOriginY = state.questDetails.questDetailsViewportOriginY;
        switch (kind) {
            case "description" -> addDescriptionActions(actions, state, player, questId, model, viewportOriginX, viewportOriginY);
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
        state.questDetails.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetails.questDetailsContextScroll = ScrollMath.clamp(state.questDetails.questDetailsContextScroll, state.questDetails.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int localX = localContextCoordinate(state.questDetails.questDetailsContextAnchorX, viewportOriginX, viewportW);
        int localY = localContextCoordinate(state.questDetails.questDetailsContextAnchorY, viewportOriginY, viewportH);
        int mx = ContextMenuPlacement.fitRightOrLeft(localX, viewportW, menuW);
        int my = ContextMenuPlacement.fitBelowOrAbove(localY, viewportH, menuH);
        state.questDetails.questDetailsContextX = viewportOriginX + mx;
        state.questDetails.questDetailsContextY = viewportOriginY + my;
        state.questDetails.questDetailsContextW = menuW;
        state.questDetails.questDetailsContextH = menuH;
        WidgetGroup canvasMenuLayer = new WidgetGroup(x, y, viewportW, viewportH);
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, actions, state.questDetails.questDetailsContextScroll, visibleRows, TabletColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientManager.closeContext(state);
            }
            refresh.run();
        }, viewportW, viewportH, ScrollState.bind(
                () -> state.questDetails.questDetailsContextScroll,
                value -> state.questDetails.questDetailsContextScroll = ScrollMath.clamp(value, state.questDetails.questDetailsContextScrollMax),
                () -> state.contextMenu.contextMenuScrollDragging,
                dragging -> state.contextMenu.contextMenuScrollDragging = dragging
        ), refresh);
        canvasMenuLayer.addWidget(menu);
        modal.addWidget(canvasMenuLayer);
    }

    private static void addDescriptionActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, int x, int y) {
        List<ContextAction> addActions = new ArrayList<>();
        addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_TEXT_BOX), "text", TabletColors.SUCCESS, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addTextAt(player, state, questId, model, x, y);
        }));
        addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_IMAGE), "image", TabletColors.SUCCESS, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addImageAt(state, questId, x, y);
        }));
        addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_ENTITY), "entity", TabletColors.SUCCESS, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addEntityAt(state, questId, x, y);
        }));
        addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_ITEM), "icon", TabletColors.SUCCESS, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addItemAt(state, questId, x, y);
        }));
        addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_BLOCK), "add_block", TabletColors.SUCCESS, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.addBlockAt(state, questId, x, y);
        }));
        if (RecipeViewerIntegrations.hasAvailableViewer()) {
            addActions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD_RECIPE_CARD), "recipe", TabletColors.SUCCESS, () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsDescriptionPanel.addRecipeCardAt(state, questId, x, y);
            }));
        }
        actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ADD), "add", TabletColors.SUCCESS, addActions));
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_BACKGROUND), "background", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            ModalOpenActions.openAssetPicker(state, ModalTargets.descBackground(questId), model.canvasBackground == null ? "" : model.canvasBackground);
        }));
        if (model.canvasBackground != null && !model.canvasBackground.isBlank() && !"default".equals(model.canvasBackground)) {
            SkinFillOverride parsed = BackgroundModes.decode(model.canvasBackground);
            String currentMode = parsed != null ? parsed.mode() : "stretch";
            String path = parsed != null ? parsed.path() : model.canvasBackground;
            List<ContextAction> modeActions = new ArrayList<>();
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_stretch"),
                    "size",
                    currentMode.equals("stretch") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        model.canvasBackground = BackgroundModes.encode("stretch", path);
                        QuestDetailsDescriptionModel.save(player, questId, model);
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_tile"),
                    "grid",
                    currentMode.equals("tile") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        model.canvasBackground = BackgroundModes.encode("tile", path);
                        QuestDetailsDescriptionModel.save(player, questId, model);
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_original_size"),
                    "original_size",
                    currentMode.equals("center") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        model.canvasBackground = BackgroundModes.encode("center", path);
                        QuestDetailsDescriptionModel.save(player, questId, model);
                    }));
            modeActions.add(ContextActionFactory.action(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.mode_dynamic"),
                    "dynamic",
                    currentMode.equals("dynamic") ? TabletColors.SUCCESS : TabletColors.TEXT_SECONDARY,
                    () -> {
                        ContextMenuController.clearDeleteConfirm(state);
                        model.canvasBackground = BackgroundModes.encode("dynamic", path);
                        QuestDetailsDescriptionModel.save(player, questId, model);
                    }));
            actions.add(ContextActionFactory.submenu(
                    TabletTranslationKeys.text("ui.questsandstuff.skin.change_mode"),
                    "layout-dashboard",
                    TabletColors.TEXT_PRIMARY,
                    modeActions));
        }
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_GRID_COLOR), "style_color", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            int color = TabletGridControls.defaultGridColor(state);
            ModalOpenActions.openColorPicker(state, ModalTargets.gridColor(), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=change_grid_color color={}", color);
        }));
        if (model.canvasBackground != null && !model.canvasBackground.isBlank() && !"default".equals(model.canvasBackground)) {
            String deleteKey = "quest_details_background:" + questId;
            actions.add(ContextActionFactory.warningDelete(state, deleteKey, TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_REMOVE_BACKGROUND), () -> {
                model.canvasBackground = "default";
                QuestDetailsDescriptionModel.save(player, questId, model);
            }));
        }
        if (state.clipboard.canvasClipboard.hasCanvasLayers()) {
            actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_PASTE), "paste", TabletColors.SUCCESS, () -> {
                ContextMenuController.clearDeleteConfirm(state);
                QuestDetailsDescriptionClipboard.pasteAtContext(player, state, questId, model, x, y);
            }));
        }
    }

    private static void addTextActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model) {
        actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_EDIT_TEXT), "rename", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            CanvasTextLayer text = model.text(state.questDetails.questDetailsContextId);
            if (text == null) {
                TextEditSession.closeQuestDetails(state, true);
                TextStyleSession.closeQuestDetails(state);
                return;
            }
            TextEditSession.beginQuestDetails(state, text.id(), text.text());
            TextStyleSession.openQuestDetails(state, text.id());
        }));
        actions.add(new ContextAction(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_TEXT_STYLE), "style", TabletColors.INTERACTIVE, false, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            TextStyleSession.openQuestDetails(state, state.questDetails.questDetailsContextId);
        }));
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_FIT_TO_GRID), "fit_grid", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitTextToGrid(player, state, questId, model, state.questDetails.questDetailsContextId);
        }));
        addOrderActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActionFactory.copy(() -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionClipboard.copyText(state, model, state.questDetails.questDetailsContextId);
        }));
        String deleteKey = "quest_details_text:" + questId + ":" + state.questDetails.questDetailsContextId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            model.removeText(state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void addImageActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, Runnable refresh) {
        CanvasImageLayer contextImage = model.image(state.questDetails.questDetailsContextId);
        boolean entityImage = contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset());
        boolean itemImage = contextImage != null && (ModelAssetPreviewRenderer.isItemAsset(contextImage.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(contextImage.asset()));
        boolean blockImage = contextImage != null && ModelAssetPreviewRenderer.isBlockModelAsset(contextImage.asset());
        boolean recipeImage = contextImage != null && CanvasRecipeCardAsset.isRecipeCardAsset(contextImage.asset());
        actions.add(ContextActionFactory.promoted(changeImageLabel(entityImage, itemImage, blockImage, recipeImage), changeImageIcon(entityImage, itemImage, blockImage, recipeImage), TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            if (entityImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descEntity(questId, state.questDetails.questDetailsContextId));
            } else if (itemImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descItem(questId, state.questDetails.questDetailsContextId));
            } else if (blockImage) {
                QuestDetailsWindow.openBlockPicker(state, ModalTargets.descBlock(questId, state.questDetails.questDetailsContextId));
            } else if (recipeImage) {
                QuestDetailsWindow.openRecipePicker(state, ModalTargets.descRecipe(questId, state.questDetails.questDetailsContextId));
            } else {
                QuestDetailsWindow.openAssetPicker(state, ModalTargets.descImage(questId, state.questDetails.questDetailsContextId));
            }
        }));
        if (entityImage) {
            String entityId = EntityPreviewRenderer.entityId(contextImage.asset());
            if (EntityVariantCatalog.hasVariants(entityId)) {
                actions.add(ContextActionFactory.changeVariant(() -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    String imageId = state.questDetails.questDetailsContextId;
                    QuestDetailsTransientManager.closeContext(state);
                    ModalOpenActions.openEntityVariantPicker(state, ModalTargets.questDetailsImage(questId, imageId), contextImage.asset());
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=change_entity_variant quest={} image={} entity={}", questId, imageId, entityId);
                }));
            }
            actions.add(ContextActionFactory.editMotion(() -> {
                ContextMenuController.clearDeleteConfirm(state);
                EntityMotionEditor.openQuestDetails(state, questId, state.questDetails.questDetailsContextId, state.questDetails.questDetailsContextX, state.questDetails.questDetailsContextY);
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=edit_entity_motion quest={} image={}", questId, state.questDetails.questDetailsContextId);
            }));
        }
        if (contextImage != null && CanvasTransformGizmo.supports(contextImage.asset())
                && descriptionSelectionCount(state) == 1
                && QuestDetailsDescriptionSelectionState.selectedImageIds(state).contains(contextImage.id())) {
            ContextMenuSections gizmoSections = new ContextMenuSections();
            CanvasTransformGizmoMenus.addModeActions(gizmoSections, ContextMenuSection.ARRANGE, state, refresh);
            CanvasTransformGizmoMenus.addCenterPivotAction(gizmoSections, ContextMenuSection.ARRANGE, state, () -> {
                CanvasImageLayer image = model.image(state.questDetails.questDetailsContextId);
                if (image != null) {
                    model.putImage(image.withCenteredPivot());
                    QuestDetailsDescriptionModel.save(player, questId, model);
                    state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(image.id());
                    state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
                    state.questDetails.questDetailsDescriptionSelection.imageIds().add(image.id());
                    state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
                    state.questDetails.questDetailsDescriptionSelection.textIds().clear();
                }
            }, refresh);
            actions.addAll(gizmoSections.build());
        }
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_FIT_TO_GRID), "fit_grid", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitImageToGrid(player, state, questId, model, state.questDetails.questDetailsContextId);
        }));
        addOrderActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActionFactory.copy(() -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionClipboard.copyImage(state, model, state.questDetails.questDetailsContextId);
        }));
        String deleteKey = "quest_details_image:" + questId + ":" + state.questDetails.questDetailsContextId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            model.removeImage(state.questDetails.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static String changeImageLabel(boolean entityImage, boolean itemImage, boolean blockImage, boolean recipeImage) {
        if (entityImage) {
            return TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_ENTITY);
        }
        if (itemImage) {
            return TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_ITEM);
        }
        if (blockImage) {
            return TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_BLOCK);
        }
        if (recipeImage) {
            return TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_RECIPE_CARD);
        }
        return TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_IMAGE);
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
            ContextMenuSections gizmoSections = new ContextMenuSections();
            CanvasTransformGizmoMenus.addModeActions(gizmoSections, ContextMenuSection.ARRANGE, state, refresh);
            actions.addAll(gizmoSections.build());
        }
        addBatchDescElementActions(actions, state, questId, model);
        actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ALIGN), "align-center-horizontal", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ALIGN_HORIZONTAL_CENTER), "align-center-horizontal", TabletColors.INTERACTIVE, () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, true);
                }),
                ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_ALIGN_VERTICAL_CENTER), "align-center-vertical", TabletColors.INTERACTIVE, () -> {
                    ContextMenuController.clearDeleteConfirm(state);
                    QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, false);
                })
        )));
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_FIT_TO_GRID), "fit_grid", TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.fitSelectionToGrid(player, state, questId, model);
        }));
        addOrderActions(actions, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, true);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, false);
            QuestDetailsDescriptionModel.save(player, questId, model);
        });
        actions.add(ContextActionFactory.copy(() -> {
            ContextMenuController.clearDeleteConfirm(state);
            QuestDetailsDescriptionPanel.copyDescriptionSelection(state, model);
        }));
        String deleteKey = "quest_details_selection:" + questId;
        actions.add(ContextActionFactory.delete(state, deleteKey, TabletTranslationKeys.text(TabletTranslationKeys.COMMON_REMOVE), () -> {
            QuestDetailsDescriptionPanel.deleteDescriptionSelection(state, model);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void addBatchDescElementActions(List<ContextAction> actions, TabletUiState state, String questId, QuestDetailsDescriptionModel model) {
        Set<String> imageIds = QuestDetailsDescriptionSelectionState.selectedImageIds(state);
        Set<String> textIds = QuestDetailsDescriptionSelectionState.selectedTextIds(state);

        if (imageIds.size() >= 2) {
            addBatchDescImageOnlyActions(actions, state, questId, model, imageIds);
        }
        if (textIds.size() >= 2) {
            addBatchDescTextOnlyActions(actions, state, questId, textIds);
        }
    }

    private static void addBatchDescImageOnlyActions(List<ContextAction> actions, TabletUiState state, String questId, QuestDetailsDescriptionModel model, Set<String> imageIds) {
        String primaryId = state.questDetails.questDetailsDescriptionSelection.primaryImageId();
        if (primaryId.isBlank()) {
            primaryId = imageIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        CanvasImageLayer primary = model.image(finalPrimaryId);
        if (primary == null) return;

        boolean entityImage = EntityPreviewRenderer.isEntityAsset(primary.asset());
        boolean itemImage = ModelAssetPreviewRenderer.isItemAsset(primary.asset()) || ModelAssetPreviewRenderer.isItemTagAsset(primary.asset());
        boolean blockImage = ModelAssetPreviewRenderer.isBlockModelAsset(primary.asset());
        boolean recipeImage = CanvasRecipeCardAsset.isRecipeCardAsset(primary.asset());

        actions.add(ContextActionFactory.action(changeImageLabel(entityImage, itemImage, blockImage, recipeImage), changeImageIcon(entityImage, itemImage, blockImage, recipeImage), TabletColors.INTERACTIVE, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            if (entityImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descEntity(questId, finalPrimaryId));
            } else if (itemImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descItem(questId, finalPrimaryId));
            } else if (blockImage) {
                QuestDetailsWindow.openBlockPicker(state, ModalTargets.descBlock(questId, finalPrimaryId));
            } else if (recipeImage) {
                QuestDetailsWindow.openRecipePicker(state, ModalTargets.descRecipe(questId, finalPrimaryId));
            } else {
                QuestDetailsWindow.openAssetPicker(state, ModalTargets.descImage(questId, finalPrimaryId));
            }
        }));
    }

    private static void addBatchDescTextOnlyActions(List<ContextAction> actions, TabletUiState state, String questId, Set<String> textIds) {
        String primaryId = state.questDetails.questDetailsDescriptionSelection.primaryTextId();
        if (primaryId.isBlank()) {
            primaryId = textIds.iterator().next();
        }
        String finalPrimaryId = primaryId;
        actions.add(new ContextAction(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_TEXT_STYLE), "style", TabletColors.INTERACTIVE, false, () -> {
            ContextMenuController.clearDeleteConfirm(state);
            TextStyleSession.openQuestDetails(state, finalPrimaryId);
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
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_BRING_TO_FRONT), "up", TabletColors.INTERACTIVE, bringToFront));
        actions.add(ContextActionFactory.action(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_SEND_TO_BACK), "down", TabletColors.TEXT_MUTED, sendToBack));
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
        String activeEdit = TextEditSession.isQuestDetailsEditing(state)
                ? state.questDetails.questDetailsTextEditTarget
                : "";
        String[] candidates = {
                state.questDetails.questDetailsTextStyleTarget,
                state.questDetails.questDetailsTextFontSizeFieldTarget,
                activeEdit,
                state.questDetails.questDetailsDescriptionSelection.primaryTextId()
        };
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank() && model.text(candidate) != null) {
                return candidate;
            }
        }
        for (String selected : state.questDetails.questDetailsDescriptionSelection.textIds()) {
            if (selected != null && !selected.isBlank() && model.text(selected) != null) {
                return selected;
            }
        }
        return "";
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
