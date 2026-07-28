package com.abo47.questsandstuff.quest.model.connection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestConnectionMetadataTest {
    @Test
    void edgeKeysAndMetadataKeysUseTheSameNormalization() {
        String connectionKey = QuestConnectionMetadata.connectionKey(" \\quest//source ", " /quest/target ");

        assertEquals("quest/source->quest/target", connectionKey);
        assertEquals("quest/source", QuestConnectionMetadata.sourceQuestId(connectionKey));
        assertEquals("quest/target", QuestConnectionMetadata.targetQuestId(connectionKey));
        assertEquals("quest/source", QuestConnectionMetadata.metadataKey(" \\quest//source "));
        assertTrue(QuestConnectionMetadata.isValidConnectionKey(connectionKey));
        assertFalse(QuestConnectionMetadata.isValidConnectionKey("quest/source-> "));
    }

    @Test
    void modeSerializationStoresOnlyGridOverrides() {
        assertEquals(QuestConnectionMode.DIRECT, QuestConnectionMode.fromSerializedName(null));
        assertEquals(QuestConnectionMode.DIRECT, QuestConnectionMode.fromSerializedName("direct"));
        assertEquals(QuestConnectionMode.GRID, QuestConnectionMode.fromSerializedName(" grid "));
        assertFalse(QuestConnectionMode.DIRECT.storedInQuestMetadata());
        assertTrue(QuestConnectionMode.GRID.storedInQuestMetadata());
    }

    @Test
    void metadataRecordNormalizesIdsAndExposesModeFlags() {
        QuestConnectionMetadata metadata = QuestConnectionMetadata.grid(" quest/source ", " quest/target ", 0x112233, true);

        assertEquals("quest/source", metadata.sourceQuestId());
        assertEquals("quest/target", metadata.targetQuestId());
        assertEquals("quest/source->quest/target", metadata.connectionKey());
        assertEquals("quest/source", metadata.metadataKey());
        assertEquals(0x112233, metadata.color());
        assertTrue(metadata.grid());
        assertFalse(metadata.direct());
        assertTrue(metadata.hidden());
    }
}
