package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EditorCommandDispatcherTest {
    @Test
    void everyConcreteEditorCommandHasDescriptor() {
        for (EditorCommandType type : EditorCommandType.values()) {
            if (type == EditorCommandType.UNKNOWN) {
                assertNull(EditorCommandDispatcher.descriptor(type));
                continue;
            }
            EditorCommandDescriptor descriptor = EditorCommandDispatcher.descriptor(type);
            assertNotNull(descriptor, type.name());
            assertEquals(type, descriptor.type(), type.name());
        }

        assertFalse(EditorCommandDispatcher.registeredTypes().contains(EditorCommandType.UNKNOWN));
        assertEquals(EditorCommandType.values().length - 1, EditorCommandDispatcher.registeredTypes().size());
    }

    @Test
    void descriptorsAreGroupedByCommandFamily() {
        assertEquals(EditorCommandFamily.CANVAS, family(EditorCommandType.MOVE_MANY));
        assertEquals(EditorCommandFamily.CLIPBOARD, family(EditorCommandType.PASTE_BLUEPRINT));
        assertEquals(EditorCommandFamily.PREREQUISITE, family(EditorCommandType.CONNECTION_COLOR));
        assertEquals(EditorCommandFamily.QUEST, family(EditorCommandType.QUEST_BACKGROUND_MANY));
        assertEquals(EditorCommandFamily.DESCRIPTION, family(EditorCommandType.DESCRIPTION_PUT));
        assertEquals(EditorCommandFamily.OBJECTIVE, family(EditorCommandType.REWARD_MOVE));
        assertEquals(EditorCommandFamily.CANVAS_LAYER, family(EditorCommandType.CANVAS_LAYER_ORDER));
    }

    @Test
    void familyCountsStayIntentional() {
        Map<EditorCommandFamily, Integer> counts = new EnumMap<>(EditorCommandFamily.class);
        for (EditorCommandType type : EditorCommandDispatcher.registeredTypes()) {
            counts.merge(family(type), 1, Integer::sum);
        }

        assertEquals(2, counts.get(EditorCommandFamily.CANVAS));
        assertEquals(3, counts.get(EditorCommandFamily.CLIPBOARD));
        assertEquals(5, counts.get(EditorCommandFamily.PREREQUISITE));
        assertEquals(12, counts.get(EditorCommandFamily.QUEST));
        assertEquals(1, counts.get(EditorCommandFamily.DESCRIPTION));
        assertEquals(6, counts.get(EditorCommandFamily.OBJECTIVE));
        assertEquals(5, counts.get(EditorCommandFamily.CANVAS_LAYER));
    }

    private static EditorCommandFamily family(EditorCommandType type) {
        EditorCommandDescriptor descriptor = EditorCommandDispatcher.descriptor(type);
        assertNotNull(descriptor, type.name());
        return descriptor.family();
    }
}
