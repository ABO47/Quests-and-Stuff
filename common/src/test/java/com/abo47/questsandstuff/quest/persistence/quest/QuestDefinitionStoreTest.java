package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.quest.model.GroupDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDefinitionStoreTest {
    @TempDir
    Path root;

    @Test
    void upsertNormalizesQuestMutationPayload() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            store.upsert(quest("quest/a", "chapter_one"));
            store.upsert(connectedQuest("quest/b", "chapter_one"));

            QuestDefinition actual = store.quest("quest/b");

            assertEquals(Set.of("quest/a"), actual.prerequisites());
            assertEquals(Map.of("quest/a", 0x33AAFF), actual.connectionColors());
            assertEquals(Map.of("quest/a", "grid"), actual.connectionModes());
            assertEquals(Set.of("quest/a"), actual.hiddenConnections());
        } finally {
            store.shutdown();
        }
    }

    @Test
    void replaceAllRemovesOldQuestsAndSkipsGrouplessReplacements() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        try {
            store.upsert(quest("quest/old", "old_chapter"));

            store.replaceAll(Map.of(
                    "quest/new", quest("quest/new", "new_chapter"),
                    "quest/groupless", grouplessQuest("quest/groupless")
            ));

            assertFalse(store.containsQuest("quest/old"));
            assertTrue(store.containsQuest("quest/new"));
            assertFalse(store.containsQuest("quest/groupless"));
        } finally {
            store.shutdown();
        }
    }

    private static QuestDefinition connectedQuest(String id, String group) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                display(id, group),
                QuestSettings.DEFAULT,
                Set.of("quest/a", "quest/b", "quest/missing"),
                Map.of("quest/a", 0x33AAFF, "quest/missing", 0xFFAA33),
                Map.of("quest/a", "grid", "quest/missing", "grid"),
                Set.of("quest/a", "quest/missing"),
                Map.of(),
                Map.of()
        );
    }

    private static QuestDefinition quest(String id, String group) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                display(id, group),
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }

    private static QuestDefinition grouplessQuest(String id) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                QuestDisplay.DEFAULT,
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }

    private static QuestDisplay display(String title, String group) {
        return QuestDisplay.forNewQuest(title, Map.of(group, GroupDef.DEFAULT));
    }
}
