package com.abo47.questsandstuff.quest.editor.session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorSession;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.util.naming.QuestNaming;

final class EditorSessionState {
    private final QuestDefinitionStore definitionStore;

    EditorSessionState(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    EditorSession createSession() {
        EditorSession session = new EditorSession();
        List<String> chapters = chapters();
        session.currentChapter = chapters.isEmpty() ? "" : chapters.get(0);
        normalizeQuestSelection(session);
        return session;
    }

    void normalizeQuestSelection(EditorSession session) {
        List<String> questIds = questIdsInChapter(session.currentChapter);
        if (questIds.isEmpty()) {
            session.currentQuest = "-";
            return;
        }
        if (!questIds.contains(session.currentQuest)) {
            session.currentQuest = questIds.get(0);
        }
    }

    List<String> chapters() {
        return new ArrayList<>(definitionStore.chapterOrder());
    }

    List<String> questIdsInChapter(String chapter) {
        return definitionStore.questDefinitions().stream()
                .filter(quest -> quest.display().chapters().containsKey(chapter))
                .map(QuestDefinition::id)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    String nextQuestId(String chapter) {
        return QuestNaming.nextQuestId(chapter, definitionStore.questIds());
    }

    String nextQuestId(String chapter, Set<String> reservedIds) {
        Set<String> reserved = new HashSet<>(definitionStore.questIds());
        if (reservedIds != null) {
            reserved.addAll(reservedIds);
        }
        return QuestNaming.nextQuestId(chapter, reserved);
    }

    void ensureChapterExists(String rawChapter) {
        String chapter = rawChapter.trim().replace('\\', '/').replaceAll("/{2,}", "/");
        if (chapter.isBlank()) {
            return;
        }
        List<String> chapters = new ArrayList<>(definitionStore.chapterOrder());
        if (chapters.contains(chapter)) {
            return;
        }
        chapters.add(chapter);
        definitionStore.setChapterOrder(chapters);
    }
}
