package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.quest.canvas.render.WorldPortalCapture;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletActiveState;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;

public final class QuestDetailsRootWidget extends WidgetGroup {
    public QuestDetailsRootWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        WorldPortalCapture.captureMainCanvas(TabletActiveState.getActiveTabletState());
        int rx = getPositionX(), ry = getPositionY(), rw = getSizeWidth(), rh = getSizeHeight();
        IGuiTexture bg = getBackgroundTexture();
        if (bg != null && !bg.equals(IGuiTexture.EMPTY)) {
            bg.draw(graphics, 0, 0, rx, ry, rw, rh);
        } else {
            SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE).draw(graphics, mouseX, mouseY, rx, ry, rw, rh);
        }
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
