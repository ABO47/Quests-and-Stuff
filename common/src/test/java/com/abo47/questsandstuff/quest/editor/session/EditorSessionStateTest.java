package com.abo47.questsandstuff.quest.editor.session;

import com.abo47.questsandstuff.quest.model.GroupDef;
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

            assertEquals("main", session.currentGroup);
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
            session.currentGroup = "side";
            session.currentQuest = "zeta";

            state.normalizeQuestSelection(session);
            assertEquals("sideQuest", session.currentQuest);

            session.currentQuest = "sideQuest";
            state.normalizeQuestSelection(session);
            assertEquals("sideQuest", session.currentQuest);

            session.currentGroup = "missing";
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

            assertEquals(List.of("main", "side"), state.groups());
            assertEquals(List.of("alpha", "main_0"), state.questIdsInGroup("main"));
            assertEquals(List.of("sideQuest"), state.questIdsInGroup("side"));
        } finally {
            store.shutdown();
        }
    }

    @Test
    void ensureGroupExistsIgnoresBlankAndAppendsNewGroup() {
        QuestDefinitionStore store = storeWithQuests();
        try {
            EditorSessionState state = new EditorSessionState(store);

            state.ensureGroupExists("  ");
            assertEquals(List.of("main", "side"), store.groupOrder());

            state.ensureGroupExists(" extra ");
            assertEquals(List.of("main", "side", "extra"), store.groupOrder());
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
        store.setGroupOrder(List.of("main", "side"));
        return store;
    }

    private static QuestDefinition quest(String id, String group) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                QuestDisplay.forNewQuest(id, Map.of(group, GroupDef.DEFAULT)),
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }
}
