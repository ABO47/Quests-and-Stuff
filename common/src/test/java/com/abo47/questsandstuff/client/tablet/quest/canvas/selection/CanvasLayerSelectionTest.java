package com.abo47.questsandstuff.client.tablet.quest.canvas.selection;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKey;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasLayerSelectionTest {
    @Test
    void selectionStoresTypedLayerKeysAndProjectsLegacyIds() {
        CanvasSelectionSet selection = new CanvasSelectionSet(
                Set.of("quest_a"),
                Set.of("image_a"),
                Set.of("text_a")
        );

        assertEquals(Set.of("quest_a"), selection.questIds());
        assertEquals(Set.of("image_a"), selection.imageIds());
        assertEquals(Set.of("text_a"), selection.textIds());
        assertTrue(selection.typedLayerKeys().contains(CanvasLayerKey.quest("quest_a")));
        assertTrue(selection.typedLayerKeys().contains(CanvasLayerKey.image("image_a")));
        assertTrue(selection.typedLayerKeys().contains(CanvasLayerKey.text("text_a")));
    }

    @Test
    void selectedLayerKeysFollowTheVisibleLayerOrder() {
        CanvasSelectionSet selection = new CanvasSelectionSet(
                Set.of("quest_a"),
                Set.of("image_a"),
                Set.of("text_a")
        );

        assertEquals(List.of(
                CanvasLayerOrdering.imageKey("image_a"),
                CanvasLayerOrdering.textKey("text_a"),
                CanvasLayerOrdering.questKey("quest_a")
        ), selection.layerKeysInOrder(List.of(
                CanvasLayerOrdering.connectionKey("quest_a->quest_b"),
                CanvasLayerOrdering.imageKey("image_a"),
                CanvasLayerOrdering.textKey("text_a"),
                CanvasLayerOrdering.questKey("quest_a")
        )));
    }

    @Test
    void selectionStateOwnsPrimaryIdsAndTypedProjection() {
        CanvasLayerSelectionState state = new CanvasLayerSelectionState();

        state.addQuest(" quest_a ");
        state.addImage(" image_a ");
        state.addText(" text_a ");

        assertEquals("image_a", state.primaryImageId());
        assertEquals("text_a", state.primaryTextId());
        assertTrue(state.hasQuest("quest_a"));
        assertTrue(state.hasImage("image_a"));
        assertTrue(state.hasText("text_a"));
        assertTrue(state.typedKeys().contains(CanvasLayerKey.quest("quest_a")));
        assertTrue(state.typedKeys().contains(CanvasLayerKey.image("image_a")));
        assertTrue(state.typedKeys().contains(CanvasLayerKey.text("text_a")));
        assertEquals(List.of("quest:quest_a", "image:image_a", "text:text_a"), state.selectionSet().layerKeys());
    }

    @Test
    void selectionStateIgnoresStalePrimaryAfterSetMutation() {
        CanvasLayerSelectionState state = new CanvasLayerSelectionState();
        state.addImage("image_a");
        state.addText("text_a");

        state.imageIds().clear();
        state.textIds().clear();

        assertEquals("", state.primaryImageId());
        assertEquals("", state.primaryTextId());
        assertTrue(state.selectionSet().layerKeys().isEmpty());
    }
}
