package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModalTargetStateTest {
    @Test
    void parsedTargetPrefersCapturedSessionTargetOverStaleField() {
        TabletUiState state = new TabletUiState();
        ModalOpenActions.openRecipePicker(state, ModalTargets.taskRecipe("quest", "task", "recipe"));
        state.questDetails.questDetailsPickTarget = ModalTargets.taskBlock("stale", "task", "block");

        ModalTargetParser.Target target = ModalTargetState.parsedTarget(
                state,
                ModalSession.TargetSlot.QUEST_DETAILS_PICK,
                state.questDetails.questDetailsPickTarget
        );

        assertTrue(target.isTaskRecipe());
        assertEquals("quest", target.questId());
        assertEquals("task", target.entryId());
    }

    @Test
    void targetFallsBackToLegacyFieldWhenNoSessionIsActive() {
        TabletUiState state = new TabletUiState();

        ModalTargetParser.Target target = ModalTargetState.parsedTarget(
                state,
                ModalSession.TargetSlot.QUEST_DETAILS_PICK,
                ModalTargets.taskBlock("quest", "task", "block")
        );

        assertTrue(target.isTaskBlock());
        assertEquals("quest", target.questId());
    }

    @Test
    void targetSetPrefersCapturedSessionSetAndCleansFallbacks() {
        TabletUiState state = new TabletUiState();
        ModalOpenActions.openBatchQuestBackgroundPicker(state, List.of("one", "two"), "background", false);
        state.modal.modalQuestBackgroundTargets.clear();
        state.modal.modalQuestBackgroundTargets.add("stale");

        Set<String> sessionTargets = ModalTargetState.targetSet(
                state,
                ModalSession.TargetSetSlot.QUEST_BACKGROUND,
                state.modal.modalQuestBackgroundTargets
        );

        assertEquals(Set.of("one", "two"), sessionTargets);

        ModalCloseActions.closeAllImmediately(state);
        Set<String> fallbackTargets = ModalTargetState.targetSet(
                state,
                ModalSession.TargetSetSlot.QUEST_BACKGROUND,
                Set.of(" fallback ", "")
        );

        assertEquals(Set.of("fallback"), fallbackTargets);
    }

    @Test
    void requirePartsRejectsMalformedTargetsWithoutMutatingThem() {
        ModalTargetParser.Target target = ModalTargetParser.parse("task_recipe|quest");

        assertFalse(ModalTargetState.requireParts("test_recipe", target, 4));
        assertEquals("task_recipe|quest", target.raw());
    }
}
