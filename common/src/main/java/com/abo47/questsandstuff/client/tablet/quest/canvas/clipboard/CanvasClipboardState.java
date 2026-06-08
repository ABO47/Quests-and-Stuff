package com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CanvasClipboardState {
    private boolean questClipboardAvailable;
    private int originX;
    private int originY;
    private final List<CanvasImageLayer> imageLayers = new ArrayList<>();
    private final List<CanvasTextLayer> textLayers = new ArrayList<>();
    private final Set<String> pendingPastedImageIds = new LinkedHashSet<>();
    private final Set<String> pendingPastedTextIds = new LinkedHashSet<>();

    public boolean hasQuestClipboard() {
        return questClipboardAvailable;
    }

    public boolean hasCanvasLayers() {
        return !imageLayers.isEmpty() || !textLayers.isEmpty();
    }

    public boolean hasContent() {
        return questClipboardAvailable || hasCanvasLayers();
    }

    public int originX() {
        return originX;
    }

    public int originY() {
        return originY;
    }

    public List<CanvasImageLayer> imageLayers() {
        return List.copyOf(imageLayers);
    }

    public List<CanvasTextLayer> textLayers() {
        return List.copyOf(textLayers);
    }

    public int imageCount() {
        return imageLayers.size();
    }

    public int textCount() {
        return textLayers.size();
    }

    public void store(boolean questClipboardAvailable, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, int originX, int originY) {
        this.questClipboardAvailable = questClipboardAvailable;
        this.originX = originX;
        this.originY = originY;
        imageLayers.clear();
        if (images != null) {
            imageLayers.addAll(images);
        }
        textLayers.clear();
        if (texts != null) {
            textLayers.addAll(texts);
        }
        clearPendingPastedLayers();
    }

    public void clearContent() {
        store(false, List.of(), List.of(), 0, 0);
    }

    public void clearPendingPastedLayers() {
        pendingPastedImageIds.clear();
        pendingPastedTextIds.clear();
    }

    public void recordPastedImage(String id) {
        String clean = clean(id);
        if (!clean.isBlank()) {
            pendingPastedImageIds.add(clean);
        }
    }

    public void recordPastedText(String id) {
        String clean = clean(id);
        if (!clean.isBlank()) {
            pendingPastedTextIds.add(clean);
        }
    }

    public Set<String> pendingPastedImageIds() {
        return Set.copyOf(pendingPastedImageIds);
    }

    public Set<String> pendingPastedTextIds() {
        return Set.copyOf(pendingPastedTextIds);
    }

    public String lastPendingPastedImageId() {
        return pendingPastedImageIds.stream().reduce((first, second) -> second).orElse("");
    }

    public String lastPendingPastedTextId() {
        return pendingPastedTextIds.stream().reduce((first, second) -> second).orElse("");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
