package com.abo47.questsandstuff.client.tablet.quest.canvas.snap;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class CanvasSnapBounds {
    private CanvasSnapBounds() {
    }

    public static CanvasSnapEngine.Bounds forQuestCard(QuestCardLayout card) {
        return new CanvasSnapEngine.Bounds(
                card.logicalX(),
                card.logicalY(),
                card.logicalX() + card.slotLogicalWidth(),
                card.logicalY() + card.slotLogicalHeight()
        );
    }

    public static CanvasSnapEngine.Bounds forImage(CanvasImageLayer image) {
        return atPivot(image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
    }

    public static CanvasSnapEngine.Bounds forText(CanvasTextLayer text) {
        return atCenterPivot(text.x(), text.y(), text.w(), text.h(), text.rotation());
    }

    public static CanvasSnapEngine.Bounds atCenterPivot(int x, int y, int width, int height, int rotationDegrees) {
        return atPivot(
                x,
                y,
                width,
                height,
                CanvasElementGeometry.defaultPivot(width),
                CanvasElementGeometry.defaultPivot(height),
                rotationDegrees
        );
    }

    public static CanvasSnapEngine.Bounds atPivot(int x, int y, int width, int height, int pivotX, int pivotY, int rotationDegrees) {
        int[] bounds = CanvasElementGeometry.logicalBoundsAtPivot(x, y, width, height, pivotX, pivotY, rotationDegrees);
        return new CanvasSnapEngine.Bounds(bounds[0], bounds[1], bounds[2], bounds[3]);
    }
}
