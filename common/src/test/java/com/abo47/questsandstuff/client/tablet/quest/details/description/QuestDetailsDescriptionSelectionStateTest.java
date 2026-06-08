package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDetailsDescriptionSelectionStateTest {
    @Test
    void selectingDescriptionImageClearsMainCanvasSelection() {
        TabletUiState state = new TabletUiState();
        state.selectedQuestIds.add("main_quest");
        state.selectedCanvasImageId = "main_image";
        state.selectedCanvasImageIds.add("main_image");
        state.selectedCanvasTextId = "main_text";
        state.selectedCanvasTextIds.add("main_text");

        QuestDetailsDescriptionSelectionState.selectOnlyImage(state, "desc_image");

        assertTrue(state.selectedQuestIds.isEmpty());
        assertEquals("", state.selectedCanvasImageId);
        assertTrue(state.selectedCanvasImageIds.isEmpty());
        assertEquals("", state.selectedCanvasTextId);
        assertTrue(state.selectedCanvasTextIds.isEmpty());
        assertEquals("desc_image", state.questDetailsSelectedImageId);
        assertEquals(List.of("image:desc_image"), QuestDetailsDescriptionSelectionState.selectionSet(state).layerKeys());
    }

    @Test
    void descriptionSelectedLayerKeysUseSharedSelectionOrdering() {
        TabletUiState state = new TabletUiState();
        state.questDetailsSelectedImageIds.add("desc_image");
        state.questDetailsSelectedTextIds.add("desc_text");
        QuestDetailsDescriptionModel model = new QuestDetailsDescriptionModel();
        model.putImage(new CanvasImageLayer("desc_image", "textures/example.png", 0, 0, 32, 32, 0));
        model.putText(new CanvasTextLayer("desc_text", "Text", 0, 0, 64, 24, 0, "left", "normal", 0xFFFFFF));
        model.ensureOrder("text:desc_text");
        model.ensureOrder("image:desc_image");

        assertEquals(List.of("text:desc_text", "image:desc_image"),
                QuestDetailsDescriptionSelectionState.selectedLayerKeys(state, model));
    }
}
