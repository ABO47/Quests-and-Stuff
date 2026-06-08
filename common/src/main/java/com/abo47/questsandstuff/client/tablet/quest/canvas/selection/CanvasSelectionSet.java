package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKey;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKind;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CanvasSelectionSet {
    private final CanvasLayerSelection layers;

    public CanvasSelectionSet(Set<String> questIds, Set<String> imageIds, Set<String> textIds) {
        this(CanvasLayerSelection.fromIds(questIds, imageIds, textIds));
    }

    public CanvasSelectionSet(CanvasLayerSelection layers) {
        this.layers = layers == null ? new CanvasLayerSelection(Set.of()) : layers;
    }

    public static CanvasSelectionSet current(TabletUiState state) {
        Set<String> imageIds = new LinkedHashSet<>(state.canvas.canvasSelection.imageIds());
        if (!state.canvas.canvasSelection.primaryImageId().isBlank()) {
            imageIds.add(state.canvas.canvasSelection.primaryImageId());
        }
        Set<String> textIds = new LinkedHashSet<>(state.canvas.canvasSelection.textIds());
        if (!state.canvas.canvasSelection.primaryTextId().isBlank()) {
            textIds.add(state.canvas.canvasSelection.primaryTextId());
        }
        return new CanvasSelectionSet(state.canvas.canvasSelection.questIds(), imageIds, textIds);
    }

    public Set<String> questIds() {
        return layers.ids(CanvasLayerKind.QUEST);
    }

    public Set<String> imageIds() {
        return layers.ids(CanvasLayerKind.IMAGE);
    }

    public Set<String> textIds() {
        return layers.ids(CanvasLayerKind.TEXT);
    }

    public Set<CanvasLayerKey> typedLayerKeys() {
        return layers.keys();
    }

    public int size() {
        return layers.size();
    }

    public boolean hasMultiple() {
        return size() > 1;
    }

    public List<String> layerKeys() {
        return layers.orderKeys();
    }

    public List<String> layerKeysInOrder(List<String> orderedLayerKeys) {
        return layers.selectedOrderKeys(orderedLayerKeys);
    }
}
