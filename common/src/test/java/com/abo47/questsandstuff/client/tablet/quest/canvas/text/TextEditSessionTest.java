package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditSessionTest {
    @Test
    void mainCanvasBeginClosesQuestDetailsDraftAndOpensCanvasStyleOwner() {
        TabletUiState state = new TabletUiState();
        TextEditSession.beginQuestDetails(state, "desc:text", "Description");

        TextEditSession.beginMainCanvas(state, "canvas:text", "Canvas");

        assertTrue(TextEditSession.isMainCanvasEditing(state));
        assertFalse(TextEditSession.isQuestDetailsEditing(state));
        assertEquals("", state.questDetails.questDetailsTextEditTarget);
        assertEquals("", state.questDetails.questDetailsTextEditDraft);
        assertEquals("canvas:text", state.canvas.canvasTextEditTarget);
        assertEquals("Canvas", state.canvas.canvasTextEditDraft);
        assertEquals(6, state.canvas.canvasTextEditCursor);
        assertEquals(6, state.canvas.canvasTextSelectionAnchor);
        assertTrue(state.canvas.canvasTextMenuOpen);
        assertEquals("canvas:text", state.canvas.canvasTextMenuTarget);
    }

    @Test
    void questDetailsBeginUsesSharedDraftCursorAndClosesCanvasStyleOwner() {
        TabletUiState state = new TabletUiState();
        TextStyleSession.openMainCanvas(state, "canvas:text");

        TextEditSession.beginQuestDetails(state, "desc:text", "Description");

        assertTrue(TextEditSession.isQuestDetailsEditing(state));
        assertFalse(TextEditSession.isMainCanvasEditing(state));
        assertEquals("desc:text", state.questDetails.questDetailsTextEditTarget);
        assertEquals("desc:text", state.canvas.canvasTextEditTarget);
        assertEquals("Description", state.questDetails.questDetailsTextEditDraft);
        assertEquals("Description", state.canvas.canvasTextEditDraft);
        assertEquals(11, state.canvas.canvasTextEditCursor);
        assertEquals(11, state.canvas.canvasTextSelectionAnchor);
        assertFalse(state.canvas.canvasTextMenuOpen);
        assertEquals("", state.canvas.canvasTextMenuTarget);
    }

    @Test
    void replacementNormalizesSelectionAndKeepsQuestDetailsDraftInSync() {
        TabletUiState state = new TabletUiState();
        TextEditSession.beginQuestDetails(state, "desc:text", "abcdef");
        TextEditSession.moveCursor(state, 2, false);
        TextEditSession.moveCursor(state, 5, true);

        TextEditSession.Replacement replacement = TextEditSession.insert(state, "XY");

        assertEquals(2, replacement.start());
        assertEquals(5, replacement.end());
        assertEquals("XY", replacement.value());
        assertEquals("abXYf", state.canvas.canvasTextEditDraft);
        assertEquals("abXYf", state.questDetails.questDetailsTextEditDraft);
        assertEquals(4, state.canvas.canvasTextEditCursor);
        assertEquals(4, state.canvas.canvasTextSelectionAnchor);
        assertFalse(TextEditSession.hasSelection(state));
    }

    @Test
    void insertCapsDraftAtSharedMaximum() {
        TabletUiState state = new TabletUiState();
        TextEditSession.beginMainCanvas(state, "text", "a".repeat(TextEditSession.MAX_DRAFT_LENGTH - 1));

        TextEditSession.Replacement replacement = TextEditSession.insert(state, "bcdef");

        assertEquals("b", replacement.value());
        assertEquals(TextEditSession.MAX_DRAFT_LENGTH, state.canvas.canvasTextEditDraft.length());
    }

    @Test
    void closeQuestDetailsClearsStaleQuestFieldsEvenWhenCommonOpenFlagIsOff() {
        TabletUiState state = new TabletUiState();
        state.canvas.canvasTextEditOpen = false;
        state.questDetails.questDetailsTextEditTarget = "stale";
        state.questDetails.questDetailsTextEditDraft = "draft";

        TextEditSession.closeQuestDetails(state, true);

        assertEquals("", state.questDetails.questDetailsTextEditTarget);
        assertEquals("", state.questDetails.questDetailsTextEditDraft);
    }
}
