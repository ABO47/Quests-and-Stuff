package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.chapter.ChapterMetadataStore;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.cloneDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.hasAnyGroup;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeDefinition;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.withId;

final class QuestDefinitionMutations {
    private final Path questsDir;
    private final Map<String, QuestDefinition> quests;
    private final ChapterMetadataStore chapters;
    private final QuestDefinitionSaveQueue saveQueue;

    QuestDefinitionMutations(
            Path questsDir,
            Map<String, QuestDefinition> quests,
            ChapterMetadataStore chapters,
            QuestDefinitionSaveQueue saveQueue
    ) {
        this.questsDir = questsDir;
        this.quests = quests;
        this.chapters = chapters;
        this.saveQueue = saveQueue;
    }

    void replaceAll(Map<String, QuestDefinition> replacement) {
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

    void upsert(QuestDefinition definition) {
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

    void upsertAll(List<QuestDefinition> definitions) {
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

    void remove(String questId) {
        QuestDefinition removed = quests.remove(questId);
        chapters.reconcile(discoverGroups());
        saveQueue.cancel(questId);
        QuestDefinitionFileCleanup.deleteQuestFile(questsDir, questId, removed);
    }

    void markDirty(String questId) {
        QuestDefinition definition = quests.get(questId);
        saveQueue.markDirty(questId, definition);
    }

    void saveNow(String questId) {
        String canonicalId = normalizeQuestId(questId);
        QuestDefinition definition = quests.get(canonicalId);
        saveQueue.saveNow(canonicalId, definition);
    }

    void saveNow(Collection<String> questIds) {
        if (questIds != null) {
            for (String questId : questIds) {
                saveNow(questId);
            }
        }
    }

    Set<String> discoverGroups() {
        Set<String> groups = new TreeSet<>();
        for (QuestDefinition definition : quests.values()) {
            groups.addAll(definition.display().groups().keySet());
        }
        return groups;
    }
}
