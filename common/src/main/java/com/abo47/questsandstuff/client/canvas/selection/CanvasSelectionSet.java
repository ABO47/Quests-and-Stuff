package com.abo47.questsandstuff.client.canvas.selection;

import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record CanvasSelectionSet(Set<String> questIds, Set<String> imageIds, Set<String> textIds) {
    public CanvasSelectionSet {
        questIds = immutableCopy(questIds);
        imageIds = immutableCopy(imageIds);
        textIds = immutableCopy(textIds);
    }

    public static CanvasSelectionSet current(TabletUiState state) {
        Set<String> imageIds = new LinkedHashSet<>(state.selectedCanvasImageIds);
        if (!state.selectedCanvasImageId.isBlank()) {
            imageIds.add(state.selectedCanvasImageId);
        }
        Set<String> textIds = new LinkedHashSet<>(state.selectedCanvasTextIds);
        if (!state.selectedCanvasTextId.isBlank()) {
            textIds.add(state.selectedCanvasTextId);
        }
        return new CanvasSelectionSet(state.selectedQuestIds, imageIds, textIds);
    }

    public int size() {
        return questIds.size() + imageIds.size() + textIds.size();
    }

    public boolean hasMultiple() {
        return size() > 1;
    }

    public List<String> layerKeys() {
        List<String> keys = new ArrayList<>();
        for (String questId : questIds) {
            keys.add(CanvasLayerOrdering.questKey(questId));
        }
        for (String imageId : imageIds) {
            keys.add(CanvasLayerOrdering.imageKey(imageId));
        }
        for (String textId : textIds) {
            keys.add(CanvasLayerOrdering.textKey(textId));
        }
        return keys;
    }

    private static Set<String> immutableCopy(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }
}
