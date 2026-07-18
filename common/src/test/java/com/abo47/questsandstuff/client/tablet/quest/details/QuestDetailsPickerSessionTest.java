package com.abo47.questsandstuff.client.tablet.quest.details;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsPickerSessionTest {
    @Test
    void typePickerUsesSingleTypedSession() {
        TabletUiState state = contextAt(24, 36);

        QuestDetailsTransientManager.openTypePicker(state, "task_change", "task_a");

        QuestDetailsPickerSession session = state.questDetails.questDetailsPickerSession;
        assertTrue(session.typePicker());
        assertFalse(session.itemSourcePicker());
        assertFalse(session.xpPicker());
        assertEquals("task_change", session.kind());
        assertEquals("task_a", session.targetId());
        assertEquals(24, session.x());
        assertEquals(36, session.y());
    }

    @Test
    void itemSourceAndXpReplaceActivePickerSession() {
        TabletUiState state = contextAt(12, 18);

        QuestDetailsTransientManager.openTypePicker(state, "task", "");
        QuestDetailsTransientManager.openItemSourcePicker(state, "task_item|quest_a|task_a|questsandstuff:item");

        QuestDetailsPickerSession itemSource = state.questDetails.questDetailsPickerSession;
        assertTrue(itemSource.itemSourcePicker());
        assertFalse(itemSource.typePicker());
        assertEquals("task_item|quest_a|task_a|questsandstuff:item", itemSource.itemSourceTarget());
        assertEquals(12, itemSource.x());
        assertEquals(18, itemSource.y());

        QuestDetailsTransientManager.openTypePicker(state, "reward_change", "reward_a");
        QuestDetailsTransientManager.openXpPicker(state, "quest_a", "reward_a", false);

        QuestDetailsPickerSession xp = state.questDetails.questDetailsPickerSession;
        assertTrue(xp.xpPicker());
        assertFalse(xp.typePicker());
        assertFalse(xp.itemSourcePicker());
        assertEquals("quest_a", xp.xpQuestId());
        assertEquals("reward_a", xp.xpEntryId());
        assertFalse(xp.xpTask());
    }

    @Test
    void closeFloatingPopupsClearsActivePickerSessionOnce() {
        TabletUiState state = contextAt(8, 14);
        QuestDetailsTransientManager.openTypePicker(state, "reward", "");

        assertTrue(QuestDetailsTransientManager.closeFloatingPopups(state));
        assertEquals(QuestDetailsPickerSession.Type.NONE, state.questDetails.questDetailsPickerSession.type());
        assertFalse(state.questDetails.questDetailsPickerSession.active());
        assertFalse(QuestDetailsTransientManager.closeFloatingPopups(state));
    }

    @Test
    void oldPickerCompatibilityFieldsDoNotExist() {
        String[] oldFields = {
                "questDetailsTypePickerOpen",
                "questDetailsTypePickerKind",
                "questDetailsTypePickerTargetId",
                "questDetailsItemSourcePickerOpen",
                "questDetailsItemSourcePickerTarget",
                "questDetailsXpPickerOpen",
                "questDetailsXpPickerTask",
                "questDetailsXpPickerQuestId",
                "questDetailsXpPickerEntryId"
        };
        for (String field : oldFields) {
            assertThrows(NoSuchFieldException.class, () -> TabletUiState.class.getDeclaredField(field), field);
        }
    }

    private static TabletUiState contextAt(int x, int y) {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsContextX = x;
        state.questDetails.questDetailsContextY = y;
        return state;
    }
}
