package com.abo47.questsandstuff.client.tablet.quest.details;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsWindowTransformCleanupTest {
    @Test
    void closeClearsQuestDetailsTransformSessionBeforeAnimationFinishes() {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsOpen = true;
        state.questDetails.questDetailsQuestId = "quest/a";
        state.questDetails.questDetailsTransformKind = "desc_image";
        state.questDetails.questDetailsTransformId = "image:a";
        state.questDetails.questDetailsTransformMode = "move";
        state.questDetails.questDetailsTransformAxis = "move_x";
        state.questDetails.questDetailsTransientImages.put("image:a", image("image:a"));
        state.canvas.dragStartImagePositions.put("image:a", new CanvasPoint(10, 20));
        state.canvas.snapGuideXVisible = true;
        state.canvas.snapGuideYVisible = true;

        QuestDetailsWindow.close(state);

        assertTrue(state.questDetails.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetails.questDetailsTransformId.isBlank());
        assertTrue(state.questDetails.questDetailsTransformMode.isBlank());
        assertTrue(state.questDetails.questDetailsTransformAxis.isBlank());
        assertTrue(state.questDetails.questDetailsTransientImages.isEmpty());
        assertTrue(state.canvas.dragStartImagePositions.isEmpty());
        assertFalse(state.canvas.snapGuideXVisible);
        assertFalse(state.canvas.snapGuideYVisible);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
