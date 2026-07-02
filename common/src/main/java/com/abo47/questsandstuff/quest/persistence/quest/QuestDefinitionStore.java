package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.persistence.chapter.ChapterMetadataSnapshot;
import com.abo47.questsandstuff.quest.persistence.chapter.ChapterMetadataStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.cloneDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizePrerequisites;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.removeUngroupedDefinitions;

public final class QuestDefinitionStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path root;
    private final Path questsDir;
    private final Path chaptersDir;
    private final Map<String, QuestDefinition> quests = new HashMap<>();
    private final ChapterMetadataStore chapters;
    private final QuestlineManifestStore manifest;
    private final QuestDefinitionSaveQueue saveQueue;
    private final QuestDefinitionMutations mutations;

    public QuestDefinitionStore(Path root) {
        this.root = root;
        this.questsDir = root.resolve("quests");
        this.chaptersDir = root.resolve("chapters");
        this.chapters = new ChapterMetadataStore(chaptersDir);
        this.manifest = new QuestlineManifestStore(root);
        this.saveQueue = new QuestDefinitionSaveQueue(questsDir, GSON);
        this.mutations = new QuestDefinitionMutations(questsDir, quests, chapters, saveQueue);
    }

    public Map<String, QuestDefinition> quests() {
        return Map.copyOf(quests);
    }

    public QuestDefinition quest(String questId) {
        return quests.get(normalizeQuestId(questId));
    }

    public boolean containsQuest(String questId) {
        return quests.containsKey(normalizeQuestId(questId));
    }

    public Set<String> questIds() {
        return Set.copyOf(quests.keySet());
    }

    public List<QuestDefinition> questDefinitions() {
        return List.copyOf(quests.values());
    }

    public Path clipboardDir() {
        return root.resolve("clipboard");
    }

    public Map<String, QuestDefinition> snapshot() {
        Map<String, QuestDefinition> snapshot = new HashMap<>();
        for (Map.Entry<String, QuestDefinition> entry : quests.entrySet()) {
            snapshot.put(entry.getKey(), cloneDefinition(entry.getValue()));
        }
        return snapshot;
    }

    public EditorSnapshot editorSnapshot() {
        return new EditorSnapshot(snapshot(), chapters.snapshot());
    }

    public List<String> chapterOrder() {
        return chapters.chapterOrder();
    }

    public void setChapterOrder(List<String> chapters) {
        this.chapters.setChapterOrder(chapters, mutations.discoverChapters());
    }

    public void renameChapterMetadata(String fromName, String toName) {
        this.chapters.renameChapter(fromName, toName, mutations.discoverChapters());
    }

    public String chapterIcon(String chapter) {
        return chapters.chapterIcon(chapter);
    }

    public String chapterBackground(String chapter) {
        return chapters.chapterBackground(chapter);
    }
    public String chapterCanvasBackground(String chapter) {
        return chapters.chapterCanvasBackground(chapter);
    }
    public String chapterTextAlign(String chapter) {
        return chapters.chapterTextAlign(chapter);
    }
    public int chapterTextColor(String chapter) {
        return chapters.chapterTextColor(chapter);
    }
    public String chapterTextStyle(String chapter) {
        return chapters.chapterTextStyle(chapter);
    }

    public int chapterTextSize(String chapter) {
        return chapters.chapterTextSize(chapter);
    }

    public boolean chapterLockUntilUnlocked(String chapter) {
        return chapters.chapterLockUntilUnlocked(chapter);
    }

    public boolean chapterHideUntilUnlocked(String chapter) {
        return chapters.chapterHideUntilUnlocked(chapter);
    }

    public Map<String, List<CanvasImageLayer>> canvasImagesByChapter() {
        return chapters.canvasImagesByChapter();
    }

    public Map<String, List<CanvasTextLayer>> canvasTextsByChapter() {
        return chapters.canvasTextsByChapter();
    }

    public Map<String, List<String>> canvasLayerOrderByChapter() {
        return chapters.canvasLayerOrderByChapter();
    }

    public List<CanvasImageLayer> canvasImages(String chapter) {
        return chapters.canvasImages(chapter);
    }

    public List<CanvasTextLayer> canvasTexts(String chapter) {
        return chapters.canvasTexts(chapter);
    }

    public List<CanvasExclusiveChoice> canvasExclusiveChoices(String chapter) {
        return chapters.canvasExclusiveChoices(chapter);
    }

    public List<String> canvasLayerOrder(String chapter) {
        return chapters.canvasLayerOrder(chapter);
    }

    public void setChapterIcon(String chapter, String icon) {
        chapters.setChapterIcon(chapter, icon);
    }

    public void setChapterBackground(String chapter, String background) {
        chapters.setChapterBackground(chapter, background);
    }
    public void setChapterCanvasBackground(String chapter, String background) {
        chapters.setChapterCanvasBackground(chapter, background);
    }
    public void setChapterTextAlign(String chapter, String align) {
        chapters.setChapterTextAlign(chapter, align);
    }
    public void setChapterTextColor(String chapter, int color) {
        chapters.setChapterTextColor(chapter, color);
    }
    public void setChapterTextStyle(String chapter, String style) {
        chapters.setChapterTextStyle(chapter, style);
    }

    public void setChapterTextSize(String chapter, int size) {
        chapters.setChapterTextSize(chapter, size);
    }

    public void setChapterLockUntilUnlocked(String chapter, boolean lockUntilUnlocked) {
        chapters.setChapterLockUntilUnlocked(chapter, lockUntilUnlocked);
    }

    public void setChapterHideUntilUnlocked(String chapter, boolean hideUntilUnlocked) {
        chapters.setChapterHideUntilUnlocked(chapter, hideUntilUnlocked);
    }

    public void putCanvasImage(String chapter, CanvasImageLayer image) {
        chapters.putCanvasImage(chapter, image);
    }

    public boolean removeCanvasImage(String chapter, String imageId) {
        return chapters.removeCanvasImage(chapter, imageId);
    }

    public void putCanvasText(String chapter, CanvasTextLayer text) {
        chapters.putCanvasText(chapter, text);
    }

    public boolean removeCanvasText(String chapter, String textId) {
        return chapters.removeCanvasText(chapter, textId);
    }

    public void putCanvasExclusiveChoice(String chapter, CanvasExclusiveChoice ec) {
        chapters.putCanvasExclusiveChoice(chapter, ec);
    }

    public boolean removeCanvasExclusiveChoice(String chapter, String ecId) {
        return chapters.removeCanvasExclusiveChoice(chapter, ecId);
    }

    public void setCanvasLayerOrder(String chapter, List<String> order) {
        chapters.setCanvasLayerOrder(chapter, order);
    }

    public void putCanvasLayers(String chapter, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> order) {
        chapters.putCanvasLayers(chapter, images, texts, order);
    }

    public void replaceAll(Map<String, QuestDefinition> replacement) {
        mutations.replaceAll(replacement);
    }

    public void replaceAll(EditorSnapshot replacement) {
        if (replacement == null) {
            return;
        }
        replaceAll(replacement.quests());
        chapters.restore(replacement.chapters());
        chapters.reconcile(mutations.discoverChapters());
        chapters.save();
    }

    public void load() {
        try {
            Map<String, QuestDefinition> loaded = QuestDefinitionLoader.load(questsDir);
            normalizePrerequisites(loaded);
            removeUngroupedDefinitions(loaded);

            quests.clear();
            quests.putAll(loaded);
            chapters.load(mutations.discoverChapters());
            manifest.ensureExists();

            QuestDefinitionFileCleanup.cleanupStaleQuestFiles(questsDir, quests);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.error("Failed to load quest definitions, keeping last in-memory good state", e);
        }
    }

    public void shutdown() {
        saveQueue.shutdown();
    }

    public void upsert(QuestDefinition definition) {
        mutations.upsert(definition);
    }

    public void upsertAll(List<QuestDefinition> definitions) {
        mutations.upsertAll(definitions);
    }

    public void remove(String questId) {
        mutations.remove(questId);
    }

    public void markDirty(String questId) {
        mutations.markDirty(questId);
    }

    public void saveNow(String questId) {
        mutations.saveNow(questId);
    }

    public void saveNow(Collection<String> questIds) {
        mutations.saveNow(questIds);
    }

    public void saveAll() {
        saveQueue.saveAll(snapshot());
        chapters.save();
        manifest.save();
        QuestDefinitionFileCleanup.cleanupStaleQuestFiles(questsDir, quests);
    }

    public record EditorSnapshot(Map<String, QuestDefinition> quests, ChapterMetadataSnapshot chapters) {
    }

}

