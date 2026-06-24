package com.abo47.questsandstuff.quest.editor.clipboard;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ClipboardDefinitionCopier {
    private ClipboardDefinitionCopier() {
    }

    public static QuestDefinition duplicateDefinition(QuestDefinition source, String newId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        Set<String> prerequisites = copyPrerequisites(source.prerequisites(), copiedIds);
        QuestDisplay display = source.display().withGroups(Map.of(group, new ChapterDefinition(true, x, y, scale)));
        return new QuestDefinition(
                source.schema(),
                newId,
                display,
                source.settings(),
                prerequisites,
                copyConnectionColors(source.connectionColors(), copiedIds, prerequisites),
                copyConnectionModes(source.connectionModes(), copiedIds, prerequisites),
                copyHiddenConnections(source.hiddenConnections(), copiedIds, prerequisites),
                copyConnectionTextures(source.connectionTextures(), copiedIds),
                copyConnectionTextureSpacings(source.connectionTextureSpacings(), copiedIds),
                source.tasksOrder(),
                source.rewardsOrder(),
                copyTasks(source.tasks(), copiedIds),
                copyRewards(source.rewards(), copiedIds)
        );
    }

    public static float normalizeScale(Float scale, float fallback) {
        float value = scale == null ? fallback : scale;
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            value = 1.0f;
        }
        return Math.max(0.5f, value);
    }

    private static Set<String> copyPrerequisites(Set<String> prerequisites, Map<String, String> copiedIds) {
        if (prerequisites == null || prerequisites.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return Set.of();
        }
        Set<String> copiedPrerequisites = new LinkedHashSet<>();
        for (String prerequisite : prerequisites) {
            String mapped = copiedIds.get(prerequisite);
            if (mapped != null && !mapped.isBlank()) {
                copiedPrerequisites.add(mapped);
            }
        }
        return Set.copyOf(copiedPrerequisites);
    }

    private static Map<String, Integer> copyConnectionColors(Map<String, Integer> colors, Map<String, String> copiedIds, Set<String> copiedPrerequisites) {
        if (colors == null || colors.isEmpty() || copiedIds == null || copiedIds.isEmpty() || copiedPrerequisites == null || copiedPrerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> copied = new HashMap<>();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            String mapped = copiedIds.get(entry.getKey());
            if (mapped != null && copiedPrerequisites.contains(mapped) && entry.getValue() != null) {
                copied.put(mapped, entry.getValue());
            }
        }
        return Map.copyOf(copied);
    }

    private static Map<String, String> copyConnectionTextures(Map<String, String> textures, Map<String, String> copiedIds) {
        if (textures == null || textures.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> copied = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : textures.entrySet()) {
            String mapped = copiedIds.get(entry.getKey());
            String texture = entry.getValue() == null ? "" : entry.getValue().trim();
            if (mapped != null && !texture.isBlank()) {
                copied.put(mapped, texture);
            }
        }
        return Map.copyOf(copied);
    }

    private static Map<String, Integer> copyConnectionTextureSpacings(Map<String, Integer> spacings, Map<String, String> copiedIds) {
        if (spacings == null || spacings.isEmpty() || copiedIds == null || copiedIds.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> copied = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : spacings.entrySet()) {
            String mapped = copiedIds.get(entry.getKey());
            if (mapped != null && entry.getValue() != null) {
                copied.put(mapped, entry.getValue());
            }
        }
        return Map.copyOf(copied);
    }

    private static Map<String, String> copyConnectionModes(Map<String, String> modes, Map<String, String> copiedIds, Set<String> copiedPrerequisites) {
        if (modes == null || modes.isEmpty() || copiedIds == null || copiedIds.isEmpty() || copiedPrerequisites == null || copiedPrerequisites.isEmpty()) {
            return Map.of();
        }
        Map<String, String> copied = new HashMap<>();
        for (Map.Entry<String, String> entry : modes.entrySet()) {
            String mapped = copiedIds.get(entry.getKey());
            String mode = entry.getValue() == null ? "" : entry.getValue().trim();
            if (mapped != null && copiedPrerequisites.contains(mapped) && "grid".equals(mode)) {
                copied.put(mapped, mode);
            }
        }
        return Map.copyOf(copied);
    }

    private static Set<String> copyHiddenConnections(Set<String> hiddenConnections, Map<String, String> copiedIds, Set<String> copiedPrerequisites) {
        if (hiddenConnections == null || hiddenConnections.isEmpty() || copiedIds == null || copiedIds.isEmpty() || copiedPrerequisites == null || copiedPrerequisites.isEmpty()) {
            return Set.of();
        }
        Set<String> copied = new HashSet<>();
        for (String hidden : hiddenConnections) {
            String mapped = copiedIds.get(hidden);
            if (mapped != null && copiedPrerequisites.contains(mapped)) {
                copied.add(mapped);
            }
        }
        return Set.copyOf(copied);
    }

    private static Map<String, QuestTaskDefinition> copyTasks(Map<String, QuestTaskDefinition> tasks, Map<String, String> copiedIds) {
        if (tasks == null || tasks.isEmpty()) {
            return Map.of();
        }
        Map<String, QuestTaskDefinition> copied = new LinkedHashMap<>();
        for (Map.Entry<String, QuestTaskDefinition> entry : tasks.entrySet()) {
            QuestTaskDefinition task = entry.getValue();
            if (task == null) {
                continue;
            }
            copied.put(entry.getKey(), task.copyForQuest(copiedIds == null ? Map.of() : copiedIds));
        }
        return copied;
    }

    private static Map<String, QuestRewardDefinition> copyRewards(Map<String, QuestRewardDefinition> rewards, Map<String, String> copiedIds) {
        if (rewards == null || rewards.isEmpty()) {
            return Map.of();
        }
        Map<String, QuestRewardDefinition> copied = new LinkedHashMap<>();
        for (Map.Entry<String, QuestRewardDefinition> entry : rewards.entrySet()) {
            QuestRewardDefinition reward = entry.getValue();
            if (reward == null) {
                continue;
            }
            copied.put(entry.getKey(), reward.copyForQuest(copiedIds == null ? Map.of() : copiedIds));
        }
        return copied;
    }
}
