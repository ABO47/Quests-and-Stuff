package com.abo47.questsandstuff.quest.persistence.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChapterMetadataStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path chaptersDir;
    private final ChapterMetadataState state = new ChapterMetadataState();

    public ChapterMetadataStore(Path chaptersDir) {
        this.chaptersDir = chaptersDir;
    }

    public List<String> chapterOrder() {
        return List.copyOf(state.chapterOrder);
    }

    public ChapterMetadataSnapshot snapshot() {
        return new ChapterMetadataSnapshot(
                List.copyOf(state.chapterOrder),
                Map.copyOf(state.chapterIcons),
                Map.copyOf(state.chapterBackgrounds),
                Map.copyOf(state.chapterCanvasBackgrounds),
                Map.copyOf(state.chapterTextAlign),
                Map.copyOf(state.chapterTextColor),
                Map.copyOf(state.chapterTextStyle),
                Map.copyOf(state.chapterTextSize),
                Map.copyOf(state.chapterLockUntilUnlocked),
                Map.copyOf(state.chapterHideUntilUnlocked),
                ChapterMetadataState.copyLayerMap(state.canvasExclusiveChoicesByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasImagesByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasTextsByChapter),
                ChapterMetadataState.copyLayerMap(state.canvasLayerOrderByChapter)
        );
    }

    public void restore(ChapterMetadataSnapshot snapshot) {
        state.clear();
        if (snapshot == null) {
            save();
            return;
        }
        state.chapterOrder.addAll(snapshot.chapterOrder());
        state.chapterIcons.putAll(snapshot.chapterIcons());
        state.chapterBackgrounds.putAll(snapshot.chapterBackgrounds());
        state.chapterCanvasBackgrounds.putAll(snapshot.chapterCanvasBackgrounds());
        state.chapterTextAlign.putAll(snapshot.chapterTextAlign());
        state.chapterTextColor.putAll(snapshot.chapterTextColor());
        state.chapterTextStyle.putAll(snapshot.chapterTextStyle());
        state.chapterTextSize.putAll(snapshot.chapterTextSize());
        state.chapterLockUntilUnlocked.putAll(snapshot.chapterLockUntilUnlocked());
        state.chapterHideUntilUnlocked.putAll(snapshot.chapterHideUntilUnlocked());
        state.canvasExclusiveChoicesByChapter.putAll(mutableLayerMap(snapshot.canvasExclusiveChoicesByChapter()));
        state.canvasImagesByChapter.putAll(mutableLayerMap(snapshot.canvasImagesByChapter()));
        state.canvasTextsByChapter.putAll(mutableLayerMap(snapshot.canvasTextsByChapter()));
        state.canvasLayerOrderByChapter.putAll(mutableLayerMap(snapshot.canvasLayerOrderByChapter()));
        save();
    }

    public void setChapterOrder(List<String> groups, Set<String> discoveredGroups) {
        state.setChapterOrder(groups, discoveredGroups);
        save();
    }

    public void renameChapter(String fromName, String toName, Set<String> discoveredGroups) {
        state.renameChapter(fromName, toName);
        state.reconcile(discoveredGroups);
        save();
    }

    public String chapterIcon(String group) {
        return state.chapterIcon(group);
    }

    public String chapterBackground(String group) {
        return state.chapterBackground(group);
    }

    public String chapterCanvasBackground(String group) {
        return state.chapterCanvasBackground(group);
    }

    public String chapterTextAlign(String group) {
        return state.chapterTextAlign(group);
    }

    public int chapterTextColor(String group) {
        return state.chapterTextColor(group);
    }

    public String chapterTextStyle(String group) {
        return state.chapterTextStyle(group);
    }

    public int chapterTextSize(String group) {
        return state.chapterTextSize(group);
    }

    public boolean chapterLockUntilUnlocked(String group) {
        return state.chapterLockUntilUnlocked(group);
    }

    public boolean chapterHideUntilUnlocked(String group) {
        return state.chapterHideUntilUnlocked(group);
    }

    public java.util.Map<String, List<CanvasImageLayer>> canvasImagesByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasImagesByChapter);
    }

    public java.util.Map<String, List<CanvasTextLayer>> canvasTextsByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasTextsByChapter);
    }

    public java.util.Map<String, List<String>> canvasLayerOrderByChapter() {
        return ChapterMetadataState.copyLayerMap(state.canvasLayerOrderByChapter);
    }

    public List<CanvasImageLayer> canvasImages(String chapter) {
        return List.copyOf(state.canvasImagesByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<CanvasTextLayer> canvasTexts(String chapter) {
        return List.copyOf(state.canvasTextsByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<String> canvasLayerOrder(String chapter) {
        return List.copyOf(state.canvasLayerOrderByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public List<CanvasExclusiveChoice> canvasExclusiveChoices(String chapter) {
        return List.copyOf(state.canvasExclusiveChoicesByChapter.getOrDefault(ChapterMetadataState.normalizeChapterName(chapter), List.of()));
    }

    public void setChapterIcon(String chapter, String icon) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        String value = icon == null ? "" : icon.trim();
        state.chapterIcons.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter icon {} -> {}", normalized, value);
        save();
    }

    public void setChapterBackground(String chapter, String background) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        String value = background == null || background.isBlank() ? "default" : background.trim();
        state.chapterBackgrounds.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter background {} -> {}", normalized, value);
        save();
    }

    public void setChapterCanvasBackground(String chapter, String background) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        String value = background == null || background.isBlank() ? "default" : background.trim();
        state.chapterCanvasBackgrounds.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter canvas_background {} -> {}", normalized, value);
        save();
    }

    public void setChapterTextAlign(String chapter, String align) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        String value = ChapterMetadataJsonCodec.normalizeTextAlign(align);
        state.chapterTextAlign.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_align {} -> {}", normalized, value);
        save();
    }

    public void setChapterTextColor(String chapter, int color) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        state.chapterTextColor.put(normalized, color);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_color {} -> {}", normalized, color);
        save();
    }

    public void setChapterTextStyle(String chapter, String style) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        String value = ChapterMetadataJsonCodec.normalizeTextStyle(style);
        state.chapterTextStyle.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_style {} -> {}", normalized, value);
        save();
    }

    public void setChapterTextSize(String chapter, int size) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        int value = CanvasTextLayer.clampFontSize(size);
        state.chapterTextSize.put(normalized, value);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter text_size {} -> {}", normalized, value);
        save();
    }

    public void setChapterLockUntilUnlocked(String chapter, boolean lockUntilUnlocked) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        state.chapterLockUntilUnlocked.put(normalized, lockUntilUnlocked);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter lock_until_unlocked {} -> {}", normalized, lockUntilUnlocked);
        save();
    }

    public void setChapterHideUntilUnlocked(String chapter, boolean hideUntilUnlocked) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (normalized.isBlank()) {
            return;
        }
        state.chapterHideUntilUnlocked.put(normalized, hideUntilUnlocked);
        QuestsAndStuffMod.debugLog("[QnS:Store] chapter hide_until_unlocked {} -> {}", normalized, hideUntilUnlocked);
        save();
    }

    public void putCanvasExclusiveChoice(String chapter, CanvasExclusiveChoice ec) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (ec != null && ChapterCanvasLayerMutations.put(state, normalized, ec, ec.id(), "exclusive_choice:" + ec.id(), state.canvasExclusiveChoicesByChapter, CanvasExclusiveChoice::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasExclusiveChoice(String chapter, String ecId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, ecId, "exclusive_choice:" + ecId, state.canvasExclusiveChoicesByChapter, CanvasExclusiveChoice::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void putCanvasImage(String chapter, CanvasImageLayer image) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (image != null && ChapterCanvasLayerMutations.put(state, normalized, image, image.id(), "image:" + image.id(), state.canvasImagesByChapter, CanvasImageLayer::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasImage(String chapter, String imageId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, imageId, "image:" + imageId, state.canvasImagesByChapter, CanvasImageLayer::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void putCanvasText(String chapter, CanvasTextLayer text) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (text != null && ChapterCanvasLayerMutations.put(state, normalized, text, text.id(), "text:" + text.id(), state.canvasTextsByChapter, CanvasTextLayer::id)) {
            saveChapter(normalized);
        }
    }

    public boolean removeCanvasText(String chapter, String textId) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!ChapterCanvasLayerMutations.remove(state, normalized, textId, "text:" + textId, state.canvasTextsByChapter, CanvasTextLayer::id)) {
            return false;
        }
        saveChapter(normalized);
        return true;
    }

    public void setCanvasLayerOrder(String chapter, List<String> order) {
        String normalized = state.ensureChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        ChapterCanvasLayerMutations.setOrder(state, normalized, order);
        saveChapter(normalized);
    }

    public void putCanvasLayers(String chapter, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> order) {
        String normalized = state.ensureChapter(chapter);
        if (normalized.isBlank()) {
            return;
        }
        if (images != null) {
            for (CanvasImageLayer image : images) {
                if (image != null) {
                    ChapterCanvasLayerMutations.put(state, normalized, image, image.id(), "image:" + image.id(), state.canvasImagesByChapter, CanvasImageLayer::id);
                }
            }
        }
        if (texts != null) {
            for (CanvasTextLayer text : texts) {
                if (text != null) {
                    ChapterCanvasLayerMutations.put(state, normalized, text, text.id(), "text:" + text.id(), state.canvasTextsByChapter, CanvasTextLayer::id);
                }
            }
        }
        ChapterCanvasLayerMutations.setOrder(state, normalized, order);
        saveChapter(normalized);
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

    public void saveChapter(String chapter) {
        String normalized = ChapterMetadataState.normalizeChapterName(chapter);
        if (!normalized.isBlank()) {
            ChapterMetadataWriter.saveGroups(chaptersDir, state, GSON, List.of(normalized));
        }
    }

    public void saveChapters(Collection<String> chapters) {
        ChapterMetadataWriter.saveGroups(chaptersDir, state, GSON, chapters);
    }

    public void reconcile(Set<String> discoveredGroups) {
        state.reconcile(discoveredGroups);
    }

    private static <T> Map<String, List<T>> mutableLayerMap(Map<String, List<T>> source) {
        Map<String, List<T>> copy = new HashMap<>();
        if (source == null) {
            return copy;
        }
        for (Map.Entry<String, List<T>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

}
