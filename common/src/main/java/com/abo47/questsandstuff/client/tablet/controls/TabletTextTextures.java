package com.abo47.questsandstuff.client.tablet.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;

public final class TabletTextTextures {
    private static final String ELLIPSIS = "..";

    private TabletTextTextures() {
    }

    public static TextTextureWidget literal(int x, int y, int width, int height, String text, int color, TextTexture.TextType type) {
        TextTextureWidget widget = new TextTextureWidget(x, y, width, height);
        widget.setClientSideWidget();
        widget.setLastComponent(Component.literal(escapeFormatText(text)));
        widget.textureStyle(texture -> configure(texture, width, color, type));
        return widget;
    }

    public static LabelWidget flatLiteral(int x, int y, int width, int height, String text, int color, TextTexture.TextType type) {
        Font font = Minecraft.getInstance().font;
        String safe = escapeFormatText(text);
        String fitted = fit(safe, width, font);
        TextTexture.TextType safeType = type == null ? TextTexture.TextType.HIDE : type;
        int drawX = x;
        if (safeType == TextTexture.TextType.RIGHT_HIDE || safeType == TextTexture.TextType.RIGHT_OVERFLOW) {
            drawX = x + Math.max(1, width) - font.width(fitted);
        } else if (safeType == TextTexture.TextType.HIDE) {
            drawX = x + (Math.max(1, width) - font.width(fitted)) / 2;
        }
        int drawY = y + Math.max(0, (height - font.lineHeight) / 2);
        LabelWidget widget = new LabelWidget(drawX, drawY, fitted);
        widget.setClientSideWidget();
        widget.setTextColor(color);
        widget.setDropShadow(false);
        return widget;
    }

    public static TextTexture literalTexture(String text, int width, int color, TextTexture.TextType type) {
        TextTexture texture = new TextTexture(escapeFormatText(text), color);
        configure(texture, width, color, type);
        return texture;
    }

    private static void configure(TextTexture texture, int width, int color, TextTexture.TextType type) {
        texture.setColor(color);
        texture.setDropShadow(false);
        texture.setWidth(Math.max(1, width));
        texture.setType(type == null ? TextTexture.TextType.HIDE : type);
    }

    private static String fit(String text, int width, Font font) {
        int maxWidth = Math.max(1, width);
        if (font.width(text) <= maxWidth) {
            return text;
        }
        int keep = text.length();
        while (keep > 0 && font.width(text.substring(0, keep) + ELLIPSIS) > maxWidth) {
            keep--;
        }
        return text.substring(0, keep) + ELLIPSIS;
    }

    private static String escapeFormatText(String text) {
        return text == null ? "" : text.replace("%", "%%");
    }
}
