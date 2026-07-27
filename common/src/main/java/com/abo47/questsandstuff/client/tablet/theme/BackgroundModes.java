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
        return "stretch".equals(mode) ? path : mode + "|" + path;
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
