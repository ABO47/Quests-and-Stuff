package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.persistence.chapter.ChapterMetadataStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.cloneDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.hasAnyGroup;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizePrerequisites;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.removeUngroupedDefinitions;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.withId;

public final class QuestDefinitionStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path root;
    private final Path questsDir;
    private final Path chaptersDir;
    private final Map<String, QuestDefinition> quests = new HashMap<>();
    private final ChapterMetadataStore chapters;
    private final QuestlineManifestStore manifest;
    private final QuestDefinitionSaveQueue saveQueue;

    public QuestDefinitionStore(Path root) {
        this.root = root;
        this.questsDir = root.resolve("quests");
        this.chaptersDir = root.resolve("chapters");
        this.chapters = new ChapterMetadataStore(chaptersDir);
        this.manifest = new QuestlineManifestStore(root);
        this.saveQueue = new QuestDefinitionSaveQueue(questsDir, GSON);
    }

    public Map<String, QuestDefinition> quests() {
        return Map.copyOf(quests);
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

    public List<String> groupOrder() {
        return chapters.groupOrder();
    }

    public void setGroupOrder(List<String> groups) {
        chapters.setGroupOrder(groups, discoverGroups());
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

    public void setCanvasLayerOrder(String group, List<String> order) {
        chapters.setCanvasLayerOrder(group, order);
    }

    public void replaceAll(Map<String, QuestDefinition> replacement) {
        Set<String> previousIds = new HashSet<>(quests.keySet());

        quests.clear();
        for (Map.Entry<String, QuestDefinition> entry : replacement.entrySet()) {
            QuestDefinition clone = cloneDefinition(entry.getValue());
            if (!hasAnyGroup(clone)) {
                QuestsAndStuffMod.debugLog("[QnS:Store] skipped groupless replacement quest {}", entry.getKey());
                continue;
            }
            quests.put(entry.getKey(), clone);
            markDirty(entry.getKey());
        }

        for (String removedId : previousIds) {
            if (!quests.containsKey(removedId)) {
                remove(removedId);
            }
        }
        chapters.reconcile(discoverGroups());
        QuestDefinitionFileCleanup.cleanupStaleQuestFiles(questsDir, quests);
    }

    public void load() {
        try {
            Map<String, QuestDefinition> loaded = QuestDefinitionLoader.load(questsDir);
            normalizePrerequisites(loaded);
            removeUngroupedDefinitions(loaded);

            quests.clear();
            quests.putAll(loaded);
            chapters.load(discoverGroups());
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
        String canonicalId = normalizeQuestId(definition.id());
        Set<String> knownIds = new HashSet<>(quests.keySet());
        knownIds.add(canonicalId);
        QuestDefinition normalized = normalizeDefinition(withId(definition, canonicalId), knownIds);
        if (!hasAnyGroup(normalized)) {
            QuestsAndStuffMod.debugLog("[QnS:Store] removing groupless quest {}", canonicalId);
            remove(canonicalId);
            return;
        }
        quests.put(canonicalId, normalized);
        chapters.reconcile(discoverGroups());
        markDirty(canonicalId);
    }

    public void upsertAll(List<QuestDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }
        Map<String, QuestDefinition> candidates = new HashMap<>();
        Set<String> knownIds = new HashSet<>(quests.keySet());
        for (QuestDefinition definition : definitions) {
            if (definition == null) {
                continue;
            }
            String canonicalId = normalizeQuestId(definition.id());
            if (canonicalId.isBlank()) {
                continue;
            }
            QuestDefinition candidate = withId(definition, canonicalId);
            if (!hasAnyGroup(candidate)) {
                continue;
            }
            candidates.put(canonicalId, candidate);
            knownIds.add(canonicalId);
        }
        if (candidates.isEmpty()) {
            return;
        }
        for (Map.Entry<String, QuestDefinition> entry : candidates.entrySet()) {
            QuestDefinition normalized = normalizeDefinition(entry.getValue(), knownIds);
            if (!hasAnyGroup(normalized)) {
                remove(entry.getKey());
                continue;
            }
            quests.put(entry.getKey(), normalized);
            markDirty(entry.getKey());
        }
        chapters.reconcile(discoverGroups());
    }

    public void remove(String questId) {
        QuestDefinition removed = quests.remove(questId);
        chapters.reconcile(discoverGroups());
        saveQueue.cancel(questId);
        QuestDefinitionFileCleanup.deleteQuestFile(questsDir, questId, removed);
    }

    public void markDirty(String questId) {
        QuestDefinition definition = quests.get(questId);
        saveQueue.markDirty(questId, definition);
    }

    public void saveNow(String questId) {
        String canonicalId = normalizeQuestId(questId);
        QuestDefinition definition = quests.get(canonicalId);
        saveQueue.saveNow(canonicalId, definition);
    }

    public void saveAll() {
        saveQueue.saveAll(snapshot());
        chapters.save();
        manifest.save();
        QuestDefinitionFileCleanup.cleanupStaleQuestFiles(questsDir, quests);
    }

    private Set<String> discoverGroups() {
        Set<String> groups = new TreeSet<>();
        for (QuestDefinition definition : quests.values()) {
            groups.addAll(definition.display().groups().keySet());
        }
        return groups;
    }

}

