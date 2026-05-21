package com.abo47.questsandstuff.quest.editor.quest;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestDefinitionEditsTest {
    @Test
    void withoutPrerequisitePrunesConnectionMetadata() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "child",
                QuestDisplay.DEFAULT,
                QuestSettings.DEFAULT,
                Set.of("deleted_parent", "kept_parent"),
                Map.of("deleted_parent", 0xFF00FF, "kept_parent", 0x00FF00),
                Map.of("deleted_parent", "grid", "kept_parent", "grid"),
                Set.of("deleted_parent", "kept_parent"),
                Map.of(),
                Map.of()
        );

        QuestDefinition edited = QuestDefinitionEdits.withoutPrerequisite(definition, "deleted_parent");

        assertEquals(Set.of("kept_parent"), edited.prerequisites());
        assertFalse(edited.connectionColors().containsKey("deleted_parent"));
        assertFalse(edited.connectionModes().containsKey("deleted_parent"));
        assertFalse(edited.hiddenConnections().contains("deleted_parent"));
        assertEquals(0x00FF00, edited.connectionColors().get("kept_parent"));
        assertEquals("grid", edited.connectionModes().get("kept_parent"));
        assertEquals(Set.of("kept_parent"), edited.hiddenConnections());
    }

    @Test
    void withoutPrerequisiteLeavesUnrelatedDefinitionAlone() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "child",
                QuestDisplay.DEFAULT,
                QuestSettings.DEFAULT,
                Set.of("kept_parent"),
                Map.of(),
                Map.of(),
                Set.of(),
                Map.of(),
                Map.of()
        );

        assertSame(definition, QuestDefinitionEdits.withoutPrerequisite(definition, "missing_parent"));
    }
}
