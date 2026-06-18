package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public record CanvasLayerHit(QuestCardLayout quest, CanvasImageLayer image, CanvasTextLayer text, CanvasExclusiveChoice exclusiveChoice) {
    public static final CanvasLayerHit EMPTY = new CanvasLayerHit(null, null, null, null);
}
