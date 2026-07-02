package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class QuestDetailsDescriptionEditActions {
    private static final int RECIPE_CARD_W = 136;
    private static final int RECIPE_CARD_H = 92;

    private QuestDetailsDescriptionEditActions() {
    }

    static void addTextAt(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int panelX, int panelY) {
        String id = nextDescriptionTextId(model);
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - 48);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - 16);
        CanvasTextLayer text = new CanvasTextLayer(id, "Text", Math.max(0, x), Math.max(0, y), 96, 32, 0, "left", "normal", ModColors.TEXT_PRIMARY);
        text = QuestDetailsDescriptionLayout.fitAndClampText(state, text, QuestDetailsWindow.descriptionContentWidth(state));
        model.putText(text);
        model.ensureOrder(QuestDetailsDescriptionModel.ORDER_TEXT + id);
        QuestDetailsDescriptionModel.save(player, questId, model);
        QuestDetailsDescriptionSelectionState.selectOnlyText(state, id);
        TextEditSession.beginQuestDetails(state, id, text.text());
        TextStyleSession.openQuestDetails(state, id);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add text quest={} text={} pos={},{}", questId, id, x, y);
    }

    static void addImageAt(TabletUiState state, String questId, int panelX, int panelY) {
        String id = nextDescriptionImageId(modelForQuest(questId));
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - 40);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - 24);
        QuestDetailsWindow.openAssetPicker(state, ModalTargets.descImageNew(questId, id, Math.max(0, x), Math.max(0, y)));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add image pending quest={} image={} pos={},{}", questId, id, x, y);
    }

    static void addEntityAt(TabletUiState state, String questId, int panelX, int panelY) {
        String id = nextDescriptionEntityId(modelForQuest(questId));
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - 32);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - 32);
        QuestDetailsWindow.openIconPicker(state, ModalTargets.descEntityNew(questId, id, Math.max(0, x), Math.max(0, y)));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add entity pending quest={} image={} pos={},{}", questId, id, x, y);
    }

    static void addItemAt(TabletUiState state, String questId, int panelX, int panelY) {
        String id = nextDescriptionItemId(modelForQuest(questId));
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - 24);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - 24);
        QuestDetailsWindow.openIconPicker(state, ModalTargets.descItemNew(questId, id, Math.max(0, x), Math.max(0, y)));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add item model pending quest={} image={} pos={},{}", questId, id, x, y);
    }

    static void addBlockAt(TabletUiState state, String questId, int panelX, int panelY) {
        String id = nextDescriptionBlockId(modelForQuest(questId));
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - 24);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - 24);
        QuestDetailsWindow.openBlockPicker(state, ModalTargets.descBlockNew(questId, id, Math.max(0, x), Math.max(0, y)));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add block model pending quest={} image={} pos={},{}", questId, id, x, y);
    }

    static void addRecipeCardAt(TabletUiState state, String questId, int panelX, int panelY) {
        String id = nextDescriptionRecipeId(modelForQuest(questId));
        int x = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorX - panelX - RECIPE_CARD_W / 2);
        int y = QuestDetailsDescriptionLayout.snap(state, state.questDetails.questDetailsContextAnchorY - panelY + state.questDetails.questDetailsDescScroll - RECIPE_CARD_H / 2);
        QuestDetailsWindow.openRecipePicker(state, ModalTargets.descRecipeNew(questId, id, Math.max(0, x), Math.max(0, y)));
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details add recipe card pending quest={} image={} pos={},{}", questId, id, x, y);
    }

    static void fitTextToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, String id) {
        CanvasTextLayer text = model.text(id);
        if (text == null) {
            return;
        }
        CanvasTextLayer fitted = QuestDetailsDescriptionLayout.fittedText(state, text);
        model.putText(QuestDetailsDescriptionLayout.clampTextToColumn(state, fitted, QuestDetailsWindow.descriptionContentWidth(state)));
        QuestDetailsDescriptionModel.save(player, questId, model);
        state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(id);
    }

    static void fitImageToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, String id) {
        CanvasImageLayer image = model.image(id);
        if (image == null) {
            return;
        }
        CanvasImageLayer fitted = QuestDetailsDescriptionLayout.fittedImage(state, image);
        model.putImage(QuestDetailsDescriptionLayout.clampImageToColumn(state, fitted, QuestDetailsWindow.descriptionContentWidth(state)));
        QuestDetailsDescriptionModel.save(player, questId, model);
        state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(id);
    }

    static void fitSelectionToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model) {
        int contentW = QuestDetailsWindow.descriptionContentWidth(state);
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                CanvasTextLayer fitted = QuestDetailsDescriptionLayout.fittedText(state, text);
                model.putText(QuestDetailsDescriptionLayout.clampTextToColumn(state, fitted, contentW));
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                CanvasImageLayer fitted = QuestDetailsDescriptionLayout.fittedImage(state, image);
                model.putImage(QuestDetailsDescriptionLayout.clampImageToColumn(state, fitted, contentW));
            }
        }
        QuestDetailsDescriptionModel.save(player, questId, model);
    }

    static void alignSelectionToCanvas(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int viewportW, int viewportH, boolean horizontal) {
        int[] bounds = selectionBounds(state, model);
        if (bounds == null) {
            return;
        }
        int currentCenter = horizontal ? (bounds[0] + bounds[2]) / 2 : (bounds[1] + bounds[3]) / 2;
        int targetCenter = horizontal ? viewportW / 2 : state.questDetails.questDetailsDescScroll + viewportH / 2;
        int delta = targetCenter - currentCenter;
        if (delta == 0) {
            return;
        }
        int contentW = Math.max(1, viewportW - 1);
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                CanvasTextLayer moved = new CanvasTextLayer(
                        text.id(),
                        text.text(),
                        horizontal ? Math.max(0, text.x() + delta) : text.x(),
                        horizontal ? text.y() : Math.max(0, text.y() + delta),
                        text.w(),
                        text.h(),
                        text.rotation(),
                        text.align(),
                        text.style(),
                        text.color(),
                        text.fontSize(),
                        text.spans()
                );
                model.putText(QuestDetailsDescriptionLayout.clampTextToColumn(state, moved, contentW));
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                CanvasImageLayer moved = image.moveTo(
                        horizontal ? Math.max(0, image.x() + delta) : image.x(),
                        horizontal ? image.y() : Math.max(0, image.y() + delta)
                );
                model.putImage(QuestDetailsDescriptionLayout.clampImageToColumn(state, moved, contentW));
            }
        }
        QuestDetailsDescriptionModel.save(player, questId, model);
    }

    static void moveSelectionLayers(TabletUiState state, QuestDetailsDescriptionModel model, boolean front) {
        List<String> selected = QuestDetailsDescriptionSelectionState.selectedLayerKeys(state, model);
        if (selected.isEmpty()) {
            return;
        }
        model.order.removeAll(selected);
        if (front) {
            model.order.addAll(selected);
        } else {
            model.order.addAll(0, selected);
        }
    }

    static void copyDescriptionSelection(TabletUiState state, QuestDetailsDescriptionModel model) {
        List<CanvasImageLayer> copiedImages = new ArrayList<>();
        List<CanvasTextLayer> copiedTexts = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                copiedTexts.add(text);
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                copiedImages.add(image);
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
        }
        state.clipboard.canvasClipboard.store(false, copiedImages, copiedTexts, minX == Integer.MAX_VALUE ? 0 : minX, minY == Integer.MAX_VALUE ? 0 : minY);
    }

    static boolean copySelectedDescriptionToClipboard(TabletUiState state, QuestDetailsDescriptionModel model) {
        if (state == null || model == null || !QuestDetailsDescriptionSelectionState.hasSelection(state)) {
            return false;
        }
        copyDescriptionSelection(state, model);
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] quest details copied selection texts={} images={}",
                state.clipboard.canvasClipboard.textCount(), state.clipboard.canvasClipboard.imageCount());
        return state.clipboard.canvasClipboard.hasCanvasLayers();
    }

    static boolean selectAllDescription(TabletUiState state, QuestDetailsDescriptionModel model) {
        if (state == null || model == null || model.texts.isEmpty() && model.images.isEmpty()) {
            return false;
        }
        QuestDetailsDescriptionSelectionState.clear(state);
        for (String textId : model.texts.keySet()) {
            state.questDetails.questDetailsDescriptionSelection.textIds().add(textId);
            state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(textId);
        }
        for (String imageId : model.images.keySet()) {
            state.questDetails.questDetailsDescriptionSelection.imageIds().add(imageId);
            state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(imageId);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details select all description texts={} images={}", model.texts.size(), model.images.size());
        return true;
    }

    static boolean deleteSelectedDescription(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model) {
        if (state == null || model == null || !QuestDetailsDescriptionSelectionState.hasSelection(state)) {
            return false;
        }
        deleteDescriptionSelection(state, model);
        QuestDetailsDescriptionModel.save(player, questId, model);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details delete selected description quest={}", questId);
        return true;
    }

    static void deleteDescriptionSelection(TabletUiState state, QuestDetailsDescriptionModel model) {
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            model.removeText(textId);
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            model.removeImage(imageId);
        }
        QuestDetailsDescriptionSelectionState.clear(state);
    }

    private static int[] selectionBounds(TabletUiState state, QuestDetailsDescriptionModel model) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                int[] box = CanvasElementGeometry.logicalBounds(text.x(), text.y(), text.w(), text.h(), text.rotation());
                minX = Math.min(minX, box[0]);
                minY = Math.min(minY, box[1]);
                maxX = Math.max(maxX, box[2]);
                maxY = Math.max(maxY, box[3]);
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                int[] box = CanvasElementGeometry.logicalBoundsAtPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
                minX = Math.min(minX, box[0]);
                minY = Math.min(minY, box[1]);
                maxX = Math.max(maxX, box[2]);
                maxY = Math.max(maxY, box[3]);
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{minX, minY, maxX, maxY};
    }

    private static QuestDetailsDescriptionModel modelForQuest(String questId) {
        return QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
    }

    private static String nextDescriptionTextId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("txt", model == null ? List.of() : model.texts.keySet());
    }

    private static String nextDescriptionImageId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("img", model == null ? List.of() : model.images.keySet());
    }

    private static String nextDescriptionEntityId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("ent", model == null ? List.of() : model.images.keySet());
    }

    private static String nextDescriptionItemId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("itm", model == null ? List.of() : model.images.keySet());
    }

    private static String nextDescriptionBlockId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("blk", model == null ? List.of() : model.images.keySet());
    }

    private static String nextDescriptionRecipeId(QuestDetailsDescriptionModel model) {
        return StableIdAllocator.nextId("rcp", model == null ? List.of() : model.images.keySet());
    }
}
