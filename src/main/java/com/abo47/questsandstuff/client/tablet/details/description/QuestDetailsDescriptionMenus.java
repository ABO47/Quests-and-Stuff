package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.overlay.CanvasTextStyleMenu;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.entity.variant.EntityVariantCatalog;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
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
        if (!state.canEdit || !state.questDetailsEditMode || !state.canvasTextEditOpen
                || state.questDetailsTextEditTarget.isBlank()
                || !state.questDetailsTextEditTarget.equals(state.canvasTextEditTarget)
                || model.text(state.questDetailsTextEditTarget) == null) {
            return;
        }
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = state.questDetailsTextEditTarget;
    }

    public static void renderStyleMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int w, int h) {
        if ((!state.questDetailsTextStyleOpen && state.questDetailsTextFontSizeSliderTarget.isBlank()) || !state.canEdit || !state.questDetailsEditMode) {
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
            updateText(player, questId, model, next);
            state.questDetailsTextStyleOpen = true;
            state.questDetailsTextStyleTarget = next.id();
        }, () -> {
            state.questDetailsTextColorQuestId = questId;
            state.questDetailsTextColorTextId = text.id();
            ModalOpenActions.openColorPicker(state, ModalTargets.questDescText(questId, text.id()), text.color());
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details text color open picker quest={} text={}", questId, text.id());
            refresh.run();
        }, refresh);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsDescriptionModel model, int x, int y, int viewportW, int viewportH) {
        String kind = state.questDetailsContextKind;
        if (!state.questDetailsContextOpen || kind == null || !kind.startsWith("desc") || !state.canEdit || !state.questDetailsEditMode) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        switch (kind) {
            case "description" -> addDescriptionActions(actions, state, player, questId, model, x, y);
            case "desc_text" -> addTextActions(actions, state, player, questId, model);
            case "desc_image" -> addImageActions(actions, state, player, questId, model);
            case "desc_selection" -> addSelectionActions(actions, state, player, questId, model, viewportW, viewportH);
            default -> {
            }
        }
        if (actions.isEmpty()) {
            return;
        }
        int menuW = 124;
        int menuH = ContextMenuPanel.heightForRows(actions.size());
        int mx = Math.max(4, Math.min(state.questDetailsContextX, state.questDetailsW - menuW - 4));
        int my = Math.max(4, Math.min(state.questDetailsContextY, state.questDetailsH - menuH - 4));
        state.questDetailsContextX = mx;
        state.questDetailsContextY = my;
        state.questDetailsContextW = menuW;
        state.questDetailsContextH = menuH;
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, actions, 0, actions.size(), ModColors.BORDER_BASE, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientState.closeContext(state);
            }
            refresh.run();
        });
        modal.addWidget(menu);
    }

    private static void addDescriptionActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, int x, int y) {
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_ADD_TEXT_BOX), "text", ModColors.SUCCESS, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.addTextAt(player, state, questId, model, x, y);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_ADD_IMAGE), "image", ModColors.SUCCESS, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.addImageAt(state, questId, x, y);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_ADD_ENTITY), "entity", ModColors.SUCCESS, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.addEntityAt(state, questId, x, y);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_BACKGROUND), "background", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            ModalOpenActions.openAssetPicker(state, ModalTargets.descBackground(questId), model.canvasBackground == null ? "" : model.canvasBackground);
        }));
        if (model.canvasBackground != null && !model.canvasBackground.isBlank() && !"default".equals(model.canvasBackground)) {
            String deleteKey = "quest_details_background:" + questId;
            actions.add(ContextActions.warningDelete(state, deleteKey, QuestVocabulary.text(QuestVocabulary.CONTEXT_REMOVE_BACKGROUND), () -> {
                model.canvasBackground = "default";
                QuestDetailsDescriptionModel.save(player, questId, model);
            }));
        }
        if (!state.canvasImageClipboard.isEmpty() || !state.canvasTextClipboard.isEmpty()) {
            actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_PASTE), "paste", ModColors.SUCCESS, () -> {
                state.contextDeleteConfirmKey = "";
                QuestDetailsDescriptionClipboard.pasteAtContext(player, state, questId, model, x, y);
            }));
        }
    }

    private static void addTextActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model) {
        actions.add(ContextActions.rename(QuestVocabulary.text(QuestVocabulary.CONTEXT_EDIT_TEXT), () -> {
            state.contextDeleteConfirmKey = "";
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
        actions.add(ContextActions.stayOpen(QuestVocabulary.text(QuestVocabulary.CONTEXT_TEXT_STYLE), "style", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            state.questDetailsTextStyleOpen = true;
            state.questDetailsTextStyleTarget = state.questDetailsContextId;
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "grid", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.fitTextToGrid(player, state, questId, model, state.questDetailsContextId);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_BRING_TO_FRONT), "up", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_SEND_TO_BACK), "down", ModColors.TEXT_MUTED, () -> {
            state.contextDeleteConfirmKey = "";
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_TEXT + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.copy(() -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionClipboard.copyText(state, model, state.questDetailsContextId);
        }));
        String deleteKey = "quest_details_text:" + questId + ":" + state.questDetailsContextId;
        actions.add(ContextActions.delete(state, deleteKey, QuestVocabulary.text(QuestVocabulary.COMMON_DELETE), () -> {
            model.removeText(state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void addImageActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model) {
        CanvasImageLayer contextImage = model.image(state.questDetailsContextId);
        boolean entityImage = contextImage != null && EntityPreviewRenderer.isEntityAsset(contextImage.asset());
        actions.add(ContextActions.action(entityImage ? QuestVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ENTITY) : QuestVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_IMAGE), entityImage ? "entity" : "image", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            if (entityImage) {
                QuestDetailsWindow.openIconPicker(state, ModalTargets.descEntity(questId, state.questDetailsContextId));
            } else {
                QuestDetailsWindow.openAssetPicker(state, ModalTargets.descImage(questId, state.questDetailsContextId));
            }
        }));
        if (entityImage) {
            String entityId = EntityPreviewRenderer.entityId(contextImage.asset());
            if (EntityVariantCatalog.hasVariants(entityId)) {
                actions.add(ContextActions.changeVariant(() -> {
                    state.contextDeleteConfirmKey = "";
                    String imageId = state.questDetailsContextId;
                    QuestDetailsTransientState.closeContext(state);
                    ModalOpenActions.openEntityVariantPicker(state, ModalTargets.questDetailsImage(questId, imageId), contextImage.asset());
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=change_entity_variant quest={} image={} entity={}", questId, imageId, entityId);
                }));
            }
            actions.add(ContextActions.editMotion(() -> {
                state.contextDeleteConfirmKey = "";
                EntityMotionEditor.openQuestDetails(state, questId, state.questDetailsContextId, state.questDetailsContextX, state.questDetailsContextY);
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details context action=edit_entity_motion quest={} image={}", questId, state.questDetailsContextId);
            }));
        }
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "grid", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.fitImageToGrid(player, state, questId, model, state.questDetailsContextId);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_BRING_TO_FRONT), "up", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            model.bringToFront(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_SEND_TO_BACK), "down", ModColors.TEXT_MUTED, () -> {
            state.contextDeleteConfirmKey = "";
            model.sendToBack(QuestDetailsDescriptionModel.ORDER_IMAGE + state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.copy(() -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionClipboard.copyImage(state, model, state.questDetailsContextId);
        }));
        String deleteKey = "quest_details_image:" + questId + ":" + state.questDetailsContextId;
        actions.add(ContextActions.delete(state, deleteKey, QuestVocabulary.text(QuestVocabulary.COMMON_DELETE), () -> {
            model.removeImage(state.questDetailsContextId);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void addSelectionActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, QuestDetailsDescriptionModel model, int viewportW, int viewportH) {
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_ALIGN_HORIZONTAL_CENTER), "align-center-horizontal", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, true);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_ALIGN_VERTICAL_CENTER), "align-center-vertical", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, false);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_FIT_TO_GRID), "grid", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.fitSelectionToGrid(player, state, questId, model);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_BRING_TO_FRONT), "up", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, true);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.action(QuestVocabulary.text(QuestVocabulary.CONTEXT_SEND_TO_BACK), "down", ModColors.TEXT_MUTED, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.moveSelectionLayers(state, model, false);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
        actions.add(ContextActions.copy(() -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsDescriptionPanel.copyDescriptionSelection(state, model);
        }));
        String deleteKey = "quest_details_selection:" + questId;
        actions.add(ContextActions.delete(state, deleteKey, QuestVocabulary.text(QuestVocabulary.COMMON_DELETE), () -> {
            QuestDetailsDescriptionPanel.deleteDescriptionSelection(state, model);
            QuestDetailsDescriptionModel.save(player, questId, model);
        }));
    }

    private static void updateText(Player player, String questId, QuestDetailsDescriptionModel model, CanvasTextLayer next) {
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
                state.questDetailsTextFontSizeSliderTarget,
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
}
