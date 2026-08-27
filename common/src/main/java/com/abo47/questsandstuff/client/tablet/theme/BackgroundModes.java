package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;

public final class BackgroundModes {
    private BackgroundModes() {
    }

    public static SkinFillOverride decode(String background) {
        return SkinFillOverride.parse(background);
    }

    public static String encode(String mode, String path) {
        return encode(mode, path, 0, 0);
    }

    public static String encode(String mode, String path, int leftEdge, int rightEdge) {
        SkinFillOverride o = new SkinFillOverride(mode, leftEdge, rightEdge, path);
        return o.encode();
    }

    public static String encode(String mode, String path, int leftEdge, int rightEdge, int topEdge, int bottomEdge) {
        SkinFillOverride o = new SkinFillOverride(mode, leftEdge, rightEdge, topEdge, bottomEdge, path);
        return o.encode();
    }

    public static String stripMode(String background) {
        if (background == null || background.isBlank() || "default".equals(background)) {
            return background;
        }
        SkinFillOverride o = decode(background);
        return o != null ? o.path() : background;
    }

    public static IGuiTexture createTexture(String background) {
        if (background == null || background.isBlank() || "default".equals(background)) {
            return null;
        }
        SkinFillOverride o = decode(background);
        if (o == null) {
            return null;
        }
        return o.createTexture();
    }
}
