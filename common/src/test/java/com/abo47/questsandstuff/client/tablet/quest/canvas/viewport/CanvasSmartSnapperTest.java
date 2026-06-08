package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasSmartSnapperTest {
    @Test
    void mainCanvasAdapterUsesSharedObjectSnapGuides() {
        TabletUiState state = new TabletUiState();
        state.canvas.objectSnapEnabled = true;
        state.canvas.canvasContentX = 10;
        state.canvas.canvasContentY = 20;
        state.canvas.canvasOffsetX = 3;
        state.canvas.canvasOffsetY = 5;
        state.canvas.canvasZoom = 1.0f;
        state.canvas.canvasContentW = 200;
        state.canvas.canvasContentH = 200;

        CanvasSnapEngine.SnapResult result = CanvasSmartSnapper.snap(
                state,
                new CanvasSnapEngine.Bounds(45, 45, 125, 75),
                List.of(card("quest:target", 129, 45, 80, 30)),
                "main",
                Set.of(),
                Set.of(),
                Set.of()
        );

        assertEquals(4, result.offsetX());
        assertEquals(0, result.offsetY());
        assertTrue(result.guideXVisible());
        assertTrue(result.guideYVisible());
        assertTrue(state.canvas.snapGuideXVisible);
        assertTrue(state.canvas.snapGuideYVisible);
        assertEquals(142, state.canvas.snapGuideX);
        assertEquals(70, state.canvas.snapGuideY);
    }

    private static QuestCardLayout card(String id, int x, int y, int width, int height) {
        return new QuestCardLayout(
                id,
                new CompoundTag(),
                x,
                y,
                width,
                height,
                width,
                height,
                x,
                y,
                1.0f,
                x,
                y,
                width,
                height
        );
    }
}
