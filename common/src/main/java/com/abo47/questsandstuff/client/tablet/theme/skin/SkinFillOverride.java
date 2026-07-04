package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.AssetKind;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
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
            QuestsAndStuffMod.LOGGER.info("[QnS:UI] Creating tile texture path={} root={}", path, TabletUiFactory.ASSETS_ROOT_DIR);
            tex = AssetLibrary.preRenderedTileTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
        } else {
            tex = createFullTexture();
        }
        if (tex == null) return null;
        CACHED.put(cacheKey, tex);
        return tex;
    }

    private IGuiTexture createFullTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return AssetLibrary.chapterBackgroundTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
        }
        return AssetLibrary.assetThumbnailTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
    }
}
