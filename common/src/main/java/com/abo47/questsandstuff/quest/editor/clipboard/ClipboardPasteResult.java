package com.abo47.questsandstuff.quest.editor.clipboard;

import com.abo47.questsandstuff.quest.model.QuestDefinition;

import java.util.List;
import java.util.Map;

public record ClipboardPasteResult(
        List<QuestDefinition> createdQuests,
        Map<String, String> remappedIds,
        int droppedExternalPrerequisiteConnections
) {
    public ClipboardPasteResult {
        createdQuests = createdQuests == null ? List.of() : List.copyOf(createdQuests);
        remappedIds = remappedIds == null ? Map.of() : Map.copyOf(remappedIds);
        droppedExternalPrerequisiteConnections = Math.max(0, droppedExternalPrerequisiteConnections);
    }

    public List<String> selectionIds() {
        return createdQuests.stream().map(QuestDefinition::id).toList();
    }
}
