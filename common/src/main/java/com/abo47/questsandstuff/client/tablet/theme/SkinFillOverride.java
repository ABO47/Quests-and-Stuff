package com.abo47.questsandstuff.client.tablet.theme;

import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.util.HashMap;
import java.util.Map;

public record SkinFillOverride(String mode, String path) {
    private static final String SEP = "|";
    private static final Map<String, IGuiTexture> CACHED = new HashMap<>();

    public static SkinFillOverride parse(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        int pipeIdx = encoded.indexOf(SEP);
        if (pipeIdx >= 0) {
            String mode = encoded.substring(0, pipeIdx);
            String path = encoded.substring(pipeIdx + 1);
            if (path.isEmpty()) return null;
            return new SkinFillOverride(mode, path);
        }
        return new SkinFillOverride("stretch", encoded);
    }

    public String encode() {
        return "stretch".equals(mode) ? path : mode + SEP + path;
    }

    public static void clearCache() {
        CACHED.clear();
    }

    public IGuiTexture createTexture() {
        if (path == null || path.isBlank()) return null;
        String cacheKey = mode + ":" + path;
        IGuiTexture cached = CACHED.get(cacheKey);
        if (cached != null) return cached;

        IGuiTexture tex;
        if ("tile".equals(mode)) {
            com.abo47.questsandstuff.QuestsAndStuffMod.LOGGER.info("[QnS:UI] Creating tile texture path={} root={}", path, com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ASSETS_ROOT_DIR);
            tex = AssetLibrary.preRenderedTileTexture(com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ASSETS_ROOT_DIR, path);
        } else {
            tex = createFullTexture();
        }
        if (tex == null) return null;
        CACHED.put(cacheKey, tex);
        return tex;
    }

    private IGuiTexture createFullTexture() {
        com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.AssetKind.GIF) {
            return AssetLibrary.chapterBackgroundTexture(com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ASSETS_ROOT_DIR, path);
        }
        return AssetLibrary.assetThumbnailTexture(com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ASSETS_ROOT_DIR, path);
    }
}
