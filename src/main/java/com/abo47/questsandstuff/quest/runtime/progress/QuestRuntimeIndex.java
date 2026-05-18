package com.abo47.questsandstuff.quest.runtime.progress;

import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.StatQuestTaskDefinition;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class QuestRuntimeIndex {
    public record TaskBinding(String questId, String taskId, QuestTaskDefinition task) {}

    private final Map<QuestSignalType, List<TaskBinding>> bySignal = new EnumMap<>(QuestSignalType.class);
    private final Map<String, Set<String>> prerequisitesByQuest = new HashMap<>();
    private final Map<String, Set<String>> dependentsByQuest = new HashMap<>();
    private final Set<String> trackedStatTaskTargets = new HashSet<>();

    public QuestRuntimeIndex(Map<String, QuestDefinition> quests) {
        for (QuestSignalType type : QuestSignalType.values()) {
            bySignal.put(type, new ArrayList<>());
        }

        for (Map.Entry<String, QuestDefinition> questEntry : quests.entrySet()) {
            String questId = questEntry.getKey();
            QuestDefinition quest = questEntry.getValue();
            prerequisitesByQuest.put(questId, Set.copyOf(quest.prerequisites()));
            for (String prerequisite : quest.prerequisites()) {
                dependentsByQuest.computeIfAbsent(prerequisite, ignored -> new HashSet<>()).add(questId);
            }

            for (Map.Entry<String, QuestTaskDefinition> taskEntry : questEntry.getValue().tasks().entrySet()) {
                QuestTaskDefinition task = taskEntry.getValue();
                TaskBinding binding = new TaskBinding(questId, taskEntry.getKey(), task);
                for (QuestSignalType type : task.signals()) {
                    bySignal.get(type).add(binding);
                }
                if (task instanceof StatQuestTaskDefinition stat && !stat.target().isBlank()) {
                    trackedStatTaskTargets.add(stat.target());
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
