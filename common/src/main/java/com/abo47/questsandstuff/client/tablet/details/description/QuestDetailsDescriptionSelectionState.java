package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class QuestDetailsDescriptionSelectionState {
    private QuestDetailsDescriptionSelectionState() {
    }

    static Set<String> selectedTextIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetailsSelectedTextIds);
        if (!state.questDetailsSelectedTextId.isBlank()) {
            ids.add(state.questDetailsSelectedTextId);
        }
        return ids;
    }

    static Set<String> selectedImageIds(TabletUiState state) {
        Set<String> ids = new LinkedHashSet<>(state.questDetailsSelectedImageIds);
        if (!state.questDetailsSelectedImageId.isBlank()) {
            ids.add(state.questDetailsSelectedImageId);
        }
        return ids;
    }

    static boolean hasSelection(TabletUiState state) {
        return !selectedTextIds(state).isEmpty() || !selectedImageIds(state).isEmpty();
    }

    static void selectOnlyText(TabletUiState state, String id) {
        clear(state);
        if (id != null && !id.isBlank()) {
            state.questDetailsSelectedTextId = id;
            state.questDetailsSelectedTextIds.add(id);
        }
    }

    static void selectOnlyImage(TabletUiState state, String id) {
        clear(state);
        if (id != null && !id.isBlank()) {
            state.questDetailsSelectedImageId = id;
            state.questDetailsSelectedImageIds.add(id);
        }
    }

    static void clear(TabletUiState state) {
        state.questDetailsSelectedTextId = "";
        state.questDetailsSelectedImageId = "";
        state.questDetailsSelectedTextIds.clear();
        state.questDetailsSelectedImageIds.clear();
        state.selectedCanvasTextId = "";
        state.selectedCanvasImageId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasImageIds.clear();
        state.selectionBoundsVisible = false;
    }

    static List<String> selectedLayerKeys(TabletUiState state, QuestDetailsDescriptionModel model) {
        List<String> selected = new ArrayList<>();
        Set<String> textIds = selectedTextIds(state);
        Set<String> imageIds = selectedImageIds(state);
        for (String key : model.order) {
            if (key.startsWith(QuestDetailsDescriptionModel.ORDER_TEXT)) {
                String id = key.substring(QuestDetailsDescriptionModel.ORDER_TEXT.length());
                if (textIds.contains(id) && model.text(id) != null) {
                    selected.add(key);
                }
            } else if (key.startsWith(QuestDetailsDescriptionModel.ORDER_IMAGE)) {
                String id = key.substring(QuestDetailsDescriptionModel.ORDER_IMAGE.length());
                if (imageIds.contains(id) && model.image(id) != null) {
                    selected.add(key);
                }
            }
        }
        return selected;
    }
}
