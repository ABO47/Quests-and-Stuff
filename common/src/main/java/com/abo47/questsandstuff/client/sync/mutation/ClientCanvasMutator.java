package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.state.ClientCanvasLayerState;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;

public final class ClientCanvasMutator {
    private ClientCanvasMutator() {
    }

    public static void putCanvasExclusiveChoiceLocal(String group, CanvasExclusiveChoice ec) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || ec == null || ec.id().isBlank()) {
            return;
        }
        ClientCanvasLayerState.putExclusiveChoice(normalized, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String group, String ecId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || ecId == null || ecId.isBlank()) {
            return;
        }
        ClientCanvasLayerState.removeExclusiveChoice(normalized, ecId);
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        ClientCanvasLayerState.putImage(normalized, image);
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || imageId == null || imageId.isBlank()) {
            return;
        }
        ClientCanvasLayerState.removeImage(normalized, imageId);
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        ClientCanvasLayerState.putText(normalized, text);
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank() || textId == null || textId.isBlank()) {
            return;
        }
        ClientCanvasLayerState.removeText(normalized, textId);
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        ClientCanvasLayerState.setLayerOrder(normalized, order);
    }
}
