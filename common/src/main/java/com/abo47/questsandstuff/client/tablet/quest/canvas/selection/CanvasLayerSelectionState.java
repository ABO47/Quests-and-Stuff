package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKey;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKind;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CanvasLayerSelectionState {
    private final LinkedHashSet<String> questIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> imageIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> textIds = new LinkedHashSet<>();
    private final LinkedHashSet<String> ecIds = new LinkedHashSet<>();
    private String primaryImageId = "";
    private String primaryTextId = "";
    private String primaryEcId = "";

    public Set<String> questIds() {
        return questIds;
    }

    public Set<String> imageIds() {
        return imageIds;
    }

    public Set<String> textIds() {
        return textIds;
    }

    public Set<String> ecIds() {
        return ecIds;
    }

    public Set<String> ids(CanvasLayerKind kind) {
        return switch (kind) {
            case QUEST -> questIds;
            case IMAGE -> imageIds;
            case TEXT -> textIds;
            case EXCLUSIVE_CHOICE -> ecIds;
            case CONNECTION -> Set.of();
        };
    }

    public String primaryImageId() {
        return imageIds.contains(primaryImageId) ? primaryImageId : "";
    }

    public String primaryTextId() {
        return textIds.contains(primaryTextId) ? primaryTextId : "";
    }

    public String primaryEcId() {
        return ecIds.contains(primaryEcId) ? primaryEcId : "";
    }

    public boolean hasQuest(String id) {
        return containsClean(questIds, id);
    }

    public boolean hasImage(String id) {
        String clean = clean(id);
        return !clean.isBlank() && (clean.equals(primaryImageId) || imageIds.contains(clean));
    }

    public boolean hasText(String id) {
        String clean = clean(id);
        return !clean.isBlank() && (clean.equals(primaryTextId) || textIds.contains(clean));
    }

    public boolean hasEc(String id) {
        String clean = clean(id);
        return !clean.isBlank() && (clean.equals(primaryEcId) || ecIds.contains(clean));
    }

    public boolean hasAny() {
        return !questIds.isEmpty() || !imageIds.isEmpty() || !textIds.isEmpty() || !ecIds.isEmpty();
    }

    public int size() {
        return questIds.size() + imageIds.size() + textIds.size() + ecIds.size();
    }

    public void clear() {
        clearQuests();
        clearImages();
        clearTexts();
        clearExclusiveChoices();
    }

    public void clearQuests() {
        questIds.clear();
    }

    public void clearImages() {
        imageIds.clear();
        primaryImageId = "";
    }

    public void clearTexts() {
        textIds.clear();
        primaryTextId = "";
    }

    public void clearExclusiveChoices() {
        ecIds.clear();
        primaryEcId = "";
    }

    public void clearPrimaryImage() {
        primaryImageId = firstOrBlank(imageIds);
    }

    public void clearPrimaryText() {
        primaryTextId = firstOrBlank(textIds);
    }

    public void clearPrimaryEc() {
        primaryEcId = firstOrBlank(ecIds);
    }

    public void addQuest(String id) {
        addClean(questIds, id);
    }

    public void addImage(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        imageIds.add(clean);
        primaryImageId = clean;
    }

    public void addText(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        textIds.add(clean);
        primaryTextId = clean;
    }

    public void addEc(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        ecIds.add(clean);
        primaryEcId = clean;
    }

    public void addQuests(Collection<String> ids) {
        addAllClean(questIds, ids);
    }

    public void addImages(Collection<String> ids) {
        addAllClean(imageIds, ids);
        primaryImageId = firstOrBlankReversed(imageIds);
    }

    public void addTexts(Collection<String> ids) {
        addAllClean(textIds, ids);
        primaryTextId = firstOrBlankReversed(textIds);
    }

    public void addEcs(Collection<String> ids) {
        addAllClean(ecIds, ids);
        primaryEcId = firstOrBlankReversed(ecIds);
    }

    public void selectOnlyQuest(String id) {
        clear();
        addQuest(id);
    }

    public void selectOnlyImage(String id) {
        clear();
        addImage(id);
    }

    public void selectOnlyText(String id) {
        clear();
        addText(id);
    }

    public void selectOnlyEc(String id) {
        clear();
        addEc(id);
    }

    public void setPrimaryImageId(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            primaryImageId = "";
            return;
        }
        imageIds.add(clean);
        primaryImageId = clean;
    }

    public void setPrimaryTextId(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            primaryTextId = "";
            return;
        }
        textIds.add(clean);
        primaryTextId = clean;
    }

    public void setPrimaryEcId(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            primaryEcId = "";
            return;
        }
        ecIds.add(clean);
        primaryEcId = clean;
    }

    public void removeQuest(String id) {
        questIds.remove(clean(id));
    }

    public void removeImage(String id) {
        String clean = clean(id);
        imageIds.remove(clean);
        if (clean.equals(primaryImageId)) {
            primaryImageId = firstOrBlank(imageIds);
        }
    }

    public void removeText(String id) {
        String clean = clean(id);
        textIds.remove(clean);
        if (clean.equals(primaryTextId)) {
            primaryTextId = firstOrBlank(textIds);
        }
    }

    public void removeEc(String id) {
        String clean = clean(id);
        ecIds.remove(clean);
        if (clean.equals(primaryEcId)) {
            primaryEcId = firstOrBlank(ecIds);
        }
    }

    public void toggleImage(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        if (imageIds.remove(clean)) {
            if (clean.equals(primaryImageId)) {
                primaryImageId = firstOrBlank(imageIds);
            }
            return;
        }
        addImage(clean);
    }

    public void toggleText(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        if (textIds.remove(clean)) {
            if (clean.equals(primaryTextId)) {
                primaryTextId = firstOrBlank(textIds);
            }
            return;
        }
        addText(clean);
    }

    public void toggleEc(String id) {
        String clean = clean(id);
        if (clean.isBlank()) {
            return;
        }
        if (ecIds.remove(clean)) {
            if (clean.equals(primaryEcId)) {
                primaryEcId = firstOrBlank(ecIds);
            }
            return;
        }
        addEc(clean);
    }

    public CanvasLayerSelection snapshot() {
        return CanvasLayerSelection.fromIds(questIds, imageIds, textIds, ecIds);
    }

    public CanvasSelectionSet selectionSet() {
        return new CanvasSelectionSet(snapshot());
    }

    public Set<CanvasLayerKey> typedKeys() {
        return snapshot().keys();
    }

    private static boolean containsClean(Set<String> ids, String id) {
        String clean = clean(id);
        return !clean.isBlank() && ids.contains(clean);
    }

    private static void addClean(Set<String> ids, String id) {
        String clean = clean(id);
        if (!clean.isBlank()) {
            ids.add(clean);
        }
    }

    private static void addAllClean(Set<String> target, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            addClean(target, id);
        }
    }

    private static String firstOrBlank(Set<String> ids) {
        return ids.stream().findFirst().orElse("");
    }

    private static String firstOrBlankReversed(LinkedHashSet<String> ids) {
        String value = "";
        for (String id : ids) {
            value = id;
        }
        return value;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
