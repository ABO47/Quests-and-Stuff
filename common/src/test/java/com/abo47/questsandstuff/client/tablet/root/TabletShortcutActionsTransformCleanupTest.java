package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletShortcutActionsTransformCleanupTest {
    @Test
    void escapeCancelsMainCanvasTransformSession() {
        TabletUiState state = new TabletUiState();
        state.canvas.draggingCanvasImage = true;
        state.canvas.canvasImageTransformAxis = "move_x";
        state.canvas.transientCanvasImages.put("image:a", image("image:a"));
        state.canvas.transientQuestPositions.put("quest/a", new CanvasPoint(10, 20));
        state.canvas.snapGuideXVisible = true;
        state.canvas.snapGuideYVisible = true;

        assertTrue(TabletShortcutActions.cancelTransient(state));

        assertFalse(state.canvas.draggingCanvasImage);
        assertTrue(state.canvas.canvasImageTransformAxis.isBlank());
        assertTrue(state.canvas.transientCanvasImages.isEmpty());
        assertTrue(state.canvas.transientQuestPositions.isEmpty());
        assertFalse(state.canvas.snapGuideXVisible);
        assertFalse(state.canvas.snapGuideYVisible);
    }

    @Test
    void escapeCancelsQuestDetailsTransformWithoutClearingMainPreviews() {
        TabletUiState state = new TabletUiState();
        state.questDetails.questDetailsTransformKind = "desc_image";
        state.questDetails.questDetailsTransformId = "image:b";
        state.questDetails.questDetailsTransformMode = "move";
        state.questDetails.questDetailsTransientImages.put("image:b", image("image:b"));
        state.canvas.transientCanvasImages.put("image:a", image("image:a"));

        assertTrue(TabletShortcutActions.cancelTransient(state));

        assertTrue(state.questDetails.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetails.questDetailsTransformId.isBlank());
        assertTrue(state.questDetails.questDetailsTransformMode.isBlank());
        assertTrue(state.questDetails.questDetailsTransientImages.isEmpty());
        assertFalse(state.canvas.transientCanvasImages.isEmpty());
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
