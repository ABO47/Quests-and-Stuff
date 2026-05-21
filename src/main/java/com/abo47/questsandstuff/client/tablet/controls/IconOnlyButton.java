package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public final class IconOnlyButton extends ButtonWidget {
    private final ResourceTexture iconTexture;
    private final int iconSize;
    private final int normalColor;
    private final int hoverColor;
    private boolean drawingHover;

    private IconOnlyButton(int x, int y, int size, ResourceLocation icon, int normalColor, int hoverColor, Consumer<ClickData> callback) {
        super(x, y, size, size, Surfaces.fill(0x00000000), callback);
        this.iconSize = Math.min(TabletUiFactory.ACTION_ICON_SIZE, Math.max(8, size - 4));
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.iconTexture = new SmoothResourceTexture(icon).setDynamicColor(() -> drawingHover ? this.hoverColor : this.normalColor);
        setClientSideWidget();
        setHoverTexture(Surfaces.fill(0x00000000));
        setClickedTexture(Surfaces.fill(0x00000000));
    }

    public static IconOnlyButton create(int x, int y, int size, String icon, int color, Consumer<ClickData> callback) {
        ResourceLocation iconLocation = UiIconAtlas.icon(icon);
        if (iconLocation == null) {
            iconLocation = UiIconAtlas.icon("style");
        }
        int hoverColor = brighten(color);
        return new IconOnlyButton(x, y, size, iconLocation, color, hoverColor, callback);
    }

    public static ImageWidget icon(int x, int y, int size, String icon, int color) {
        ResourceLocation iconLocation = UiIconAtlas.icon(icon);
        if (iconLocation == null) {
            iconLocation = UiIconAtlas.icon("style");
        }
        return new ImageWidget(x, y, size, size, new SmoothResourceTexture(iconLocation).setDynamicColor(() -> color));
    }

    public IconOnlyButton tooltips(Component[] tooltips) {
        if (tooltips != null && tooltips.length > 0) {
            setHoverTooltips(tooltips);
        }
        return this;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawingHover = isMouseOverElement(mouseX, mouseY) && isActive();
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position pos = getPosition();
        int iconX = pos.x + Math.max(0, (getSizeWidth() - iconSize) / 2);
        int iconY = pos.y + Math.max(0, (getSizeHeight() - iconSize) / 2);
        iconTexture.draw(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize);
        drawingHover = false;
    }

    private static int brighten(int color) {
        int alpha = color & 0xFF000000;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 42);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 42);
        int b = Math.min(255, (color & 0xFF) + 42);
        return alpha | (r << 16) | (g << 8) | b;
    }
}
