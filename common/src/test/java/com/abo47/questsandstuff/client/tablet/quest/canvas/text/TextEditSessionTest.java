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
        assertEquals("", state.questDetailsTextEditTarget);
        assertEquals("", state.questDetailsTextEditDraft);
        assertEquals("canvas:text", state.canvasTextEditTarget);
        assertEquals("Canvas", state.canvasTextEditDraft);
        assertEquals(6, state.canvasTextEditCursor);
        assertEquals(6, state.canvasTextSelectionAnchor);
        assertTrue(state.canvasTextMenuOpen);
        assertEquals("canvas:text", state.canvasTextMenuTarget);
    }

    @Test
    void questDetailsBeginUsesSharedDraftCursorAndClosesCanvasStyleOwner() {
        TabletUiState state = new TabletUiState();
        TextStyleSession.openMainCanvas(state, "canvas:text");

        TextEditSession.beginQuestDetails(state, "desc:text", "Description");

        assertTrue(TextEditSession.isQuestDetailsEditing(state));
        assertFalse(TextEditSession.isMainCanvasEditing(state));
        assertEquals("desc:text", state.questDetailsTextEditTarget);
        assertEquals("desc:text", state.canvasTextEditTarget);
        assertEquals("Description", state.questDetailsTextEditDraft);
        assertEquals("Description", state.canvasTextEditDraft);
        assertEquals(11, state.canvasTextEditCursor);
        assertEquals(11, state.canvasTextSelectionAnchor);
        assertFalse(state.canvasTextMenuOpen);
        assertEquals("", state.canvasTextMenuTarget);
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
        assertEquals("abXYf", state.canvasTextEditDraft);
        assertEquals("abXYf", state.questDetailsTextEditDraft);
        assertEquals(4, state.canvasTextEditCursor);
        assertEquals(4, state.canvasTextSelectionAnchor);
        assertFalse(TextEditSession.hasSelection(state));
    }

    @Test
    void insertCapsDraftAtSharedMaximum() {
        TabletUiState state = new TabletUiState();
        TextEditSession.beginMainCanvas(state, "text", "a".repeat(TextEditSession.MAX_DRAFT_LENGTH - 1));

        TextEditSession.Replacement replacement = TextEditSession.insert(state, "bcdef");

        assertEquals("b", replacement.value());
        assertEquals(TextEditSession.MAX_DRAFT_LENGTH, state.canvasTextEditDraft.length());
    }

    @Test
    void closeQuestDetailsClearsStaleQuestFieldsEvenWhenCommonOpenFlagIsOff() {
        TabletUiState state = new TabletUiState();
        state.canvasTextEditOpen = false;
        state.questDetailsTextEditTarget = "stale";
        state.questDetailsTextEditDraft = "draft";

        TextEditSession.closeQuestDetails(state, true);

        assertEquals("", state.questDetailsTextEditTarget);
        assertEquals("", state.questDetailsTextEditDraft);
    }
}
