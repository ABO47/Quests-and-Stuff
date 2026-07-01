package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.gui.GuiGraphics;

public final class TiledTexture implements IGuiTexture {
    private final IGuiTexture source;
    private final int tileW;
    private final int tileH;

    public TiledTexture(IGuiTexture source, int tileW, int tileH) {
        this.source = source;
        this.tileW = tileW;
        this.tileH = tileH;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        int tw = Math.max(1, tileW);
        int th = Math.max(1, tileH);
        boolean needsClip = width % tw != 0 || height % th != 0;
        if (needsClip) {
            graphics.enableScissor((int)x, (int)y, (int)(x + width), (int)(y + height));
        }
        for (int tx = 0; tx < width; tx += tw) {
            for (int ty = 0; ty < height; ty += th) {
                source.draw(graphics, mouseX, mouseY, x + tx, y + ty, tw, th);
            }
        }
        if (needsClip) {
            graphics.disableScissor();
        }
    }
}
