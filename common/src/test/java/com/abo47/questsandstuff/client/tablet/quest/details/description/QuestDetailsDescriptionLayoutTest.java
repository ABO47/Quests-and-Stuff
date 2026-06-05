package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionLayoutTest {
    @Test
    void descriptionColumnClampsRightEdgeAndStillAllowsDownwardGrowth() {
        TabletUiState state = new TabletUiState();
        state.questDetailsCanvasLocked = false;
        CanvasTextLayer text = new CanvasTextLayer("text", "Label", 180, 600, 60, 20, 0, "left", "normal", 0xFFFFFF);

        CanvasTextLayer clamped = QuestDetailsDescriptionLayout.clampTextToColumn(state, text, 200);

        assertEquals(140, clamped.x());
        assertEquals(600, clamped.y());
    }

    @Test
    void descriptionColumnClampsBlockModelVisualFootprintOnLockedEdges() {
        TabletUiState state = new TabletUiState();
        state.questDetailsCanvasLocked = false;
        CanvasImageLayer rightBlock = new CanvasImageLayer("rightBlock", "block:minecraft:oak_planks", 150, 40, 100, 100, 0, 45, 0, 30);
        CanvasImageLayer leftTopBlock = new CanvasImageLayer("leftTopBlock", "block:minecraft:oak_planks", 0, 0, 100, 100, 0, 45, 0, 30);

        CanvasImageLayer clampedRight = QuestDetailsDescriptionLayout.clampImageToColumn(state, rightBlock, 200);
        CanvasImageLayer clampedLeftTop = QuestDetailsDescriptionLayout.clampImageToColumn(state, leftTopBlock, 200);
        int[] rightBounds = QuestDetailsDescriptionLayout.imageBoundsForColumnClamp(clampedRight);
        int[] leftTopBounds = QuestDetailsDescriptionLayout.imageBoundsForColumnClamp(clampedLeftTop);

        assertTrue(clampedRight.x() < 100);
        assertTrue(rightBounds[2] <= 200);
        assertEquals(40, clampedRight.y());
        assertTrue(leftTopBounds[0] >= 1);
        assertTrue(leftTopBounds[1] >= 1);
    }

    @Test
    void descriptionColumnClampsLeftRightAndTopWithSameBoundsLogic() {
        TabletUiState state = new TabletUiState();
        state.questDetailsCanvasLocked = false;
        CanvasImageLayer rightOverflow = new CanvasImageLayer("right", "item:minecraft:stick", 180, 20, 60, 20, 0);
        CanvasImageLayer leftTopOverflow = new CanvasImageLayer("left", "item:minecraft:stick", -12, -8, 32, 32, 0);

        CanvasImageLayer clampedRight = QuestDetailsDescriptionLayout.clampImageToColumn(state, rightOverflow, 200);
        CanvasImageLayer clampedLeftTop = QuestDetailsDescriptionLayout.clampImageToColumn(state, leftTopOverflow, 200);

        assertEquals(140, clampedRight.x());
        assertEquals(20, clampedRight.y());
        assertEquals(1, clampedLeftTop.x());
        assertEquals(1, clampedLeftTop.y());
    }

    @Test
    void descriptionColumnClampsTopToVisibleScrollEdge() {
        TabletUiState state = new TabletUiState();
        state.questDetailsCanvasLocked = false;
        state.questDetailsDescScroll = 80;
        CanvasImageLayer aboveVisibleTop = new CanvasImageLayer("image", "item:minecraft:stick", 20, 40, 60, 20, 0);
        CanvasImageLayer belowVisibleTop = new CanvasImageLayer("below", "item:minecraft:stick", 20, 140, 60, 20, 0);

        CanvasImageLayer clampedTop = QuestDetailsDescriptionLayout.clampImageToColumn(state, aboveVisibleTop, 200);
        CanvasImageLayer unchanged = QuestDetailsDescriptionLayout.clampImageToColumn(state, belowVisibleTop, 200);

        assertEquals(20, clampedTop.x());
        assertEquals(81, clampedTop.y());
        assertEquals(20, unchanged.x());
        assertEquals(140, unchanged.y());
    }

    @Test
    void descriptionScrollMaxIsZeroWhenElementsFit() {
        TabletUiState state = new TabletUiState();
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(new CanvasTextLayer("text", "Label", 20, 24, 60, 20, 0, "left", "normal", 0xFFFFFF));

        assertEquals(0, QuestDetailsDescriptionLayout.descriptionScrollMax(model, 100));
        assertEquals(0, QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, 100, 40));
        assertEquals(100, QuestDetailsDescriptionLayout.descriptionScrollKnobHeight(100, 0));
    }

    @Test
    void descriptionScrollMaxAppearsWhenElementsExtendBelowViewport() {
        TabletUiState state = new TabletUiState();
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(new CanvasTextLayer("text", "Label", 20, 90, 60, 32, 0, "left", "normal", 0xFFFFFF));

        int max = QuestDetailsDescriptionLayout.descriptionScrollMax(model, 100);

        assertTrue(max > 0);
        assertEquals(max, QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, 100, max + 40));
        assertTrue(QuestDetailsDescriptionLayout.descriptionScrollKnobHeight(100, max) < 100);
    }
}
