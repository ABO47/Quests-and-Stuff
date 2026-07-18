package com.abo47.questsandstuff.quest.runtime.progress;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.StatQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

public final class QuestRuntimeIndex {
    public record TaskBinding(String questId, String taskId, QuestTaskDefinition task) {}

    private final Map<QuestSignalType, List<TaskBinding>> bySignal = new EnumMap<>(QuestSignalType.class);
    private final Map<String, Set<String>> prerequisitesByQuest = new HashMap<>();
    private final Map<String, Set<String>> dependentsByQuest = new HashMap<>();
    private final Map<String, Set<String>> statTargetsByQuest = new HashMap<>();
    private final Map<String, Integer> statTargetCounts = new HashMap<>();
    private final Set<String> trackedStatTaskTargets = new HashSet<>();

    public QuestRuntimeIndex(Map<String, QuestDefinition> quests) {
        for (QuestSignalType type : QuestSignalType.values()) {
            bySignal.put(type, new ArrayList<>());
        }

        for (Map.Entry<String, QuestDefinition> questEntry : quests.entrySet()) {
            upsert(questEntry.getValue());
        }
    }

    public void upsertAll(Iterable<QuestDefinition> definitions) {
        if (definitions == null) {
            return;
        }
        for (QuestDefinition definition : definitions) {
            upsert(definition);
        }
    }

    public void upsert(QuestDefinition definition) {
        if (definition == null || definition.id() == null || definition.id().isBlank()) {
            return;
        }
        String questId = definition.id();
        remove(questId);
        prerequisitesByQuest.put(questId, Set.copyOf(definition.prerequisites()));
        for (String prerequisite : definition.prerequisites()) {
            dependentsByQuest.computeIfAbsent(prerequisite, ignored -> new HashSet<>()).add(questId);
        }

        Set<String> statTargets = new HashSet<>();
        for (Map.Entry<String, QuestTaskDefinition> taskEntry : definition.tasks().entrySet()) {
            QuestTaskDefinition task = taskEntry.getValue();
            TaskBinding binding = new TaskBinding(questId, taskEntry.getKey(), task);
            for (QuestSignalType type : task.signals()) {
                bySignal.get(type).add(binding);
            }
            if (task instanceof StatQuestTaskDefinition stat && !stat.target().isBlank()) {
                statTargets.add(stat.target());
            }
        }
        if (!statTargets.isEmpty()) {
            statTargetsByQuest.put(questId, Set.copyOf(statTargets));
            for (String target : statTargets) {
                statTargetCounts.merge(target, 1, Integer::sum);
                trackedStatTaskTargets.add(target);
            }
        }
    }

    public void remove(String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        for (List<TaskBinding> bindings : bySignal.values()) {
            bindings.removeIf(binding -> questId.equals(binding.questId()));
        }
        Set<String> prerequisites = prerequisitesByQuest.remove(questId);
        if (prerequisites != null) {
            for (String prerequisite : prerequisites) {
                Set<String> dependents = dependentsByQuest.get(prerequisite);
                if (dependents == null) {
                    continue;
                }
                dependents.remove(questId);
                if (dependents.isEmpty()) {
                    dependentsByQuest.remove(prerequisite);
                }
            }
        }
        Set<String> statTargets = statTargetsByQuest.remove(questId);
        if (statTargets != null) {
            for (String target : statTargets) {
                int count = statTargetCounts.getOrDefault(target, 0) - 1;
                if (count <= 0) {
                    statTargetCounts.remove(target);
                    trackedStatTaskTargets.remove(target);
                } else {
                    statTargetCounts.put(target, count);
                }
            }
        }
    }

    public List<TaskBinding> bindings(QuestSignalType type) {
        return bySignal.getOrDefault(type, List.of());
    }

    public Set<String> prerequisites(String questId) {
        return prerequisitesByQuest.getOrDefault(questId, Set.of());
    }

    public Set<String> dependents(String questId) {
        return dependentsByQuest.getOrDefault(questId, Set.of());
    }

    public Set<String> trackedStatTaskTargets() {
        return Set.copyOf(trackedStatTaskTargets);
    }

}
