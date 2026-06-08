package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasElementStore;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

import java.util.List;
import java.util.function.UnaryOperator;

public final class CanvasLayerMutations {
    private CanvasLayerMutations() {
    }

    public static void moveQuestLayer(TabletUiState state, String group, String questId, boolean front) {
        CanvasLayerOrdering.moveQuestLayer(state, group, questId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveImageLayer(TabletUiState state, String group, String imageId, boolean front) {
        CanvasLayerOrdering.moveImageLayer(state, group, imageId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveTextLayer(TabletUiState state, String group, String textId, boolean front) {
        CanvasLayerOrdering.moveTextLayer(state, group, textId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveConnectionLayer(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean front) {
        CanvasLayerOrdering.moveConnectionLayer(state, group, QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId), front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveCanvasLayers(TabletUiState state, String group, List<String> layerKeys, boolean front) {
        CanvasLayerOrdering.moveLayers(state, group, layerKeys, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image) {
        CanvasElementStore.putCanvasImage(state, group, image);
    }

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image, boolean syncServer) {
        CanvasElementStore.putCanvasImage(state, group, image, syncServer);
    }

    public static boolean removeCanvasImage(TabletUiState state, String group, String imageId) {
        return CanvasElementStore.removeCanvasImage(state, group, imageId);
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text) {
        CanvasElementStore.putCanvasText(state, group, text);
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text, boolean syncServer) {
        CanvasElementStore.putCanvasText(state, group, text, syncServer);
    }

    public static boolean removeCanvasText(TabletUiState state, String group, String textId) {
        return CanvasElementStore.removeCanvasText(state, group, textId);
    }

    public static CanvasTextLayer findCanvasText(TabletUiState state, String group, String textId) {
        return CanvasElementStore.findCanvasText(state, group, textId);
    }

    public static CanvasImageLayer findCanvasImage(TabletUiState state, String group, String imageId) {
        return CanvasElementStore.findCanvasImage(state, group, imageId);
    }

    public static void updateCanvasText(TabletUiState state, String group, String textId, UnaryOperator<CanvasTextLayer> updater) {
        CanvasElementStore.updateCanvasText(state, group, textId, updater);
    }

    public static void persistCanvasImage(TabletUiState state, String group, String imageId) {
        CanvasElementStore.persistCanvasImage(state, group, imageId);
    }

    public static void persistCanvasText(TabletUiState state, String group, String textId) {
        CanvasElementStore.persistCanvasText(state, group, textId);
    }

    public static CanvasImageLayer effectiveCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.transientCanvasImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.transientCanvasTexts.getOrDefault(text.id(), text);
    }

    public static CanvasImageLayer effectiveQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.questDetailsTransientImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.questDetailsTransientTexts.getOrDefault(text.id(), text);
    }

    public static void putTransientCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.transientCanvasImages.put(image.id(), image);
    }

    public static void putTransientCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.transientCanvasTexts.put(text.id(), text);
    }

    public static void putTransientQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.questDetailsTransientImages.put(image.id(), image);
    }

    public static void putTransientQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.questDetailsTransientTexts.put(text.id(), text);
    }

    public static boolean commitTransientCanvasImage(TabletUiState state, String group, String imageId) {
        if (state == null || group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        CanvasImageLayer preview = state.transientCanvasImages.remove(imageId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasImage(state, group, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasText(TabletUiState state, String group, String textId) {
        if (state == null || group == null || group.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        CanvasTextLayer preview = state.transientCanvasTexts.remove(textId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasText(state, group, preview, false);
        return true;
    }

    public static void commitSelectedTransientCanvasLayers(TabletUiState state, String group) {
        for (String imageId : CanvasSelectionActions.selectedCanvasImageIds(state)) {
            commitTransientCanvasImage(state, group, imageId);
        }
        for (String textId : CanvasSelectionActions.selectedCanvasTextIds(state)) {
            commitTransientCanvasText(state, group, textId);
        }
    }
}
