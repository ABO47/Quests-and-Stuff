package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestObjectiveJsonsTest {
    @Test
    void readResultReportsMalformedJsonWithoutThrowing() {
        QuestObjectiveJsons.ParseResult result = QuestObjectiveJsons.readResult("{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultRejectsNonObjectJson() {
        QuestObjectiveJsons.ParseResult result = QuestObjectiveJsons.readResult("\"string\"");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertEquals("expected JSON object", result.diagnostic());
    }

    @Test
    void readForEditKeepsSafeFallbackForMalformedTaskJson() {
        QuestObjectiveJsons.ParseResult result = QuestObjectiveJsons.readForEdit("quest_a", "task_a", true, "{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultAcceptsValidObjectiveObjects() {
        QuestObjectiveJsons.ParseResult result = QuestObjectiveJsons.readResult("{\"id\":\"task_a\",\"type\":\"questsandstuff:check\"}");

        assertTrue(result.valid());
        assertEquals("task_a", QuestObjectiveJsons.asString(result.value(), "id", ""));
        assertEquals("questsandstuff:check", QuestObjectiveJsons.asString(result.value(), "type", ""));
        assertEquals("", result.diagnostic());
    }
}
