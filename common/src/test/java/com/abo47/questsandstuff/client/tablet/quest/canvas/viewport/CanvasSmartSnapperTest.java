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
        state.objectSnapEnabled = true;
        state.canvasContentX = 10;
        state.canvasContentY = 20;
        state.canvasOffsetX = 3;
        state.canvasOffsetY = 5;
        state.canvasZoom = 1.0f;
        state.canvasContentW = 200;
        state.canvasContentH = 200;

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
        assertTrue(state.snapGuideXVisible);
        assertTrue(state.snapGuideYVisible);
        assertEquals(142, state.snapGuideX);
        assertEquals(70, state.snapGuideY);
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
