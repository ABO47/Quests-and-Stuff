package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CanvasLayerOrderingTest {
    @Test
    void normalizedOrderProvidesRenderOrderAndHitPriority() {
        TabletUiState state = new TabletUiState();
        String chapter = "main";
        QuestCardLayout quest = card("quest_a");
        CanvasImageLayer image = image("image_a");
        CanvasTextLayer text = text("text_a");
        String connection = CanvasLayerOrdering.connectionKey("quest_a->quest_b");

        state.canvas.canvasLayerOrderByChapter.put(group, List.of(
                CanvasLayerOrdering.questKey("quest_a"),
                CanvasLayerOrdering.imageKey("image_a"),
                CanvasLayerOrdering.textKey("text_a"),
                connection
        ));

        CanvasLayerOrder order = CanvasLayerOrdering.normalizedOrder(
                state,
                group,
                List.of(quest),
                List.of(image),
                List.of(text),
                List.of(connection)
        );

        assertEquals(List.of(
                CanvasLayerKey.connection("quest_a->quest_b"),
                CanvasLayerKey.quest("quest_a"),
                CanvasLayerKey.image("image_a"),
                CanvasLayerKey.text("text_a")
        ), order.backToFront());
        assertEquals(List.of(
                CanvasLayerKey.text("text_a"),
                CanvasLayerKey.image("image_a"),
                CanvasLayerKey.quest("quest_a"),
                CanvasLayerKey.connection("quest_a->quest_b")
        ), order.hitPriority());
    }

    @Test
    void resolveElementHitUsesTheTopVisibleLayer() {
        QuestCardLayout quest = card("quest_a");
        CanvasImageLayer image = image("image_a");
        CanvasTextLayer text = text("text_a");
        List<String> order = List.of(
                CanvasLayerOrdering.connectionKey("quest_a->quest_b"),
                CanvasLayerOrdering.questKey("quest_a"),
                CanvasLayerOrdering.imageKey("image_a"),
                CanvasLayerOrdering.textKey("text_a")
        );

        CanvasLayerHit hit = CanvasLayerOrdering.resolveElementHit(order, quest, image, text);

        assertNull(hit.quest());
        assertNull(hit.image());
        assertSame(text, hit.text());
    }

    @Test
    void imageCanBeTheTopHitWhenItIsOrderedAboveQuest() {
        QuestCardLayout quest = card("quest_a");
        CanvasImageLayer image = image("image_a");
        List<String> order = List.of(
                CanvasLayerOrdering.questKey("quest_a"),
                CanvasLayerOrdering.imageKey("image_a")
        );

        CanvasLayerHit hit = CanvasLayerOrdering.resolveElementHit(order, quest, image, null);

        assertNull(hit.quest());
        assertSame(image, hit.image());
        assertNull(hit.text());
    }

    private static QuestCardLayout card(String questId) {
        return new QuestCardLayout(questId, new CompoundTag(), 0, 0, 80, 40, 80, 40, 0, 0, 1.0f, 0, 0, 80, 40);
    }

    private static CanvasImageLayer image(String id) {
        return new CanvasImageLayer(id, "textures/example.png", 0, 0, 32, 32, 0);
    }

    private static CanvasTextLayer text(String id) {
        return new CanvasTextLayer(id, "Text", 0, 0, 64, 24, 0, "left", "normal", 0xFFFFFF);
    }
}
