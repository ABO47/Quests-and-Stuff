package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
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
        state.canvas.dragStartTextPositions.put("stale", new CanvasPoint(1, 2));
        state.questDetails.questDetailsTransientTexts.put("stale", text("stale", 1, 2));
        state.canvas.snapGuideXVisible = true;
        state.canvas.snapGuideYVisible = true;

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
        CanvasLayerMutations.putTransientQuestDetailsText(state, model.text("text:a").moveTo(25, 45));

        CanvasTextLayer moved = state.questDetails.questDetailsTransientTexts.get("text:a");
        assertEquals(25, moved.x());
        assertEquals(45, moved.y());
        assertTrue(state.canvas.dragStartTextPositions.isEmpty());
        assertFalse(state.questDetails.questDetailsTransientTexts.isEmpty());

        CanvasTransformSessions.clearQuestDetailsSession(state);

        assertTrue(state.questDetails.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetails.questDetailsTransformId.isBlank());
        assertTrue(state.questDetails.questDetailsTransientTexts.isEmpty());
        assertFalse(state.canvas.snapGuideXVisible);
        assertFalse(state.canvas.snapGuideYVisible);
    }

    @Test
    void descriptionTransformUsesSharedObjectSnapGuides() {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsObjectSnapEnabled = true;
        state.questDetails.questDetailsGridSnapLocked = true;

        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(text("moving", 45, 45));
        model.putText(text("target", 129, 45));
        QuestDetailsDescriptionTransform transform = new QuestDetailsDescriptionTransform(state, () -> 0, () -> 0, () -> 200, () -> 200);

        transform.beginTransform(
                model,
                "desc_text",
                "moving",
                new QuestDetailsDescriptionTransform.ElementRect(45, 45, 80, 30, 0),
                false,
                false,
                false,
                45,
                45,
                45
        );
        transform.applyTransform(model, 45, 45);

        assertEquals(49, model.text("moving").x());
        assertEquals(45, model.text("moving").y());
        assertTrue(state.canvas.snapGuideXVisible);
        assertTrue(state.canvas.snapGuideYVisible);
        assertEquals(129, state.canvas.snapGuideX);
        assertEquals(45, state.canvas.snapGuideY);
    }

    private static CanvasTextLayer text(String id, int x, int y) {
        return new CanvasTextLayer(id, "Label", x, y, 80, 30, 0, "left", "normal", 0xFFFFFFFF);
    }
}
