package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

public final class QuestDetailsDescriptionClipboard {
    private QuestDetailsDescriptionClipboard() {
    }

    public static void copyText(TabletUiState state, QuestDetailsDescriptionModel model, String id) {
        CanvasTextLayer text = model.text(id);
        if (text == null) {
            return;
        }
        state.canvasQuestClipboardAvailable = false;
        state.canvasImageClipboard.clear();
        state.canvasTextClipboard.clear();
        state.canvasTextClipboard.add(text);
        state.canvasClipboardOriginX = text.x();
        state.canvasClipboardOriginY = text.y();
    }

    public static void copyImage(TabletUiState state, QuestDetailsDescriptionModel model, String id) {
        CanvasImageLayer image = model.image(id);
        if (image == null) {
            return;
        }
        state.canvasQuestClipboardAvailable = false;
        state.canvasImageClipboard.clear();
        state.canvasTextClipboard.clear();
        state.canvasImageClipboard.add(image);
        state.canvasClipboardOriginX = image.x();
        state.canvasClipboardOriginY = image.y();
    }

    public static void pasteAtContext(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int panelX, int panelY) {
        int anchorX = QuestDetailsDescriptionLayout.snap(state, state.questDetailsContextAnchorX - panelX);
        int anchorY = QuestDetailsDescriptionLayout.snap(state, state.questDetailsContextAnchorY - panelY + state.questDetailsDescScroll);
        pasteAt(player, state, questId, model, anchorX, anchorY, QuestDetailsWindow.descriptionContentWidth(state));
    }

    public static boolean pasteFromKeyboard(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int viewportW, int viewportH) {
        if (state == null || model == null || state.canvasImageClipboard.isEmpty() && state.canvasTextClipboard.isEmpty()) {
            return false;
        }
        int[] anchor = keyboardPasteAnchor(state, model, viewportW, viewportH);
        pasteAt(player, state, questId, model, anchor[0], anchor[1], Math.max(1, viewportW - 1));
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] quest details pasted shortcut quest={} texts={} images={} anchor={},{}",
                questId, state.canvasTextClipboard.size(), state.canvasImageClipboard.size(), anchor[0], anchor[1]);
        return true;
    }

    public static boolean nudgeSelected(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int dx, int dy) {
        if (state == null || model == null || dx == 0 && dy == 0 || !QuestDetailsDescriptionSelectionState.hasSelection(state)) {
            return false;
        }
        int contentW = QuestDetailsWindow.descriptionContentWidth(state);
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                CanvasPoint point = QuestDetailsDescriptionLayout.clampAnchorToColumn(state, text.x() + dx, text.y() + dy, text.w(), text.h(), text.w() / 2, text.h() / 2, text.rotation(), contentW);
                model.putText(text.moveTo(point.x, point.y));
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                CanvasImageLayer moved = image.moveTo(image.x() + dx, image.y() + dy);
                model.putImage(QuestDetailsDescriptionLayout.clampImageToColumn(state, moved, contentW));
            }
        }
        QuestDetailsDescriptionModel.save(player, questId, model);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details nudge description quest={} dx={} dy={}", questId, dx, dy);
        return true;
    }

    private static void pasteAt(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int anchorX, int anchorY, int contentW) {
        int dx = anchorX - state.canvasClipboardOriginX;
        int dy = anchorY - state.canvasClipboardOriginY;
        QuestDetailsDescriptionSelectionState.clear(state);
        for (CanvasTextLayer text : state.canvasTextClipboard) {
            String id = StableIdAllocator.nextId("txt", model.texts.keySet());
            CanvasTextLayer pasted = new CanvasTextLayer(id, text.text(), text.x() + dx, text.y() + dy, text.w(), text.h(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            pasted = QuestDetailsDescriptionLayout.fitAndClampText(state, pasted, contentW);
            model.putText(pasted);
            model.ensureOrder(QuestDetailsDescriptionModel.ORDER_TEXT + id);
            state.questDetailsSelectedTextIds.add(id);
            state.questDetailsSelectedTextId = id;
        }
        for (CanvasImageLayer image : state.canvasImageClipboard) {
            String id = StableIdAllocator.nextId(imageIdPrefix(image), model.images.keySet());
            CanvasImageLayer pasted = new CanvasImageLayer(id, image.asset(), image.x() + dx, image.y() + dy, image.w(), image.h(), image.rotation(), image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), image.pivotX(), image.pivotY());
            pasted = QuestDetailsDescriptionLayout.fitAndClampImage(state, pasted, contentW);
            model.putImage(pasted);
            model.ensureOrder(QuestDetailsDescriptionModel.ORDER_IMAGE + id);
            state.questDetailsSelectedImageIds.add(id);
            state.questDetailsSelectedImageId = id;
        }
        QuestDetailsDescriptionModel.save(player, questId, model);
    }

    private static String imageIdPrefix(CanvasImageLayer image) {
        String id = image == null ? "" : image.id();
        return id.startsWith("ent_") ? "ent" : "img";
    }

    private static int[] keyboardPasteAnchor(TabletUiState state, QuestDetailsDescriptionModel model, int viewportW, int viewportH) {
        int[] bounds = selectionBounds(state, model);
        if (bounds != null) {
            return new int[]{QuestDetailsDescriptionLayout.snap(state, bounds[0] + 12), QuestDetailsDescriptionLayout.snap(state, bounds[1] + 12)};
        }
        int x = Math.max(0, viewportW / 2);
        int y = Math.max(0, state.questDetailsDescScroll + viewportH / 2);
        return new int[]{QuestDetailsDescriptionLayout.snap(state, x), QuestDetailsDescriptionLayout.snap(state, y)};
    }

    private static int[] selectionBounds(TabletUiState state, QuestDetailsDescriptionModel model) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
            CanvasTextLayer text = model.text(textId);
            if (text != null) {
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
        }
        for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
            CanvasImageLayer image = model.image(imageId);
            if (image != null) {
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
        }
        return minX == Integer.MAX_VALUE ? null : new int[]{minX, minY};
    }

}
