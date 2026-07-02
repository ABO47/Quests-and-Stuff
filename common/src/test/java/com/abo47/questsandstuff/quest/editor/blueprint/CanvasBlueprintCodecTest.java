package com.abo47.questsandstuff.quest.editor.blueprint;

import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasBlueprintCodecTest {
    @Test
    void roundTripsBlueprintShareCode() {
        CanvasBlueprint blueprint = new CanvasBlueprint(
                "Test Blueprint",
                12,
                34,
                List.of(),
                List.of(new CanvasImageLayer("img_1", "pics/test.png", 40, 50, 80, 60, 0)),
                List.of(),
                List.of("image:img_1")
        );

        String code = CanvasBlueprintCodec.encode(blueprint);
        CanvasBlueprint decoded = CanvasBlueprintCodec.decode(code);

        assertTrue(code.startsWith(CanvasBlueprintCodec.PREFIX));
        assertFalse(decoded.isEmpty());
        assertEquals("Test Blueprint", decoded.name());
        assertEquals(1, decoded.images().size());
        assertEquals("pics/test.png", decoded.images().get(0).asset());
    }

    @Test
    void malformedBlueprintCodeFallsBackToEmptyBlueprint() {
        CanvasBlueprint decoded = CanvasBlueprintCodec.decode(CanvasBlueprintCodec.PREFIX + "not-valid-base64");

        assertTrue(decoded.isEmpty());
    }

    @Test
    void malformedBlueprintJsonFieldsUseSafeFallbacks() {
        CanvasBlueprint decoded = CanvasBlueprint.fromJson("""
                {
                  "name": "Broken fields",
                  "origin_x": "bad",
                  "images": [
                    {
                      "id": "img_bad",
                      "asset": "pics/test.png",
                      "x": "bad",
                      "w": "wide"
                    }
                  ],
                  "layer_order": ["image:img_bad"]
                }
                """);

        assertFalse(decoded.isEmpty());
        assertEquals(0, decoded.originX());
        assertEquals(1, decoded.images().size());
        assertEquals(0, decoded.images().get(0).x());
        assertEquals(80, decoded.images().get(0).w());
    }
}
