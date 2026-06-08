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
        state.draggingCanvasImage = true;
        state.canvasImageTransformAxis = "move_x";
        state.transientCanvasImages.put("image:a", image("image:a"));
        state.transientQuestPositions.put("quest/a", new CanvasPoint(10, 20));
        state.snapGuideXVisible = true;
        state.snapGuideYVisible = true;

        assertTrue(TabletShortcutActions.cancelTransient(state));

        assertFalse(state.draggingCanvasImage);
        assertTrue(state.canvasImageTransformAxis.isBlank());
        assertTrue(state.transientCanvasImages.isEmpty());
        assertTrue(state.transientQuestPositions.isEmpty());
        assertFalse(state.snapGuideXVisible);
        assertFalse(state.snapGuideYVisible);
    }

    @Test
    void escapeCancelsQuestDetailsTransformWithoutClearingMainPreviews() {
        TabletUiState state = new TabletUiState();
        state.questDetailsTransformKind = "desc_image";
        state.questDetailsTransformId = "image:b";
        state.questDetailsTransformMode = "move";
        state.questDetailsTransientImages.put("image:b", image("image:b"));
        state.transientCanvasImages.put("image:a", image("image:a"));

        assertTrue(TabletShortcutActions.cancelTransient(state));

        assertTrue(state.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetailsTransformId.isBlank());
        assertTrue(state.questDetailsTransformMode.isBlank());
        assertTrue(state.questDetailsTransientImages.isEmpty());
        assertFalse(state.transientCanvasImages.isEmpty());
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }
}
