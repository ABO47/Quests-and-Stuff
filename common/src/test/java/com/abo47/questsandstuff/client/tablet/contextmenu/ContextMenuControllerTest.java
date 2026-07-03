package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextMenuControllerTest {
    @Test
    void openCanvasResetsSessionAndTargets() {
        TabletUiState state = dirtyState();

        ContextMenuController.openCanvas(state, 10, 12, 100, 120, 98, 118);

        assertTrue(state.contextMenu.contextMenuOpen);
        assertEquals(10, state.contextMenu.contextMenuX);
        assertEquals(12, state.contextMenu.contextMenuY);
        assertEquals(10, state.contextMenu.contextMenuAnchorX);
        assertEquals(12, state.contextMenu.contextMenuAnchorY);
        assertEquals(100, state.contextMenu.contextLogicalX);
        assertEquals(120, state.contextMenu.contextLogicalY);
        assertEquals(98, state.contextMenu.contextPointerLogicalX);
        assertEquals(118, state.contextMenu.contextPointerLogicalY);
        assertEquals(0, state.contextMenu.contextMenuRows);
        assertEquals(0, state.contextMenu.contextMenuScroll);
        assertEquals(0, state.contextMenu.contextMenuScrollMax);
        assertFalse(state.contextMenu.contextMenuScrollDragging);
        assertFalse(state.contextMenu.contextQuestCompletionSoundMenuOpen);
        assertEquals("", state.contextMenu.contextDeleteConfirmKey);
        assertEquals("", state.contextMenu.contextQuestId);
        assertEquals("", state.contextMenu.contextConnectionSource);
        assertEquals("", state.contextMenu.contextConnectionTarget);
        assertEquals("", state.contextMenu.contextCanvasImageId);
        assertEquals("", state.contextMenu.contextCanvasTextId);
    }

    @Test
    void targetHelpersKeepOnlyOneCanvasTarget() {
        TabletUiState state = new TabletUiState();

        ContextMenuController.targetQuest(state, "quest_a");
        ContextMenuController.targetImage(state, "image_a");
        assertEquals(ContextMenuTarget.IMAGE, state.contextMenu.contextMenuTarget);
        assertEquals("", state.contextMenu.contextQuestId);
        assertEquals("image_a", state.contextMenu.contextCanvasImageId);

        ContextMenuController.targetConnection(state, "source", "target");
        assertEquals(ContextMenuTarget.CONNECTION, state.contextMenu.contextMenuTarget);
        assertEquals("", state.contextMenu.contextCanvasImageId);
        assertEquals("source", state.contextMenu.contextConnectionSource);
        assertEquals("target", state.contextMenu.contextConnectionTarget);
    }

    @Test
    void closeClearsRuntimeMenuState() {
        TabletUiState state = dirtyState();

        ContextMenuController.close(state);

        assertFalse(state.contextMenu.contextMenuOpen);
        assertEquals(0, state.contextMenu.contextMenuRows);
        assertEquals(0, state.contextMenu.contextMenuScroll);
        assertEquals(0, state.contextMenu.contextMenuScrollMax);
        assertFalse(state.contextMenu.contextMenuScrollDragging);
        assertEquals(0, state.contextMenu.contextMenuWidthPx);
        assertEquals(0, state.contextMenu.contextMenuHeightPx);
        assertEquals("", state.contextMenu.contextDeleteConfirmKey);
        assertFalse(state.contextMenu.contextQuestCompletionSoundMenuOpen);
    }

    @Test
    void layoutClampsScrollAndStopsDraggingWhenNoScrollRange() {
        TabletUiState state = new TabletUiState();
        state.contextMenu.contextMenuScroll = 8;
        state.contextMenu.contextMenuScrollDragging = true;

        ContextMenuController.setLayout(state, 4, 5, 90, 80, 3, 2);
        assertEquals(2, state.contextMenu.contextMenuScroll);
        assertTrue(state.contextMenu.contextMenuScrollDragging);

        ContextMenuController.setLayout(state, 4, 5, 90, 80, 3, 0);
        assertEquals(0, state.contextMenu.contextMenuScroll);
        assertFalse(state.contextMenu.contextMenuScrollDragging);
    }

    @Test
    void deleteConfirmArmsThenConfirms() {
        TabletUiState state = new TabletUiState();

        assertFalse(ContextMenuController.confirmDeleteClick(state, "quest"));
        assertEquals("quest", state.contextMenu.contextDeleteConfirmKey);
        assertEquals("Sure?", ContextMenuController.pendingDeleteLabel(state, "quest", "Delete"));

        assertTrue(ContextMenuController.confirmDeleteClick(state, "quest"));
        assertEquals("", state.contextMenu.contextDeleteConfirmKey);
    }

    private static TabletUiState dirtyState() {
        TabletUiState state = new TabletUiState();
        state.contextMenu.contextMenuOpen = true;
        state.contextMenu.contextMenuRows = 5;
        state.contextMenu.contextMenuScroll = 3;
        state.contextMenu.contextMenuScrollMax = 4;
        state.contextMenu.contextMenuScrollDragging = true;
        state.contextMenu.contextMenuWidthPx = 120;
        state.contextMenu.contextMenuHeightPx = 160;
        state.contextMenu.contextDeleteConfirmKey = "delete";
        state.contextMenu.contextQuestCompletionSoundMenuOpen = true;
        state.contextMenu.contextQuestId = "quest";
        state.contextMenu.contextConnectionSource = "source";
        state.contextMenu.contextConnectionTarget = "target";
        state.contextMenu.contextCanvasImageId = "image";
        state.contextMenu.contextCanvasTextId = "text";
        return state;
    }
}
