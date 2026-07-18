package com.abo47.questsandstuff.quest.editor.quest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;

import com.google.gson.JsonElement;

public final class QuestDefinitionEdits {
    private QuestDefinitionEdits() {
    }

    public static QuestDefinition deepCopyDefinition(QuestDefinition source) {
        if (source == null) {
            return null;
        }
        try {
            JsonElement encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, source)
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
            return QuestDefinition.CODEC.parse(JsonOps.INSTANCE, encoded)
                    .getOrThrow(false, QuestsAndStuffMod.LOGGER::error);
        } catch (Exception ignored) {
            return new QuestDefinition(
                    source.schema(),
                    source.id(),
                    new QuestDisplay(
                            source.display().title(),
                            source.display().subtitle(),
                            List.copyOf(source.display().description()),
                            new HashMap<>(source.display().chapters()),
                            source.display().icon(),
                            source.display().iconBackground(),
                            source.display().completionSound(),
                            source.display().completionSoundVolume(),
                            source.display().completionHudBackground(),
                            source.display().visualHidden(),
                            source.display().questBackground(),
                            source.display().questBackgroundGrayscale()
                    ),
                    new QuestSettings(
                            source.settings().individualProgress(),
                            source.settings().hiddenMode(),
                            source.settings().repeatable(),
                            source.settings().autoClaimRewards(),
                            source.settings().unlockNotification(),
                            source.settings().showPrerequisiteArrow()
                    ),
                    Set.copyOf(source.prerequisites()),
                    Map.copyOf(source.connectionColors()),
                    Map.copyOf(source.connectionModes()),
                    Set.copyOf(source.hiddenConnections()),
                    Map.copyOf(source.connectionTextures()),
                    Map.copyOf(source.connectionTextureSpacings()),
                    List.of(),
                    List.of(),
                    orderedCopy(source.tasks()),
                    orderedCopy(source.rewards())
            );
        }
    }

    public static QuestDefinition withPrerequisites(QuestDefinition definition, Set<String> prerequisites) {
        Set<String> copiedPrerequisites = Set.copyOf(prerequisites);
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                copiedPrerequisites,
                pruneConnectionColors(definition.connectionColors(), copiedPrerequisites),
                pruneConnectionModes(definition.connectionModes(), copiedPrerequisites),
                pruneHiddenConnections(definition.hiddenConnections(), copiedPrerequisites),
                pruneConnectionTextures(definition.connectionTextures(), copiedPrerequisites),
                pruneConnectionTextureSpacings(definition.connectionTextureSpacings(), copiedPrerequisites),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withoutPrerequisite(QuestDefinition definition, String prerequisiteId) {
        String normalizedPrerequisite = normalizeQuestId(prerequisiteId);
        if (definition == null || normalizedPrerequisite.isBlank() || !definition.prerequisites().contains(normalizedPrerequisite)) {
            return definition;
        }
        Set<String> prerequisites = new HashSet<>(definition.prerequisites());
        prerequisites.remove(normalizedPrerequisite);
        return withPrerequisites(definition, prerequisites);
    }

    public static QuestDefinition withDisplay(QuestDefinition definition, QuestDisplay display) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                display,
                definition.settings(),
                prerequisites,
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

    public static QuestDefinition withSettings(QuestDefinition definition, QuestSettings settings) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                settings,
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

    public static QuestDefinition withConnectionColors(QuestDefinition definition, Map<String, Integer> colors) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                pruneConnectionColors(colors, prerequisites),
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

    public static QuestDefinition withConnectionModes(QuestDefinition definition, Map<String, String> modes) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                pruneConnectionModes(modes, prerequisites),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withHiddenConnections(QuestDefinition definition, Set<String> hiddenConnections) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                pruneHiddenConnections(hiddenConnections, prerequisites),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withConnectionTextures(QuestDefinition definition, Map<String, String> textures) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                pruneConnectionTextures(textures, prerequisites),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withConnectionTextureSpacings(QuestDefinition definition, Map<String, Integer> spacings) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                pruneConnectionTextureSpacings(spacings, prerequisites),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    public static QuestDefinition withChapters(QuestDefinition definition, Map<String, ChapterDef> groups) {
        Set<String> prerequisites = definition.prerequisites();
        QuestDisplay display = definition.display().withChapters(Map.copyOf(groups));
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                display,
                definition.settings(),
                prerequisites,
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

    public static QuestDefinition withTasks(QuestDefinition definition, Map<String, QuestTaskDefinition> tasks) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                orderedCopy(tasks),
                definition.rewards()
        );
    }

    public static QuestDefinition withRewards(QuestDefinition definition, Map<String, QuestRewardDefinition> rewards) {
        Set<String> prerequisites = definition.prerequisites();
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                orderedCopy(rewards)
        );
    }

    private static <T> Map<String, T> orderedCopy(Map<String, T> source) {
        return source == null || source.isEmpty() ? Map.of() : new LinkedHashMap<>(source);
    }

    private static Map<String, Integer> pruneConnectionColors(Map<String, Integer> colors, Set<String> prerequisites) {
        if (colors == null || colors.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            if (!key.isBlank() && prerequisites.contains(key) && entry.getValue() != null) {
                out.put(key, entry.getValue());
            }
        }
        return Map.copyOf(out);
    }

    private static Map<String, String> pruneConnectionModes(Map<String, String> modes, Set<String> prerequisites) {
        if (modes == null || modes.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<String, String> entry : modes.entrySet()) {
            String key = QuestConnectionMetadata.metadataKey(entry.getKey());
            QuestConnectionMode mode = QuestConnectionMode.fromSerializedName(entry.getValue());
            if (!key.isBlank() && prerequisites.contains(key) && mode.storedInQuestMetadata()) {
                out.put(key, mode.serializedName());
            }
        }
        return Map.copyOf(out);
    }

    private static Set<String> pruneHiddenConnections(Set<String> hiddenConnections, Set<String> prerequisites) {
        if (hiddenConnections == null || hiddenConnections.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (String hidden : hiddenConnections) {
            String key = QuestConnectionMetadata.metadataKey(hidden);
            if (!key.isBlank() && prerequisites.contains(key)) {
                out.add(key);
            }
        }
        return Set.copyOf(out);
    }

    private static Map<String, String> pruneConnectionTextures(Map<String, String> textures, Set<String> prerequisites) {
        if (textures == null || textures.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new HashMap<>();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            String key = normalizeQuestId(entry.getKey());
            if (!key.isBlank() && prerequisites.contains(key) && entry.getValue() != null && !entry.getValue().isBlank()) {
                out.put(key, entry.getValue());
            }
        }
        return Map.copyOf(out);
    }

    private static Map<String, Integer> pruneConnectionTextureSpacings(Map<String, Integer> spacings, Set<String> prerequisites) {
        if (spacings == null || spacings.isEmpty() || prerequisites == null || prerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<String, Integer> entry : spacings.entrySet()) {
            String key = normalizeQuestId(entry.getKey());
            if (!key.isBlank() && prerequisites.contains(key) && entry.getValue() != null && entry.getValue() > 0) {
                out.put(key, entry.getValue());
            }
        }
        return Map.copyOf(out);
    }

    private static String normalizeQuestId(String questId) {
        return QuestConnectionMetadata.normalizeQuestId(questId);
    }
}
