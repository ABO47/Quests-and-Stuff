package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public final class QuestDetailsRootWidget extends WidgetGroup {
    private final TabletUiState state;

    public QuestDetailsRootWidget(int x, int y, int width, int height, TabletUiState state) {
        super(x, y, width, height);
        this.state = state;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        IGuiTexture bg = getBackgroundTexture();
        if (bg != null && !bg.equals(IGuiTexture.EMPTY)) {
            bg.draw(graphics, 0, 0, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        } else {
            SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
