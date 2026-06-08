package com.abo47.questsandstuff.quest.editor.session;

import com.abo47.questsandstuff.util.QuestIdentity;

final class EditorSessionNames {
    private EditorSessionNames() {
    }

    static String normalizeGroup(String groupName) {
        return QuestIdentity.groupName(groupName);
    }

    static String normalizeQuestId(String questId) {
        return QuestIdentity.questId(questId);
    }
}
