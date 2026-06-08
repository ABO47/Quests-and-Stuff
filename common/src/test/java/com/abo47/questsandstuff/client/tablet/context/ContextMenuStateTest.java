package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextMenuStateTest {
    @Test
    void openCanvasResetsSessionAndTargets() {
        TabletUiState state = dirtyState();

        ContextMenuState.openCanvas(state, 10, 12, 100, 120, 98, 118);

        assertTrue(state.contextMenuOpen);
        assertEquals(10, state.contextMenuX);
        assertEquals(12, state.contextMenuY);
        assertEquals(10, state.contextMenuAnchorX);
        assertEquals(12, state.contextMenuAnchorY);
        assertEquals(100, state.contextLogicalX);
        assertEquals(120, state.contextLogicalY);
        assertEquals(98, state.contextPointerLogicalX);
        assertEquals(118, state.contextPointerLogicalY);
        assertEquals(0, state.contextMenuRows);
        assertEquals(0, state.contextMenuScroll);
        assertEquals(0, state.contextMenuScrollMax);
        assertFalse(state.contextMenuScrollDragging);
        assertFalse(state.contextQuestCompletionSoundMenuOpen);
        assertEquals("", state.contextDeleteConfirmKey);
        assertEquals("", state.contextQuestId);
        assertEquals("", state.contextEdgeSource);
        assertEquals("", state.contextEdgeTarget);
        assertEquals("", state.contextCanvasImageId);
        assertEquals("", state.contextCanvasTextId);
    }

    @Test
    void targetHelpersKeepOnlyOneCanvasTarget() {
        TabletUiState state = new TabletUiState();

        ContextMenuState.targetQuest(state, "quest_a");
        ContextMenuState.targetImage(state, "image_a");
        assertEquals(ContextMenuTarget.IMAGE, state.contextMenuTarget);
        assertEquals("", state.contextQuestId);
        assertEquals("image_a", state.contextCanvasImageId);

        ContextMenuState.targetEdge(state, "source", "target");
        assertEquals(ContextMenuTarget.EDGE, state.contextMenuTarget);
        assertEquals("", state.contextCanvasImageId);
        assertEquals("source", state.contextEdgeSource);
        assertEquals("target", state.contextEdgeTarget);
    }

    @Test
    void closeClearsRuntimeMenuState() {
        TabletUiState state = dirtyState();

        ContextMenuState.close(state);

        assertFalse(state.contextMenuOpen);
        assertEquals(0, state.contextMenuRows);
        assertEquals(0, state.contextMenuScroll);
        assertEquals(0, state.contextMenuScrollMax);
        assertFalse(state.contextMenuScrollDragging);
        assertEquals(0, state.contextMenuWidthPx);
        assertEquals(0, state.contextMenuHeightPx);
        assertEquals("", state.contextDeleteConfirmKey);
        assertFalse(state.contextQuestCompletionSoundMenuOpen);
    }

    @Test
    void layoutClampsScrollAndStopsDraggingWhenNoScrollRange() {
        TabletUiState state = new TabletUiState();
        state.contextMenuScroll = 8;
        state.contextMenuScrollDragging = true;

        ContextMenuState.setLayout(state, 4, 5, 90, 80, 3, 2);
        assertEquals(2, state.contextMenuScroll);
        assertTrue(state.contextMenuScrollDragging);

        ContextMenuState.setLayout(state, 4, 5, 90, 80, 3, 0);
        assertEquals(0, state.contextMenuScroll);
        assertFalse(state.contextMenuScrollDragging);
    }

    @Test
    void deleteConfirmArmsThenConfirms() {
        TabletUiState state = new TabletUiState();

        assertFalse(ContextMenuState.confirmDeleteClick(state, "quest"));
        assertEquals("quest", state.contextDeleteConfirmKey);
        assertEquals("Sure?", ContextMenuState.pendingDeleteLabel(state, "quest", "Delete"));

        assertTrue(ContextMenuState.confirmDeleteClick(state, "quest"));
        assertEquals("", state.contextDeleteConfirmKey);
    }

    private static TabletUiState dirtyState() {
        TabletUiState state = new TabletUiState();
        state.contextMenuOpen = true;
        state.contextMenuRows = 5;
        state.contextMenuScroll = 3;
        state.contextMenuScrollMax = 4;
        state.contextMenuScrollDragging = true;
        state.contextMenuWidthPx = 120;
        state.contextMenuHeightPx = 160;
        state.contextDeleteConfirmKey = "delete";
        state.contextQuestCompletionSoundMenuOpen = true;
        state.contextQuestId = "quest";
        state.contextEdgeSource = "source";
        state.contextEdgeTarget = "target";
        state.contextCanvasImageId = "image";
        state.contextCanvasTextId = "text";
        return state;
    }
}
