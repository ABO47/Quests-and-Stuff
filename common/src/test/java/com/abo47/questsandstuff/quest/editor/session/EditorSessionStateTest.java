package com.abo47.questsandstuff.quest.editor.session;

import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorSessionStateTest {
    @TempDir
    Path root;

    @Test
    void createSessionUsesFirstGroupAndFirstQuestInThatGroup() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);

            EditorSessionService.EditorSession session = state.createSession();

            assertEquals("main", session.currentChapter);
            assertEquals("alpha", session.currentQuest);
        } finally {
            store.shutdown();
        }
    }

    @Test
    void normalizeQuestSelectionKeepsValidQuestAndReplacesMissingQuest() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);
            EditorSessionService.EditorSession session = new EditorSessionService.EditorSession();
            session.currentChapter = "side";
            session.currentQuest = "zeta";

            state.normalizeQuestSelection(session);
            assertEquals("sideQuest", session.currentQuest);

            session.currentQuest = "sideQuest";
            state.normalizeQuestSelection(session);
            assertEquals("sideQuest", session.currentQuest);

            session.currentChapter = "missing";
            state.normalizeQuestSelection(session);
            assertEquals("-", session.currentQuest);
        } finally {
            store.shutdown();
        }
    }

    @Test
    void listsGroupsInOrderAndQuestIdsInGroupSortedById() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);

            assertEquals(List.of("main", "side"), state.chapters());
            assertEquals(List.of("alpha", "main_0"), state.questIdsInChapter("main"));
            assertEquals(List.of("sideQuest"), state.questIdsInChapter("side"));
        } finally {
            store.shutdown();
        }
    }

    @Test
    void ensureChapterExistsIgnoresBlankAndAppendsNewGroup() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);

            state.ensureChapterExists("  ");
            assertEquals(List.of("main", "side"), store.chapterOrder());

            state.ensureChapterExists(" extra ");
            assertEquals(List.of("main", "side", "extra"), store.chapterOrder());
        } finally {
            store.shutdown();
        }
    }

    @Test
    void nextQuestIdUsesStoreIdsAndCallerReservations() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);

            assertEquals("quest_0001_main", state.nextQuestId("main"));
            assertEquals("quest_0002_main", state.nextQuestId("main", Set.of("quest_0001_main")));
        } finally {
            store.shutdown();
        }
    }

    private QuestDefinitionStore storeWithQuests() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        store.upsert(quest("alpha", "main"));
        store.upsert(quest("main_0", "main"));
        store.upsert(quest("sideQuest", "side"));
        store.setChapterOrder(List.of("main", "side"));
        return store;
    }

    private static QuestDefinition quest(String id, String chapter) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                QuestDisplay.forNewQuest(id, Map.of(chapter, ChapterDef.DEFAULT)),
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }
}
