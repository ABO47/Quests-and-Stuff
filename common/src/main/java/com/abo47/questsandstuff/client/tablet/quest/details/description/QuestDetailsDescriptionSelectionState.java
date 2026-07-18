package com.abo47.questsandstuff.client.tablet.quest.details.description;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

final class QuestDetailsDescriptionSelectionState {
    private QuestDetailsDescriptionSelectionState() {
    }

    static Set<String> selectedTextIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetails.questDetailsDescriptionSelection.textIds());
        if (!state.questDetails.questDetailsDescriptionSelection.primaryTextId().isBlank()) {
            ids.add(state.questDetails.questDetailsDescriptionSelection.primaryTextId());
        }
        return ids;
    }

    static Set<String> selectedImageIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetails.questDetailsDescriptionSelection.imageIds());
        if (!state.questDetails.questDetailsDescriptionSelection.primaryImageId().isBlank()) {
            ids.add(state.questDetails.questDetailsDescriptionSelection.primaryImageId());
        }
        return ids;
    }

    static boolean hasSelection(TabletUiState state) {
        return !selectedTextIds(state).isEmpty() || !selectedImageIds(state).isEmpty();
    }

    static CanvasSelectionSet selectionSet(TabletUiState state) {
        return new CanvasSelectionSet(Set.of(), selectedImageIds(state), selectedTextIds(state));
    }

    static void selectOnlyText(TabletUiState state, String id) {
        clear(state);
        if (id != null && !id.isBlank()) {
            state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(id);
            state.questDetails.questDetailsDescriptionSelection.textIds().add(id);
        }
    }

    static void selectOnlyImage(TabletUiState state, String id) {
        clear(state);
        if (id != null && !id.isBlank()) {
            state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(id);
            state.questDetails.questDetailsDescriptionSelection.imageIds().add(id);
        }
    }

    static void clear(TabletUiState state) {
        state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
        state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId("");
        state.questDetails.questDetailsDescriptionSelection.textIds().clear();
        state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.selectionBoundsVisible = false;
    }

    static List<String> selectedLayerKeys(TabletUiState state, QuestDetailsDescriptionModel model) {
        List<String> selected = selectionSet(state).layerKeysInOrder(model.order);
        return selected.stream()
                .filter(key -> {
                    if (key.startsWith(QuestDetailsDescriptionModel.ORDER_TEXT)) {
                        return model.text(key.substring(QuestDetailsDescriptionModel.ORDER_TEXT.length())) != null;
                    }
                    if (key.startsWith(QuestDetailsDescriptionModel.ORDER_IMAGE)) {
                        return model.image(key.substring(QuestDetailsDescriptionModel.ORDER_IMAGE.length())) != null;
                    }
                    return false;
                })
                .toList();
    }
}
