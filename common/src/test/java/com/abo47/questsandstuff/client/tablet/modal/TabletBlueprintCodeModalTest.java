package com.abo47.questsandstuff.client.tablet.modal;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprintCodec;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabletBlueprintCodeModalTest {
    @Test
    void longSingleLineBlueprintCodeRoundTripsUnchanged() {
        CanvasBlueprint blueprint = new CanvasBlueprint(
                "Long Blueprint",
                12,
                34,
                List.of(),
                List.of(new CanvasImageLayer("img_1", "textures/very/long/path/that/makes/the/share/code/large.png", 40, 50, 80, 60, 0)),
                List.of(),
                List.of("image:img_1")
        );
        String code = CanvasBlueprintCodec.encode(blueprint);
        assertTrue(code.length() > 92);

        List<String> lines = TabletBlueprintCodeModal.editorLines(code);
        String roundTripped = TabletBlueprintCodeModal.rawCode(lines);
        CanvasBlueprint decoded = CanvasBlueprintCodec.decode(roundTripped);

        assertEquals(List.of(code), lines);
        assertEquals(code, roundTripped);
        assertFalse(decoded.isEmpty());
        assertEquals("Long Blueprint", decoded.name());
    }

    @Test
    void multilineCodePreservesIntentionalLineBreaksWithoutTrimming() {
        List<String> lines = TabletBlueprintCodeModal.editorLines(" first \nsecond\n");

        assertEquals(List.of(" first ", "second", ""), lines);
        assertEquals(" first \nsecond\n", TabletBlueprintCodeModal.rawCode(lines));
    }
}
