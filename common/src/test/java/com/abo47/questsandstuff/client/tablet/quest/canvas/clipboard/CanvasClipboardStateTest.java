package com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasClipboardStateTest {
    @Test
    void storeTracksQuestClipboardCanvasLayersAndOrigin() {
        CanvasClipboardState state = new CanvasClipboardState();
        CanvasImageLayer image = image("image:one");
        CanvasTextLayer text = text("text:one");

        state.store(true, List.of(image), List.of(text), 12, 34);

        assertTrue(state.hasQuestClipboard());
        assertTrue(state.hasCanvasLayers());
        assertTrue(state.hasContent());
        assertEquals(12, state.originX());
        assertEquals(34, state.originY());
        assertEquals(List.of(image), state.imageLayers());
        assertEquals(List.of(text), state.textLayers());
        assertEquals(1, state.imageCount());
        assertEquals(1, state.textCount());
    }

    @Test
    void recordAndClearPendingPastedLayersDoesNotClearClipboardContent() {
        CanvasClipboardState state = new CanvasClipboardState();
        state.store(false, List.of(image("image:one")), List.of(text("text:one")), 5, 6);

        state.recordPastedImage(" image:two ");
        state.recordPastedText(" text:two ");
        state.recordPastedImage(" ");
        state.recordPastedText(null);

        assertTrue(state.pendingPastedImageIds().contains("image:two"));
        assertTrue(state.pendingPastedTextIds().contains("text:two"));
        assertEquals("image:two", state.lastPendingPastedImageId());
        assertEquals("text:two", state.lastPendingPastedTextId());

        state.clearPendingPastedLayers();

        assertTrue(state.pendingPastedImageIds().isEmpty());
        assertTrue(state.pendingPastedTextIds().isEmpty());
        assertTrue(state.hasCanvasLayers());
    }

    @Test
    void storingNewContentClearsPreviousPendingPastedLayers() {
        CanvasClipboardState state = new CanvasClipboardState();
        state.store(false, List.of(image("image:one")), List.of(), 1, 2);
        state.recordPastedImage("image:two");

        state.store(false, List.of(), List.of(text("text:one")), 3, 4);

        assertTrue(state.pendingPastedImageIds().isEmpty());
        assertTrue(state.pendingPastedTextIds().isEmpty());
        assertFalse(state.hasQuestClipboard());
        assertTrue(state.hasCanvasLayers());
        assertEquals(0, state.imageCount());
        assertEquals(1, state.textCount());
        assertEquals(3, state.originX());
        assertEquals(4, state.originY());
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", 10, 20, 40, 50, 0);
    }

    private static CanvasTextLayer text(String id) {
        return new CanvasTextLayer(id, "Hello", 10, 20, 80, 24, 0, "left", "normal", 0xFFFFFFFF);
    }
}
