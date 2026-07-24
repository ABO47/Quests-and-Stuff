package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.Consumer;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.utils.Position;

import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;

public final class IconOnlyButton extends ButtonWidget {
    private final ResourceTexture iconTexture;
    private final int iconSize;

    private IconOnlyButton(int x, int y, int size, ResourceLocation icon, int color, Consumer<ClickData> callback) {
        super(x, y, size, size, SurfaceFactory.transparentFill(), callback);
        this.iconSize = Math.max(8, size - 2);
        this.iconTexture = new SmoothResourceTexture(icon).setDynamicColor(() -> color);
        setClientSideWidget();
        setHoverTexture(GlowShaderHelper.hoverGlow());
        setClickedTexture(SurfaceFactory.transparentFill());
    }

    public static IconOnlyButton create(int x, int y, int size, String icon, int color, Consumer<ClickData> callback) {
        ResourceLocation iconLocation = resolveIcon(icon);
        return new IconOnlyButton(x, y, size, iconLocation, color, callback);
    }

    public static ImageWidget icon(int x, int y, int size, String icon, int color) {
        ResourceLocation iconLocation = resolveIcon(icon);
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
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position pos = getPosition();
        int iconX = pos.x + Math.max(0, (getSizeWidth() - iconSize) / 2);
        int iconY = pos.y + Math.max(0, (getSizeHeight() - iconSize) / 2);
        iconTexture.draw(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize);
    }

    private static ResourceLocation resolveIcon(String icon) {
        ResourceLocation iconLocation = IconAtlas.icon(icon);
        if (iconLocation == null && icon != null && !icon.isBlank()) {
            iconLocation = IconAtlas.icon("context_" + icon);
        }
        if (iconLocation == null) {
            iconLocation = IconAtlas.icon("text");
        }
        if (iconLocation == null) {
            iconLocation = IconAtlas.icon("add");
        }
        return iconLocation;
    }
}
