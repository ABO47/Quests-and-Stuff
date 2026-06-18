package com.abo47.questsandstuff.client.tablet.quest.canvas.layer;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public final class CanvasElementStore {
    private CanvasElementStore() {
    }

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image) {
        putCanvasImage(state, group, image, true);
    }

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image, boolean syncServer) {
        if (group == null || group.isBlank() || image == null) {
            return;
        }
        List<CanvasImageLayer> images = new ArrayList<>(state.canvas.canvasImagesByGroup.getOrDefault(group, List.of()));
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
        state.canvas.canvasImagesByGroup.put(group, images);
        CanvasLayerOrdering.ensurePresent(state, group, CanvasLayerOrdering.imageKey(image.id()));
        ClientQuestCache.putCanvasImageLocal(group, image);
        persistLayerOrderLocal(state, group);
        if (syncServer) {
            sendCanvasImage(group, image);
        }
    }

    public static boolean removeCanvasImage(TabletUiState state, String group, String imageId) {
        if (group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        List<CanvasImageLayer> images = new ArrayList<>(state.canvas.canvasImagesByGroup.getOrDefault(group, List.of()));
        boolean removed = images.removeIf(image -> image.id().equals(imageId));
        if (!removed) {
            return false;
        }
        if (images.isEmpty()) {
            state.canvas.canvasImagesByGroup.remove(group);
        } else {
            state.canvas.canvasImagesByGroup.put(group, images);
        }
        if (imageId.equals(state.canvas.canvasSelection.primaryImageId())) {
            state.canvas.canvasSelection.setPrimaryImageId("");
        }
        state.canvas.canvasSelection.imageIds().remove(imageId);
        CanvasLayerOrdering.remove(state, group, CanvasLayerOrdering.imageKey(imageId));
        ClientQuestCache.removeCanvasImageLocal(group, imageId);
        persistLayerOrderLocal(state, group);
        sendCanvasImageRemove(group, imageId);
        return true;
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text) {
        putCanvasText(state, group, text, true);
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text, boolean syncServer) {
        if (group == null || group.isBlank() || text == null) {
            return;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvas.canvasTextsByGroup.getOrDefault(group, List.of()));
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
        state.canvas.canvasTextsByGroup.put(group, texts);
        CanvasLayerOrdering.ensurePresent(state, group, CanvasLayerOrdering.textKey(text.id()));
        ClientQuestCache.putCanvasTextLocal(group, text);
        persistLayerOrderLocal(state, group);
        if (syncServer) {
            sendCanvasText(group, text);
        }
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String group, CanvasExclusiveChoice ec) {
        putCanvasExclusiveChoice(state, group, ec, true);
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String group, CanvasExclusiveChoice ec, boolean syncServer) {
        if (group == null || group.isBlank() || ec == null) {
            return;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of()));
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
        state.canvas.canvasExclusiveChoicesByGroup.put(group, choices);
        CanvasLayerOrdering.ensurePresent(state, group, CanvasLayerOrdering.exclusiveChoiceKey(ec.id()));
        ClientQuestCache.putCanvasExclusiveChoiceLocal(group, ec);
        persistLayerOrderLocal(state, group);
        if (syncServer) {
            sendCanvasExclusiveChoice(group, ec);
        }
    }

    public static boolean removeCanvasExclusiveChoice(TabletUiState state, String group, String ecId) {
        if (group == null || group.isBlank() || ecId == null || ecId.isBlank()) {
            return false;
        }
        List<CanvasExclusiveChoice> choices = new ArrayList<>(state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of()));
        boolean removed = choices.removeIf(ec -> ec.id().equals(ecId));
        if (!removed) {
            return false;
        }
        if (choices.isEmpty()) {
            state.canvas.canvasExclusiveChoicesByGroup.remove(group);
        } else {
            state.canvas.canvasExclusiveChoicesByGroup.put(group, choices);
        }
        if (ecId.equals(state.canvas.canvasSelection.primaryEcId())) {
            state.canvas.canvasSelection.setPrimaryEcId("");
        }
        state.canvas.canvasSelection.ecIds().remove(ecId);
        CanvasLayerOrdering.remove(state, group, CanvasLayerOrdering.exclusiveChoiceKey(ecId));
        ClientQuestCache.removeCanvasExclusiveChoiceLocal(group, ecId);
        persistLayerOrderLocal(state, group);
        sendCanvasExclusiveChoiceRemove(group, ecId);
        return true;
    }

    public static CanvasExclusiveChoice findCanvasExclusiveChoice(TabletUiState state, String group, String ecId) {
        if (group == null || group.isBlank() || ecId == null || ecId.isBlank()) {
            return null;
        }
        return state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of()).stream()
                .filter(ec -> ec.id().equals(ecId))
                .findFirst()
                .orElse(null);
    }

    public static boolean removeCanvasText(TabletUiState state, String group, String textId) {
        if (group == null || group.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvas.canvasTextsByGroup.getOrDefault(group, List.of()));
        boolean removed = texts.removeIf(text -> text.id().equals(textId));
        if (!removed) {
            return false;
        }
        if (texts.isEmpty()) {
            state.canvas.canvasTextsByGroup.remove(group);
        } else {
            state.canvas.canvasTextsByGroup.put(group, texts);
        }
        if (textId.equals(state.canvas.canvasSelection.primaryTextId())) {
            state.canvas.canvasSelection.setPrimaryTextId("");
        }
        state.canvas.canvasSelection.textIds().remove(textId);
        if (textId.equals(state.canvas.canvasTextMenuTarget)) {
            TextStyleSession.closeMainCanvas(state);
        }
        CanvasLayerOrdering.remove(state, group, CanvasLayerOrdering.textKey(textId));
        ClientQuestCache.removeCanvasTextLocal(group, textId);
        persistLayerOrderLocal(state, group);
        sendCanvasTextRemove(group, textId);
        return true;
    }

    public static CanvasTextLayer findCanvasText(TabletUiState state, String group, String textId) {
        if (group == null || group.isBlank() || textId == null || textId.isBlank()) {
            return null;
        }
        return state.canvas.canvasTextsByGroup.getOrDefault(group, List.of()).stream()
                .filter(text -> text.id().equals(textId))
                .findFirst()
                .orElse(null);
    }

    public static void updateCanvasText(TabletUiState state, String group, String textId, UnaryOperator<CanvasTextLayer> updater) {
        CanvasTextLayer current = findCanvasText(state, group, textId);
        if (current == null || updater == null) {
            return;
        }
        CanvasTextLayer next = updater.apply(current);
        if (next != null) {
            putCanvasText(state, group, next);
        }
    }

    public static void persistCanvasImage(TabletUiState state, String group, String imageId) {
        CanvasImageLayer image = findCanvasImage(state, group, imageId);
        if (image == null) {
            return;
        }
        ClientQuestCache.putCanvasImageLocal(group, image);
        sendCanvasImage(group, image);
    }

    public static void persistCanvasText(TabletUiState state, String group, String textId) {
        CanvasTextLayer text = findCanvasText(state, group, textId);
        if (text == null) {
            return;
        }
        ClientQuestCache.putCanvasTextLocal(group, text);
        sendCanvasText(group, text);
    }

    public static void persistCanvasExclusiveChoice(TabletUiState state, String group, String ecId) {
        CanvasExclusiveChoice ec = findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            return;
        }
        ClientQuestCache.putCanvasExclusiveChoiceLocal(group, ec);
        sendCanvasExclusiveChoice(group, ec);
    }

    public static void persistLayerOrder(TabletUiState state, String group) {
        persistLayerOrderLocal(state, group);
        sendLayerOrder(state, group);
    }

    public static CanvasImageLayer findCanvasImage(TabletUiState state, String group, String imageId) {
        if (group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return null;
        }
        return state.canvas.canvasImagesByGroup.getOrDefault(group, List.of()).stream()
                .filter(image -> image.id().equals(imageId))
                .findFirst()
                .orElse(null);
    }

    private static void persistLayerOrderLocal(TabletUiState state, String group) {
        ClientQuestCache.setCanvasLayerOrderLocal(group, state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
    }

    private static void sendCanvasExclusiveChoice(String group, CanvasExclusiveChoice ec) {
        CompoundTag payload = EditorCommandPayloads.canvasExclusiveChoicePut(group, ec);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT, payload));
    }

    private static void sendCanvasExclusiveChoiceRemove(String group, String ecId) {
        CompoundTag payload = EditorCommandPayloads.canvasExclusiveChoiceRemove(group, ecId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_REMOVE, payload));
    }

    private static void sendCanvasImage(String group, CanvasImageLayer image) {
        CompoundTag payload = EditorCommandPayloads.canvasImagePut(group, image);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_IMAGE_PUT, payload));
    }

    private static void sendCanvasImageRemove(String group, String imageId) {
        CompoundTag payload = EditorCommandPayloads.canvasImageRemove(group, imageId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_IMAGE_REMOVE, payload));
    }

    private static void sendCanvasText(String group, CanvasTextLayer text) {
        CompoundTag payload = EditorCommandPayloads.canvasTextPut(group, text);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_TEXT_PUT, payload));
    }

    private static void sendCanvasTextRemove(String group, String textId) {
        CompoundTag payload = EditorCommandPayloads.canvasTextRemove(group, textId);
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_TEXT_REMOVE, payload));
    }

    private static void sendLayerOrder(TabletUiState state, String group) {
        CompoundTag payload = EditorCommandPayloads.canvasLayerOrder(group, state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of()));
        ModNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.CANVAS_LAYER_ORDER, payload));
    }
}
