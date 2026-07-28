package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import org.joml.Vector4f;

import net.minecraft.client.gui.GuiGraphics;

public final class CanvasViewportScissor {
    private CanvasViewportScissor() {
    }

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height, Runnable draw) {
        var trans = graphics.pose().last().pose();
        var realPos = trans.transform(new Vector4f(x, y, 0, 1));
        var realPos2 = trans.transform(new Vector4f(x + width, y + height, 0, 1));
        graphics.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
        try {
            draw.run();
        } finally {
            graphics.disableScissor();
        }
    }
}
