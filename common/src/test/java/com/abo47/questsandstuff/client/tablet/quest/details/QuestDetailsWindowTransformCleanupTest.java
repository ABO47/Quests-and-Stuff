package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsWindowTransformCleanupTest {
    @Test
    void closeClearsQuestDetailsTransformSessionBeforeAnimationFinishes() {
        TabletUiState state = new TabletUiState();
        state.questDetailsOpen = true;
        state.questDetailsQuestId = "quest/a";
        state.questDetailsTransformKind = "desc_image";
        state.questDetailsTransformId = "image:a";
        state.questDetailsTransformMode = "move";
        state.questDetailsTransformAxis = "move_x";
        state.questDetailsTransientImages.put("image:a", image("image:a"));
        state.dragStartImagePositions.put("image:a", new CanvasPoint(10, 20));
        state.snapGuideXVisible = true;
        state.snapGuideYVisible = true;

        QuestDetailsWindow.close(state);

        assertTrue(state.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetailsTransformId.isBlank());
        assertTrue(state.questDetailsTransformMode.isBlank());
        assertTrue(state.questDetailsTransformAxis.isBlank());
        assertTrue(state.questDetailsTransientImages.isEmpty());
        assertTrue(state.dragStartImagePositions.isEmpty());
        assertFalse(state.snapGuideXVisible);
        assertFalse(state.snapGuideYVisible);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
