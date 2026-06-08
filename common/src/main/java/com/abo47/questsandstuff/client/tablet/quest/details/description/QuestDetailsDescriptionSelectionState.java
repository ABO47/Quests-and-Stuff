package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class QuestDetailsDescriptionSelectionState {
    private QuestDetailsDescriptionSelectionState() {
    }

    static Set<String> selectedTextIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetailsDescriptionSelection.textIds());
        if (!state.questDetailsDescriptionSelection.primaryTextId().isBlank()) {
            ids.add(state.questDetailsDescriptionSelection.primaryTextId());
        }
        return ids;
    }

    static Set<String> selectedImageIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetailsDescriptionSelection.imageIds());
        if (!state.questDetailsDescriptionSelection.primaryImageId().isBlank()) {
            ids.add(state.questDetailsDescriptionSelection.primaryImageId());
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
            state.questDetailsDescriptionSelection.setPrimaryTextId(id);
            state.questDetailsDescriptionSelection.textIds().add(id);
        }
    }

    static void selectOnlyImage(TabletUiState state, String id) {
        clear(state);
        if (id != null && !id.isBlank()) {
            state.questDetailsDescriptionSelection.setPrimaryImageId(id);
            state.questDetailsDescriptionSelection.imageIds().add(id);
        }
    }

    static void clear(TabletUiState state) {
        state.questDetailsDescriptionSelection.setPrimaryTextId("");
        state.questDetailsDescriptionSelection.setPrimaryImageId("");
        state.questDetailsDescriptionSelection.textIds().clear();
        state.questDetailsDescriptionSelection.imageIds().clear();
        state.canvasSelection.setPrimaryTextId("");
        state.canvasSelection.setPrimaryImageId("");
        state.canvasSelection.questIds().clear();
        state.canvasSelection.textIds().clear();
        state.canvasSelection.imageIds().clear();
        state.selectionBoundsVisible = false;
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
