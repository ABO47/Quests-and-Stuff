package com.abo47.questsandstuff.client.tablet.screen;

import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

final class HeaderIconWidget extends Widget {
    private final String iconName;

    HeaderIconWidget(int x, int y, int size, String iconName) {
        super(x, y, size, size);
        this.iconName = iconName;
        setActive(false);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var texture = UiIconAtlas.iconTexture(iconName);
        if (texture != null) {
            texture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }
}
