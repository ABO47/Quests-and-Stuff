package com.abo47.questsandstuff.quest.editor.session;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorSession;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.util.naming.QuestNaming;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class EditorSessionState {
    private final QuestDefinitionStore definitionStore;

    EditorSessionState(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    EditorSession createSession() {
        EditorSession session = new EditorSession();
        List<String> groups = groups();
        session.currentGroup = groups.isEmpty() ? "" : groups.get(0);
        normalizeQuestSelection(session);
        return session;
    }

    void normalizeQuestSelection(EditorSession session) {
        List<String> questIds = questIdsInGroup(session.currentGroup);
        if (questIds.isEmpty()) {
            session.currentQuest = "-";
            return;
        }
        if (!questIds.contains(session.currentQuest)) {
            session.currentQuest = questIds.get(0);
        }
    }

    List<String> groups() {
        return new ArrayList<>(definitionStore.groupOrder());
    }

    List<String> questIdsInGroup(String group) {
        return definitionStore.questDefinitions().stream()
                .filter(quest -> quest.display().groups().containsKey(group))
                .map(QuestDefinition::id)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    String nextQuestId(String group) {
        return QuestNaming.nextQuestId(group, definitionStore.questIds());
    }

    String nextQuestId(String group, Set<String> reservedIds) {
        Set<String> reserved = new HashSet<>(definitionStore.questIds());
        if (reservedIds != null) {
            reserved.addAll(reservedIds);
        }
        return QuestNaming.nextQuestId(group, reserved);
    }

    void ensureGroupExists(String rawGroup) {
        String group = EditorSessionNames.normalizeGroup(rawGroup);
        if (group.isBlank()) {
            return;
        }
        List<String> groups = new ArrayList<>(definitionStore.groupOrder());
        if (groups.contains(group)) {
            return;
        }
        groups.add(group);
        definitionStore.setGroupOrder(groups);
    }
}
