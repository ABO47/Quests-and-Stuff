package com.abo47.questsandstuff.client.tablet.quest.details.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskJsonFactoryTest {
    @Test
    void readResultReportsMalformedJsonWithoutThrowing() {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult("{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultRejectsNonObjectJson() {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult("\"string\"");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertEquals("expected JSON object", result.diagnostic());
    }

    @Test
    void readForEditKeepsSafeFallbackForMalformedTaskJson() {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readForEdit("quest_a", "task_a", true, "{bad json");

        assertFalse(result.valid());
        assertTrue(result.value().entrySet().isEmpty());
        assertFalse(result.diagnostic().isBlank());
    }

    @Test
    void readResultAcceptsValidTaskObjects() {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult("{\"id\":\"task_a\",\"type\":\"questsandstuff:check\"}");

        assertTrue(result.valid());
        assertEquals("task_a", TaskJsonFactory.asString(result.value(), "id", ""));
        assertEquals("questsandstuff:check", TaskJsonFactory.asString(result.value(), "type", ""));
        assertEquals("", result.diagnostic());
    }

    @Test
    void fieldAccessorsFallbackForWrongJsonTypes() {
        TaskJsonFactory.ParseResult result = TaskJsonFactory.readResult("{\"id\":{\"nested\":true},\"manual\":{\"nested\":true}}");

        assertTrue(result.valid());
        assertEquals("fallback", TaskJsonFactory.asString(result.value(), "id", "fallback"));
        assertTrue(TaskJsonFactory.asBoolean(result.value(), "manual", true));
    }
}
