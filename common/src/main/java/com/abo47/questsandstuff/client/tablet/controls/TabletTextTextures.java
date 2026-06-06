package com.abo47.questsandstuff.client.tablet.controls;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import net.minecraft.network.chat.Component;

public final class TabletTextTextures {
    private TabletTextTextures() {
    }

    public static TextTextureWidget literal(int x, int y, int width, int height, String text, int color, TextTexture.TextType type) {
        TextTextureWidget widget = new TextTextureWidget(x, y, width, height);
        widget.setClientSideWidget();
        widget.setLastComponent(Component.literal(escapeFormatText(text)));
        widget.textureStyle(texture -> configure(texture, width, color, type));
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

    private static String escapeFormatText(String text) {
        return text == null ? "" : text.replace("%", "%%");
    }
}
