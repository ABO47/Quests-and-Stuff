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

    public List<String> groupOrder() {
        return chapters.groupOrder();
    }

    public void setGroupOrder(List<String> groups) {
        chapters.setGroupOrder(groups, mutations.discoverGroups());
    }

    public void renameGroupMetadata(String fromName, String toName) {
        chapters.renameGroup(fromName, toName, mutations.discoverGroups());
    }

    public String groupIcon(String group) {
        return chapters.groupIcon(group);
    }

    public String groupBackground(String group) {
        return chapters.groupBackground(group);
    }
    public String groupCanvasBackground(String group) {
        return chapters.groupCanvasBackground(group);
    }
    public String groupTextAlign(String group) {
        return chapters.groupTextAlign(group);
    }
    public int groupTextColor(String group) {
        return chapters.groupTextColor(group);
    }
    public String groupTextStyle(String group) {
        return chapters.groupTextStyle(group);
    }

    public int groupTextSize(String group) {
        return chapters.groupTextSize(group);
    }

    public boolean groupLockUntilUnlocked(String group) {
        return chapters.groupLockUntilUnlocked(group);
    }

    public boolean groupHideUntilUnlocked(String group) {
        return chapters.groupHideUntilUnlocked(group);
    }

    public Map<String, List<CanvasImageLayer>> canvasImagesByGroup() {
        return chapters.canvasImagesByGroup();
    }

    public Map<String, List<CanvasTextLayer>> canvasTextsByGroup() {
        return chapters.canvasTextsByGroup();
    }

    public Map<String, List<String>> canvasLayerOrderByGroup() {
        return chapters.canvasLayerOrderByGroup();
    }

    public List<CanvasImageLayer> canvasImages(String group) {
        return chapters.canvasImages(group);
    }

    public List<CanvasTextLayer> canvasTexts(String group) {
        return chapters.canvasTexts(group);
    }

    public List<CanvasExclusiveChoice> canvasExclusiveChoices(String group) {
        return chapters.canvasExclusiveChoices(group);
    }

    public List<String> canvasLayerOrder(String group) {
        return chapters.canvasLayerOrder(group);
    }

    public void setGroupIcon(String group, String icon) {
        chapters.setGroupIcon(group, icon);
    }

    public void setGroupBackground(String group, String background) {
        chapters.setGroupBackground(group, background);
    }
    public void setGroupCanvasBackground(String group, String background) {
        chapters.setGroupCanvasBackground(group, background);
    }
    public void setGroupTextAlign(String group, String align) {
        chapters.setGroupTextAlign(group, align);
    }
    public void setGroupTextColor(String group, int color) {
        chapters.setGroupTextColor(group, color);
    }
    public void setGroupTextStyle(String group, String style) {
        chapters.setGroupTextStyle(group, style);
    }

    public void setGroupTextSize(String group, int size) {
        chapters.setGroupTextSize(group, size);
    }

    public void setGroupLockUntilUnlocked(String group, boolean lockUntilUnlocked) {
        chapters.setGroupLockUntilUnlocked(group, lockUntilUnlocked);
    }

    public void setGroupHideUntilUnlocked(String group, boolean hideUntilUnlocked) {
        chapters.setGroupHideUntilUnlocked(group, hideUntilUnlocked);
    }

    public void putCanvasImage(String group, CanvasImageLayer image) {
        chapters.putCanvasImage(group, image);
    }

    public boolean removeCanvasImage(String group, String imageId) {
        return chapters.removeCanvasImage(group, imageId);
    }

    public void putCanvasText(String group, CanvasTextLayer text) {
        chapters.putCanvasText(group, text);
    }

    public boolean removeCanvasText(String group, String textId) {
        return chapters.removeCanvasText(group, textId);
    }

    public void putCanvasExclusiveChoice(String group, CanvasExclusiveChoice ec) {
        chapters.putCanvasExclusiveChoice(group, ec);
    }

    public boolean removeCanvasExclusiveChoice(String group, String ecId) {
        return chapters.removeCanvasExclusiveChoice(group, ecId);
    }

    public void setCanvasLayerOrder(String group, List<String> order) {
        chapters.setCanvasLayerOrder(group, order);
    }

    public void putCanvasLayers(String group, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<String> order) {
        chapters.putCanvasLayers(group, images, texts, order);
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
        chapters.reconcile(mutations.discoverGroups());
        chapters.save();
    }

    public void load() {
        try {
            Map<String, QuestDefinition> loaded = QuestDefinitionLoader.load(questsDir);
            normalizePrerequisites(loaded);
            removeUngroupedDefinitions(loaded);

            quests.clear();
            quests.putAll(loaded);
            chapters.load(mutations.discoverGroups());
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

