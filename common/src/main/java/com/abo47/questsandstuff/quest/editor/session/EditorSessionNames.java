package com.abo47.questsandstuff.quest.editor.session;

final class EditorSessionNames {
    private EditorSessionNames() {
    }

    static String normalizeGroup(String groupName) {
        return groupName == null ? "" : groupName.trim();
    }

    static String normalizeQuestId(String questId) {
        return questId == null ? "" : questId.trim();
    }
}
