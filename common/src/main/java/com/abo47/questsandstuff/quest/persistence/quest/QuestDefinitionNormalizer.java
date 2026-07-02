package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class QuestDefinitionNormalizer {
    private QuestDefinitionNormalizer() {
    }

    static QuestDefinition cloneDefinition(QuestDefinition definition) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                new QuestDisplay(
                        definition.display().title(),
                        definition.display().subtitle(),
                        new ArrayList<>(definition.display().description()),
                        new HashMap<>(definition.display().chapters()),
                        definition.display().icon(),
                        definition.display().iconBackground(),
                        definition.display().completionSound(),
                        definition.display().completionSoundVolume(),
                        definition.display().completionHudBackground(),
                        definition.display().visualHidden(),
                        definition.display().questBackground(),
                        definition.display().questBackgroundGrayscale()
                ),
                new QuestSettings(
                        definition.settings().individualProgress(),
                        definition.settings().hiddenMode(),
                        definition.settings().repeatable(),
                        definition.settings().autoClaimRewards(),
                        definition.settings().unlockNotification(),
                        definition.settings().showPrerequisiteArrow()
                ),
                Set.copyOf(definition.prerequisites()),
                new HashMap<>(definition.connectionColors()),
                new HashMap<>(definition.connectionModes()),
                Set.copyOf(definition.hiddenConnections()),
                new HashMap<>(definition.connectionTextures()),
                new HashMap<>(definition.connectionTextureSpacings()),
                new ArrayList<>(definition.tasksOrder()),
                new ArrayList<>(definition.rewardsOrder()),
                orderedCopy(definition.tasks()),
                orderedCopy(definition.rewards())
        );
    }

    static QuestDefinition withId(QuestDefinition definition, String id) {
        return new QuestDefinition(
                definition.schema(),
                id,
                definition.display(),
                definition.settings(),
                definition.prerequisites(),
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    static String normalizeQuestId(String questId) {
        return QuestIdentity.questIdOrDefault(questId);
    }

    private static <T> Map<String, T> orderedCopy(Map<String, T> source) {
        return source == null || source.isEmpty() ? Map.of() : new LinkedHashMap<>(source);
    }

    static void normalizePrerequisites(Map<String, QuestDefinition> definitions) {
        Set<String> knownIds = new HashSet<>(definitions.keySet());
        for (Map.Entry<String, QuestDefinition> entry : new ArrayList<>(definitions.entrySet())) {
            definitions.put(entry.getKey(), normalizeDefinition(entry.getValue(), knownIds));
        }
    }

    static void removeUngroupedDefinitions(Map<String, QuestDefinition> definitions) {
        for (Map.Entry<String, QuestDefinition> entry : new ArrayList<>(definitions.entrySet())) {
            if (hasAnyGroup(entry.getValue())) {
                continue;
            }
            definitions.remove(entry.getKey());
            QuestsAndStuffMod.debugLog("[QnS:Store] dropped groupless quest {} during load", entry.getKey());
        }
    }

    static QuestDefinition normalizeDefinition(QuestDefinition definition, Set<String> knownIds) {
        Set<String> prerequisites = new HashSet<>();
        for (String prerequisite : definition.prerequisites()) {
            if (prerequisite == null || prerequisite.isBlank()) {
                continue;
            }
            String normalizedPrerequisite = normalizeQuestId(prerequisite);
            if (normalizedPrerequisite.equals(definition.id())) {
                continue;
            }
            if (!knownIds.contains(normalizedPrerequisite)) {
                continue;
            }
            prerequisites.add(normalizedPrerequisite);
        }
        Map<String, String> filteredTextures = filterConnectionTextures(definition.connectionTextures(), prerequisites);

        return new QuestDefinition(
                definition.schema(),
                normalizeQuestId(definition.id()),
                definition.display(),
                definition.settings(),
                Set.copyOf(prerequisites),
                filterConnectionColors(definition.connectionColors(), prerequisites),
                filterConnectionModes(definition.connectionModes(), prerequisites),
                filterHiddenConnections(definition.hiddenConnections(), prerequisites),
                filteredTextures,
                filterConnectionTextureSpacings(definition.connectionTextureSpacings(), prerequisites),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    static boolean hasAnyGroup(QuestDefinition definition) {
        return definition != null
                && definition.display() != null
                && definition.display().chapters() != null
                && !definition.display().chapters().isEmpty();
    }

    static String chapterFolderName(String group) {
        return QuestIdentity.chapterFolderName(group);
    }

    static String primaryChapter(QuestDefinition definition) {
        if (definition.display().chapters().isEmpty()) {
            return "";
        }
        return definition.display().chapters().keySet().stream().sorted().findFirst().orElse("");
    }

    private static Map<String, String> filterConnectionModes(Map<String, String> modes, Set<String> prerequisites) {
        if (modes == null || modes.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : modes.entrySet()) {
            String key = normalizeQuestId(entry.getKey());
            String mode = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!key.isBlank() && prerequisites.contains(key) && "grid".equals(mode)) {
                filtered.put(key, mode);
            }
        }
        return Map.copyOf(filtered);
    }

    private static Set<String> filterHiddenConnections(Set<String> hidden, Set<String> prerequisites) {
        if (hidden == null || hidden.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Set.of();
        }
        Set<String> filtered = new HashSet<>();
        for (String entry : hidden) {
            String key = normalizeQuestId(entry);
            if (!key.isBlank() && prerequisites.contains(key)) {
                filtered.add(key);
            }
        }
        return Set.copyOf(filtered);
    }

    private static Map<String, Integer> filterConnectionColors(Map<String, Integer> colors, Set<String> prerequisites) {
        if (colors == null || colors.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

    private static Map<String, String> filterConnectionTextures(Map<String, String> textures, Set<String> prerequisites) {
        if (textures == null || textures.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null && !entry.getValue().isBlank()) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

    private static Map<String, Integer> filterConnectionTextureSpacings(Map<String, Integer> spacings, Set<String> prerequisites) {
        if (spacings == null || spacings.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> entry : spacings.entrySet()) {
            if (entry.getKey() != null && prerequisites.contains(normalizeQuestId(entry.getKey())) && entry.getValue() != null && entry.getValue() > 0) {
                filtered.put(normalizeQuestId(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(filtered);
    }

}
