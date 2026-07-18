package com.abo47.questsandstuff.client.tablet.quest.canvas;

import java.util.List;
import java.util.function.UnaryOperator;

import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasElementStore;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

public final class CanvasLayerMutations {
    private CanvasLayerMutations() {
    }

    public static void moveQuestLayer(TabletUiState state, String chapter, String questId, boolean front) {
        CanvasLayerOrdering.moveQuestLayer(state, chapter, questId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveImageLayer(TabletUiState state, String chapter, String imageId, boolean front) {
        CanvasLayerOrdering.moveImageLayer(state, chapter, imageId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveTextLayer(TabletUiState state, String chapter, String textId, boolean front) {
        CanvasLayerOrdering.moveTextLayer(state, chapter, textId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveConnectionLayer(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, boolean front) {
        CanvasLayerOrdering.moveConnectionLayer(state, chapter, QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId), front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveCanvasLayers(TabletUiState state, String chapter, List<String> layerKeys, boolean front) {
        CanvasLayerOrdering.moveLayers(state, chapter, layerKeys, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void moveExclusiveChoiceLayer(TabletUiState state, String chapter, String ecId, boolean front) {
        CanvasLayerOrdering.moveExclusiveChoiceLayer(state, chapter, ecId, front);
        CanvasElementStore.persistLayerOrder(state, chapter);
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String chapter, CanvasExclusiveChoice ec) {
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, ec);
    }

    public static void putCanvasExclusiveChoice(TabletUiState state, String chapter, CanvasExclusiveChoice ec, boolean syncServer) {
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, ec, syncServer);
    }

    public static void putCanvasExclusiveChoices(TabletUiState state, String chapter, List<CanvasExclusiveChoice> ecs, boolean syncServer) {
        CanvasElementStore.putCanvasExclusiveChoices(state, chapter, ecs, syncServer);
    }

    public static boolean removeCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        return CanvasElementStore.removeCanvasExclusiveChoice(state, chapter, ecId);
    }

    public static CanvasExclusiveChoice findCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        return CanvasElementStore.findCanvasExclusiveChoice(state, chapter, ecId);
    }

    public static void putCanvasImage(TabletUiState state, String chapter, CanvasImageLayer image) {
        CanvasElementStore.putCanvasImage(state, chapter, image);
    }

    public static void putCanvasImage(TabletUiState state, String chapter, CanvasImageLayer image, boolean syncServer) {
        CanvasElementStore.putCanvasImage(state, chapter, image, syncServer);
    }

    public static boolean removeCanvasImage(TabletUiState state, String chapter, String imageId) {
        return CanvasElementStore.removeCanvasImage(state, chapter, imageId);
    }

    public static void putCanvasText(TabletUiState state, String chapter, CanvasTextLayer text) {
        CanvasElementStore.putCanvasText(state, chapter, text);
    }

    public static void putCanvasText(TabletUiState state, String chapter, CanvasTextLayer text, boolean syncServer) {
        CanvasElementStore.putCanvasText(state, chapter, text, syncServer);
    }

    public static boolean removeCanvasText(TabletUiState state, String chapter, String textId) {
        return CanvasElementStore.removeCanvasText(state, chapter, textId);
    }

    public static CanvasTextLayer findCanvasText(TabletUiState state, String chapter, String textId) {
        return CanvasElementStore.findCanvasText(state, chapter, textId);
    }

    public static CanvasImageLayer findCanvasImage(TabletUiState state, String chapter, String imageId) {
        return CanvasElementStore.findCanvasImage(state, chapter, imageId);
    }

    public static void updateCanvasText(TabletUiState state, String chapter, String textId, UnaryOperator<CanvasTextLayer> updater) {
        CanvasElementStore.updateCanvasText(state, chapter, textId, updater);
    }

    public static void persistCanvasImage(TabletUiState state, String chapter, String imageId) {
        CanvasElementStore.persistCanvasImage(state, chapter, imageId);
    }

    public static void persistCanvasText(TabletUiState state, String chapter, String textId) {
        CanvasElementStore.persistCanvasText(state, chapter, textId);
    }

    public static void persistCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        CanvasElementStore.persistCanvasExclusiveChoice(state, chapter, ecId);
    }

    public static CanvasImageLayer effectiveCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.canvas.transientCanvasImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.canvas.transientCanvasTexts.getOrDefault(text.id(), text);
    }

    public static CanvasExclusiveChoice effectiveCanvasExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        if (state == null || ec == null) {
            return ec;
        }
        return state.canvas.transientCanvasExclusiveChoices.getOrDefault(ec.id(), ec);
    }

    public static CanvasImageLayer effectiveQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.questDetails.questDetailsTransientImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.questDetails.questDetailsTransientTexts.getOrDefault(text.id(), text);
    }

    public static void putTransientCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasImages.put(image.id(), image);
    }

    public static void putTransientCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasTexts.put(text.id(), text);
    }

    public static void putTransientCanvasExclusiveChoice(TabletUiState state, CanvasExclusiveChoice ec) {
        if (state == null || ec == null || ec.id().isBlank()) {
            return;
        }
        state.canvas.transientCanvasExclusiveChoices.put(ec.id(), ec);
    }

    public static void putTransientQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.questDetails.questDetailsTransientImages.put(image.id(), image);
    }

    public static void putTransientQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.questDetails.questDetailsTransientTexts.put(text.id(), text);
    }

    public static boolean commitTransientCanvasImage(TabletUiState state, String chapter, String imageId) {
        if (state == null || chapter == null || chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        CanvasImageLayer preview = state.canvas.transientCanvasImages.remove(imageId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasImage(state, chapter, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasText(TabletUiState state, String chapter, String textId) {
        if (state == null || chapter == null || chapter.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        CanvasTextLayer preview = state.canvas.transientCanvasTexts.remove(textId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasText(state, chapter, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasExclusiveChoice(TabletUiState state, String chapter, String ecId) {
        if (state == null || chapter == null || chapter.isBlank() || ecId == null || ecId.isBlank()) {
            return false;
        }
        CanvasExclusiveChoice preview = state.canvas.transientCanvasExclusiveChoices.remove(ecId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasExclusiveChoice(state, chapter, preview, false);
        return true;
    }

    public static void commitSelectedTransientCanvasLayers(TabletUiState state, String chapter) {
        for (String imageId : CanvasSelectionActions.selectedImageIds(state)) {
            commitTransientCanvasImage(state, chapter, imageId);
        }
        for (String textId : CanvasSelectionActions.selectedTextIds(state)) {
            commitTransientCanvasText(state, chapter, textId);
        }
        for (String ecId : CanvasSelectionActions.selectedEcIds(state)) {
            commitTransientCanvasExclusiveChoice(state, chapter, ecId);
        }
    }
}
