package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextStyleSessionTest {
    @Test
    void mainCanvasStyleCloseDoesNotTouchQuestDetailsStyleState() {
        TabletUiState state = new TabletUiState();
        TextStyleSession.openMainCanvas(state, "canvas:text");
        TextStyleSession.openQuestDetails(state, "desc:text");
        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.MAIN_CANVAS, "canvas:text");
        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.QUEST_DETAILS, "desc:text");

        TextStyleSession.closeMainCanvas(state);

        assertFalse(state.canvas.canvasTextMenuOpen);
        assertEquals("", state.canvas.canvasTextMenuTarget);
        assertEquals("", state.canvas.canvasTextFontSizeFieldTarget);
        assertTrue(state.questDetails.questDetailsTextStyleOpen);
        assertEquals("desc:text", state.questDetails.questDetailsTextStyleTarget);
        assertEquals("desc:text", state.questDetails.questDetailsTextFontSizeFieldTarget);
    }

    @Test
    void questDetailsStyleCloseResetsTargetFontFieldAndBounds() {
        TabletUiState state = new TabletUiState();
        TextStyleSession.openQuestDetails(state, "desc:text");
        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.QUEST_DETAILS, "desc:text");
        TextStyleSession.setQuestDetailsBounds(state, 1, 2, 3, 4);

        TextStyleSession.closeQuestDetails(state);

        assertFalse(state.questDetails.questDetailsTextStyleOpen);
        assertEquals("", state.questDetails.questDetailsTextStyleTarget);
        assertEquals("", state.questDetails.questDetailsTextFontSizeFieldTarget);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuX);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuY);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuW);
        assertEquals(0, state.questDetails.questDetailsTextStyleMenuH);
    }

    @Test
    void fontSizeFieldOpenAggregatesChapterCanvasAndQuestDetailsFields() {
        TabletUiState state = new TabletUiState();

        assertFalse(TextStyleSession.isAnyFontSizeFieldOpen(state));

        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.MAIN_CANVAS, "canvas:text");
        assertTrue(TextStyleSession.isAnyFontSizeFieldOpen(state));
        assertEquals("canvas:text", TextStyleSession.fontSizeTarget(state, TextStyleSession.Surface.MAIN_CANVAS));

        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.MAIN_CANVAS, "");
        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.QUEST_DETAILS, "desc:text");
        assertTrue(TextStyleSession.isAnyFontSizeFieldOpen(state));
        assertEquals("desc:text", TextStyleSession.fontSizeTarget(state, TextStyleSession.Surface.QUEST_DETAILS));
    }

    @Test
    void recentQuestDetailsInteractionUsesProvidedClockWindow() {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsTextStyleInteractionAtMs = 1_000L;

        assertTrue(TextStyleSession.recentlyHandledQuestDetailsClick(state, 1_200L, 350L));
        assertFalse(TextStyleSession.recentlyHandledQuestDetailsClick(state, 1_500L, 350L));
    }
}
