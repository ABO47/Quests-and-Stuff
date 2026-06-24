package com.abo47.questsandstuff.quest.model.connection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestConnectionMetadataTest {
    @Test
    void edgeKeysAndMetadataKeysUseTheSameNormalization() {
        String edgeKey = QuestConnectionMetadata.edgeKey(" \\quest//source ", " /quest/target ");

        assertEquals("quest/source->quest/target", edgeKey);
        assertEquals("quest/source", QuestConnectionMetadata.sourceQuestId(edgeKey));
        assertEquals("quest/target", QuestConnectionMetadata.targetQuestId(edgeKey));
        assertEquals("quest/source", QuestConnectionMetadata.metadataKey(" \\quest//source "));
        assertTrue(QuestConnectionMetadata.isValidEdgeKey(edgeKey));
        assertFalse(QuestConnectionMetadata.isValidEdgeKey("quest/source-> "));
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
        assertEquals("quest/source->quest/target", metadata.edgeKey());
        assertEquals("quest/source", metadata.metadataKey());
        assertEquals(0x112233, metadata.color());
        assertTrue(metadata.grid());
        assertFalse(metadata.direct());
        assertTrue(metadata.hidden());
    }
}
