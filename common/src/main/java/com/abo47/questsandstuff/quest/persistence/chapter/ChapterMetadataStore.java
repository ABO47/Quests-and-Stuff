package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class ChapterMetadataStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path chaptersDir;
    private final ChapterMetadataState state = new ChapterMetadataState();

    public ChapterMetadataStore(Path chaptersDir) {
        this.chaptersDir = chaptersDir;
    }

    public List<String> groupOrder() {
        return List.copyOf(state.groupOrder);
    }

    public void setGroupOrder(List<String> groups, Set<String> discoveredGroups) {
        state.setGroupOrder(groups, discoveredGroups);
        save();
    }

    public String groupIcon(String group) {
        return state.groupIcon(group);
    }

    public String groupBackground(String group) {
        return state.groupBackground(group);
    }

    public String groupCanvasBackground(String group) {
        return state.groupCanvasBackground(group);
    }

    public String groupTextAlign(String group) {
        return state.groupTextAlign(group);
    }

    public int groupTextColor(String group) {
        return state.groupTextColor(group);
    }

    public String groupTextStyle(String group) {
        return state.groupTextStyle(group);
    }

    public int groupTextSize(String group) {
        return state.groupTextSize(group);
    }

    public boolean groupLockUntilUnlocked(String group) {
        return state.groupLockUntilUnlocked(group);
    }

    public boolean groupHideUntilUnlocked(String group) {
        return state.groupHideUntilUnlocked(group);
    }

    public java.util.Map<String, List<CanvasImageLayer>> canvasImagesByGroup() {
        return ChapterMetadataState.copyLayerMap(state.canvasImagesByGroup);
    }

    public java.util.Map<String, List<CanvasTextLayer>> canvasTextsByGroup() {
        return ChapterMetadataState.copyLayerMap(state.canvasTextsByGroup);
    }

    public java.util.Map<String, List<String>> canvasLayerOrderByGroup() {
        return ChapterMetadataState.copyLayerMap(state.canvasLayerOrderByGroup);
    }

    public List<CanvasImageLayer> canvasImages(String group) {
        return List.copyOf(state.canvasImagesByGroup.getOrDefault(ChapterMetadataState.normalizeGroupName(group), List.of()));
    }

    public List<CanvasTextLayer> canvasTexts(String group) {
        return List.copyOf(state.canvasTextsByGroup.getOrDefault(ChapterMetadataState.normalizeGroupName(group), List.of()));
    }

    public List<String> canvasLayerOrder(String group) {
        return List.copyOf(state.canvasLayerOrderByGroup.getOrDefault(ChapterMetadataState.normalizeGroupName(group), List.of()));
    }

    public void setGroupIcon(String group, String icon) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        String value = icon == null ? "" : icon.trim();
        state.groupIcons.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter icon {} -> {}", normalized, value);
        save();
    }

    public void setGroupBackground(String group, String background) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        String value = background == null || background.isBlank() ? "default" : background.trim();
        state.groupBackgrounds.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter background {} -> {}", normalized, value);
        save();
    }

    public void setGroupCanvasBackground(String group, String background) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        String value = background == null || background.isBlank() ? "default" : background.trim();
        state.groupCanvasBackgrounds.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter canvas_background {} -> {}", normalized, value);
        save();
    }

    public void setGroupTextAlign(String group, String align) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        String value = ChapterMetadataJsonCodec.normalizeTextAlign(align);
        state.groupTextAlign.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_align {} -> {}", normalized, value);
        save();
    }

    public void setGroupTextColor(String group, int color) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        state.groupTextColor.put(normalized, color);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_color {} -> {}", normalized, color);
        save();
    }

    public void setGroupTextStyle(String group, String style) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        String value = ChapterMetadataJsonCodec.normalizeTextStyle(style);
        state.groupTextStyle.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_style {} -> {}", normalized, value);
        save();
    }

    public void setGroupTextSize(String group, int size) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        int value = CanvasTextLayer.clampFontSize(size);
        state.groupTextSize.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_size {} -> {}", normalized, value);
        save();
    }

    public void setGroupLockUntilUnlocked(String group, boolean lockUntilUnlocked) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        state.groupLockUntilUnlocked.put(normalized, lockUntilUnlocked);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter lock_until_unlocked {} -> {}", normalized, lockUntilUnlocked);
        save();
    }

    public void setGroupHideUntilUnlocked(String group, boolean hideUntilUnlocked) {
        String normalized = ChapterMetadataState.normalizeGroupName(group);
        if (normalized.isBlank()) {
            return;
        }
        state.groupHideUntilUnlocked.put(normalized, hideUntilUnlocked);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter hide_until_unlocked {} -> {}", normalized, hideUntilUnlocked);
        save();
    }

    public void putCanvasImage(String group, CanvasImageLayer image) {
        if (image != null && ChapterCanvasLayerMutations.put(state, group, image, image.id(), "image:" + image.id(), state.canvasImagesByGroup, CanvasImageLayer::id)) {
            save();
        }
    }

    public boolean removeCanvasImage(String group, String imageId) {
        if (!ChapterCanvasLayerMutations.remove(state, group, imageId, "image:" + imageId, state.canvasImagesByGroup, CanvasImageLayer::id)) {
            return false;
        }
        save();
        return true;
    }

    public void putCanvasText(String group, CanvasTextLayer text) {
        if (text != null && ChapterCanvasLayerMutations.put(state, group, text, text.id(), "text:" + text.id(), state.canvasTextsByGroup, CanvasTextLayer::id)) {
            save();
        }
    }

    public boolean removeCanvasText(String group, String textId) {
        if (!ChapterCanvasLayerMutations.remove(state, group, textId, "text:" + textId, state.canvasTextsByGroup, CanvasTextLayer::id)) {
            return false;
        }
        save();
        return true;
    }

    public void setCanvasLayerOrder(String group, List<String> order) {
        String normalized = state.ensureGroup(group);
        if (normalized.isBlank()) {
            return;
        }
        ChapterCanvasLayerMutations.setOrder(state, normalized, order);
        save();
    }

    public void load(Set<String> discoveredGroups) {
        state.clear();
        try {
            boolean migrated = false;
            for (Path path : ChapterMetadataFiles.jsonFiles(chaptersDir)) {
                migrated |= ChapterMetadataReader.read(path, state);
            }
            reconcile(discoveredGroups);
            if (migrated) {
                save();
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to read chapter metadata from {}", chaptersDir, e);
        }
    }

    public void save() {
        ChapterMetadataWriter.save(chaptersDir, state, GSON);
    }

    public void reconcile(Set<String> discoveredGroups) {
        state.reconcile(discoveredGroups);
    }

}
