package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionTransformApplyTest {
    @Test
    void previewTextTransformWritesTransientTextOnly() {
        TabletUiState state = new TabletUiState();
        state.questDetailsTransformKind = "desc_text";
        state.questDetailsTransformId = "text";
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(text("text", 10, 20));
        model.putImage(image("image", 30, 40));

        QuestDetailsDescriptionTransformApply.preview(state, model);

        assertEquals(text("text", 10, 20), state.questDetailsTransientTexts.get("text"));
        assertTrue(state.questDetailsTransientImages.isEmpty());
    }

    @Test
    void previewSelectionTransformWritesSelectedTransientLayers() {
        TabletUiState state = new TabletUiState();
        state.questDetailsTransformKind = "selection";
        state.questDetailsDescriptionSelection.textIds().add("text");
        state.questDetailsDescriptionSelection.imageIds().add("image");
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putText(text("text", 10, 20));
        model.putText(text("other", 40, 50));
        model.putImage(image("image", 30, 40));

        QuestDetailsDescriptionTransformApply.preview(state, model);

        assertEquals(text("text", 10, 20), state.questDetailsTransientTexts.get("text"));
        assertEquals(image("image", 30, 40), state.questDetailsTransientImages.get("image"));
        assertFalse(state.questDetailsTransientTexts.containsKey("other"));
    }

    @Test
    void clearEditDragStateCancelsSelectionAndTransformSession() {
        TabletUiState state = new TabletUiState();
        state.selectingCanvasTextRange = true;
        state.questDetailsBoxSelecting = true;
        state.questDetailsTransformKind = "desc_text";
        state.questDetailsTransformId = "text";
        state.questDetailsTransientTexts.put("text", text("text", 10, 20));

        QuestDetailsDescriptionTransformApply.clearEditDragState(state);

        assertFalse(state.selectingCanvasTextRange);
        assertFalse(state.questDetailsBoxSelecting);
        assertTrue(state.questDetailsTransformKind.isBlank());
        assertTrue(state.questDetailsTransformId.isBlank());
        assertTrue(state.questDetailsTransientTexts.isEmpty());
    }

    private static CanvasTextLayer text(String id, int x, int y) {
        return new CanvasTextLayer(id, "Text", x, y, 80, 30, 0, "left", "normal", 0xFFFFFFFF);
    }

    private static CanvasImageLayer image(String id, int x, int y) {
        return new CanvasImageLayer(id, "item:minecraft:diamond", x, y, 40, 40, 0);
    }
}
