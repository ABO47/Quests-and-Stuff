package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionTransformSessionTest {
    @Test
    void beginUpdateAndClearDescriptionTransformUsesSharedSessionCleanup() {
        TabletUiState state = new TabletUiState();
        state.dragStartTextPositions.put("stale", new CanvasPoint(1, 2));
        state.questDetailsTransientTexts.put("stale", text("stale", 1, 2));
        state.snapGuideXVisible = true;
        state.snapGuideYVisible = true;

        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(text("text:a", 10, 20));
        QuestDetailsDescriptionTransform transform = new QuestDetailsDescriptionTransform(state, () -> 0, () -> 0, () -> 300, () -> 300);

        transform.beginTransform(
                model,
                "desc_text",
                "text:a",
                new QuestDetailsDescriptionTransform.ElementRect(10, 20, 80, 30, 0),
                false,
                false,
                false,
                10,
                20,
                20
        );
        CanvasRenderer.putTransientQuestDetailsText(state, model.text("text:a").moveTo(25, 45));

        CanvasTextLayer moved = state.questDetailsTransientTexts.get("text:a");
        assertEquals(25, moved.x());
        assertEquals(45, moved.y());
        assertTrue(state.dragStartTextPositions.isEmpty());
        assertFalse(state.questDetailsTransientTexts.isEmpty());

        CanvasTransformSessions.clearQuestDetailsSession(state);

        assertTrue(state.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetailsTransformId.isBlank());
        assertTrue(state.questDetailsTransientTexts.isEmpty());
        assertFalse(state.snapGuideXVisible);
        assertFalse(state.snapGuideYVisible);
    }

    private static CanvasTextLayer text(String id, int x, int y) {
        return new CanvasTextLayer(id, "Label", x, y, 80, 30, 0, "left", "normal", 0xFFFFFFFF);
    }
}
