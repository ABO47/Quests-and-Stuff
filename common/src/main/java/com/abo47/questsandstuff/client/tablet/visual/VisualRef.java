package com.abo47.questsandstuff.client.tablet.visual;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public final class VisualRef {
    public static final String ASSET_PREFIX = "asset|";

    private VisualRef() {
    }

    public static String assetRef(String relativePath) {
        String path = relativePath == null ? "" : relativePath.trim();
        return path.startsWith(ASSET_PREFIX) ? path : ASSET_PREFIX + path;
    }

    public static boolean isAssetRef(String ref) {
        return ref != null && ref.startsWith(ASSET_PREFIX);
    }

    public static String assetPath(String ref) {
        if (!isAssetRef(ref)) {
            return "";
        }
        return ref.substring(ASSET_PREFIX.length()).trim();
    }

    public static IGuiTexture assetIconTexture(String ref) {
        String path = assetPath(ref);
        if (path.isEmpty()) {
            return null;
        }
        return TabletUiFactory.chapterBackgroundTexture(path);
    }
}
