package com.abo47.questsandstuff.client.tablet.quest.canvas.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasElementStore {
    private CanvasElementStore() {
    }

    public static void putCanvasImage(TabletUiState state, String chapter, CanvasImageLayer image) {
        putCanvasImage(state, chapter, image, true);
    }

    public static void putCanvasImage(TabletUiState state, String chapter, CanvasImageLayer image, boolean syncServer) {
        if (chapter == null || chapter.isBlank() || image == null) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()));
        boolean replaced = false;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).id().equals(image.id())) {
                images.set(i, image);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            images.add(image);
        }
        state.canvas.canvasImagesByChapter.put(chapter, images);
        CanvasLayerOrdering.ensurePresent(state, chapter, CanvasLayerOrdering.imageKey(image.id()));
        ClientQuestStateFacade.putCanvasImageLocal(chapter, image);
        persistLayerOrderLocal(state, chapter);
        if (syncServer) {
            sendCanvasImage(chapter, image);
        }
    }

    public static boolean removeCanvasImage(TabletUiState state, String chapter, String imageId) {
        if (chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        List<CanvasImageLayer> images = new ArrayList<>(state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()));
        boolean removed = images.removeIf(image -> image.id().equals(imageId));
        if (!removed) {
            return false;
        }
        if (images.isEmpty()) {
            state.canvas.canvasImagesByChapter.remove(chapter);
        } else {
            state.canvas.canvasImagesByChapter.put(chapter, images);
        }
        if (imageId.equals(state.canvas.canvasSelection.primaryImageId())) {
            state.canvas.canvasSelection.setPrimaryImageId("");
        }
        state.canvas.canvasSelection.imageIds().remove(imageId);
        CanvasLayerOrdering.remove(state, chapter, CanvasLayerOrdering.imageKey(imageId));
        ClientQuestStateFacade.removeCanvasImageLocal(chapter, imageId);
        persistLayerOrderLocal(state, chapter);
        sendCanvasImageRemove(chapter, imageId);
        return true;
    }

    public static void putCanvasText(TabletUiState state, String chapter, CanvasTextLayer text) {
        putCanvasText(state, chapter, text, true);
    }

    public static void putCanvasText(TabletUiState state, String chapter, CanvasTextLayer text, boolean syncServer) {
        if (chapter == null || chapter.isBlank() || text == null) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of()));
        boolean replaced = false;
        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).id().equals(text.id())) {
                texts.set(i, text);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            texts.add(text);
        }
        state.canvas.canvasTextsByChapter.put(chapter, texts);
        CanvasLayerOrdering.ensurePresent(state, chapter, CanvasLayerOrdering.textKey(text.id()));
        ClientQuestStateFacade.putCanvasTextLocal(chapter, text);
        persistLayerOrderLocal(state, chapter);
        if (syncServer) {
            sendCanvasText(chapter, text);
        }
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String chapter, CanvasExclusiveChoice ec) {
        putCanvasExclusiveChoice(state, chapter, ec, true);
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String chapter, CanvasExclusiveChoice ec, boolean syncServer) {
        if (chapter == null || chapter.isBlank() || ec == null) {
            return;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()));
        boolean replaced = false;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).id().equals(ec.id())) {
                choices.set(i, ec);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            choices.add(ec);
        }
        state.canvas.canvasExclusiveChoicesByChapter.put(chapter, choices);
        CanvasLayerOrdering.ensurePresent(state, chapter, CanvasLayerOrdering.exclusiveChoiceKey(ec.id()));
        ClientQuestStateFacade.putCanvasExclusiveChoiceLocal(chapter, ec);
        persistLayerOrderLocal(state, chapter);
        if (syncServer) {
            sendCanvasExclusiveChoice(chapter, ec);
        }
    }

    public static void putCanvasExclusiveChoices(TabletUiState state, String chapter, List<CanvasExclusiveChoice> ecs, boolean syncServer) {
        if (chapter == null || chapter.isBlank() || ecs == null || ecs.isEmpty()) {
            return;
        }
        List<CanvasExclusiveChoice> existing = new ArrayList<>(state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()));
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec == null) continue;
            boolean replaced = false;
            for (int i = 0; i < existing.size(); i++) {
                if (existing.get(i).id().equals(ec.id())) {
                    existing.set(i, ec);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                existing.add(ec);
            }
            CanvasLayerOrdering.ensurePresent(state, chapter, CanvasLayerOrdering.exclusiveChoiceKey(ec.id()));
        }
        state.canvas.canvasExclusiveChoicesByChapter.put(chapter, existing);
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec == null) continue;
            ClientQuestStateFacade.putCanvasExclusiveChoiceLocal(chapter, ec);
        }
        persistLayerOrderLocal(state, chapter);
        if (syncServer) {
            sendCanvasExclusiveChoices(chapter, ecs);
        }
    }

    public static boolean removeCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        if (chapter == null || chapter.isBlank() || ecId == null || ecId.isBlank()) {
            return false;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()));
        boolean removed = choices.removeIf(ec -> ec.id().equals(ecId));
        if (!removed) {
            return false;
        }
        if (choices.isEmpty()) {
            state.canvas.canvasExclusiveChoicesByChapter.remove(chapter);
        } else {
            state.canvas.canvasExclusiveChoicesByChapter.put(chapter, choices);
        }
        if (ecId.equals(state.canvas.canvasSelection.primaryEcId())) {
            state.canvas.canvasSelection.setPrimaryEcId("");
        }
        state.canvas.canvasSelection.ecIds().remove(ecId);
        CanvasLayerOrdering.remove(state, chapter, CanvasLayerOrdering.exclusiveChoiceKey(ecId));
        ClientQuestStateFacade.removeCanvasExclusiveChoiceLocal(chapter, ecId);
        persistLayerOrderLocal(state, chapter);
        sendCanvasExclusiveChoiceRemove(chapter, ecId);
        return true;
    }

    public static CanvasExclusiveChoice findCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        if (chapter == null || chapter.isBlank() || ecId == null || ecId.isBlank()) {
            return null;
        }
        return state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(ec -> ec.id().equals(ecId))
                .findFirst()
                .orElse(null);
    }

    public static boolean removeCanvasText(TabletUiState state, String chapter, String textId) {
        if (chapter == null || chapter.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of()));
        boolean removed = texts.removeIf(text -> text.id().equals(textId));
        if (!removed) {
            return false;
        }
        if (texts.isEmpty()) {
            state.canvas.canvasTextsByChapter.remove(chapter);
        } else {
            state.canvas.canvasTextsByChapter.put(chapter, texts);
        }
        if (textId.equals(state.canvas.canvasSelection.primaryTextId())) {
            state.canvas.canvasSelection.setPrimaryTextId("");
        }
        state.canvas.canvasSelection.textIds().remove(textId);
        if (textId.equals(state.canvas.canvasTextMenuTarget)) {
            TextStyleSession.closeMainCanvas(state);
        }
        CanvasLayerOrdering.remove(state, chapter, CanvasLayerOrdering.textKey(textId));
        ClientQuestStateFacade.removeCanvasTextLocal(chapter, textId);
        persistLayerOrderLocal(state, chapter);
        sendCanvasTextRemove(chapter, textId);
        return true;
    }

    public static CanvasTextLayer findCanvasText(TabletUiState state, String chapter, String textId) {
        if (chapter == null || chapter.isBlank() || textId == null || textId.isBlank()) {
            return null;
        }
        return state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(text -> text.id().equals(textId))
                .findFirst()
                .orElse(null);
    }

    public static void updateCanvasText(TabletUiState state, String chapter, String textId, UnaryOperator<CanvasTextLayer> updater) {
        CanvasTextLayer current = findCanvasText(state, chapter, textId);
        if (current == null || updater == null) {
            return;
        }
        CanvasTextLayer next = updater.apply(current);
        if (next != null) {
            putCanvasText(state, chapter, next);
        }
    }

    public static void persistCanvasImage(TabletUiState state, String chapter, String imageId) {
        CanvasImageLayer image = findCanvasImage(state, chapter, imageId);
        if (image == null) {
            return;
        }
        ClientQuestStateFacade.putCanvasImageLocal(chapter, image);
        sendCanvasImage(chapter, image);
    }

    public static void persistCanvasText(TabletUiState state, String chapter, String textId) {
        CanvasTextLayer text = findCanvasText(state, chapter, textId);
        if (text == null) {
            return;
        }
        ClientQuestStateFacade.putCanvasTextLocal(chapter, text);
        sendCanvasText(chapter, text);
    }

    public static void persistCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        CanvasExclusiveChoice ec = findCanvasExclusiveChoice(state, chapter, ecId);
        if (ec == null) {
            return;
        }
        ClientQuestStateFacade.putCanvasExclusiveChoiceLocal(chapter, ec);
        sendCanvasExclusiveChoice(chapter, ec);
    }

    public static void persistLayerOrder(TabletUiState state, String chapter) {
        persistLayerOrderLocal(state, chapter);
        sendLayerOrder(state, chapter);
    }

    public static CanvasImageLayer findCanvasImage(TabletUiState state, String chapter, String imageId) {
        if (chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of()).stream()
                .filter(image -> image.id().equals(imageId))
                .findFirst()
                .orElse(null);
    }

    private static void persistLayerOrderLocal(TabletUiState state, String chapter) {
        ClientQuestStateFacade.setCanvasLayerOrderLocal(chapter, state.canvas.canvasLayerOrderByChapter.getOrDefault(chapter, List.of()));
    }

    private static void sendCanvasExclusiveChoice(String chapter, CanvasExclusiveChoice ec) {
        CompoundTag payload = EditorCommandPayloads.canvasExclusiveChoicePut(chapter, ec);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT, payload));
    }

    private static void sendCanvasExclusiveChoices(String chapter, List<CanvasExclusiveChoice> ecs) {
        CompoundTag payload = EditorCommandPayloads.canvasExclusiveChoicesPut(chapter, ecs);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT_MANY, payload));
    }

    private static void sendCanvasExclusiveChoiceRemove(String chapter, String ecId) {
        CompoundTag payload = EditorCommandPayloads.canvasExclusiveChoiceRemove(chapter, ecId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_REMOVE, payload));
    }

    private static void sendCanvasImage(String chapter, CanvasImageLayer image) {
        CompoundTag payload = EditorCommandPayloads.canvasImagePut(chapter, image);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_IMAGE_PUT, payload));
    }

    private static void sendCanvasImageRemove(String chapter, String imageId) {
        CompoundTag payload = EditorCommandPayloads.canvasImageRemove(chapter, imageId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_IMAGE_REMOVE, payload));
    }

    private static void sendCanvasText(String chapter, CanvasTextLayer text) {
        CompoundTag payload = EditorCommandPayloads.canvasTextPut(chapter, text);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_TEXT_PUT, payload));
    }

    private static void sendCanvasTextRemove(String chapter, String textId) {
        CompoundTag payload = EditorCommandPayloads.canvasTextRemove(chapter, textId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_TEXT_REMOVE, payload));
    }

    private static void sendLayerOrder(TabletUiState state, String chapter) {
        CompoundTag payload = EditorCommandPayloads.canvasLayerOrder(chapter, state.canvas.canvasLayerOrderByChapter.getOrDefault(chapter, List.of()));
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_LAYER_ORDER, payload));
    }
}
