package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.AssetKind;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

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
        if (cached != null) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] createTexture CACHED: mode={}, path={}, class={}", mode, path, cached.getClass().getSimpleName());
            return cached;
        }

        IGuiTexture tex;
        if ("tile".equals(mode)) {
            tex = createTileTexture();
        } else if ("center".equals(mode)) {
            tex = createCenterTexture();
        } else if ("dynamic".equals(mode)) {
            tex = createDynamicTexture();
        } else {
            tex = createFullTexture();
        }
        if (tex == null) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] createTexture FAILED: mode={}, path={}", mode, path);
            return null;
        }
        QuestsAndStuffMod.debugLog("[QnS:Skin] createTexture NEW: mode={}, path={}, class={}", mode, path, tex.getClass().getSimpleName());
        CACHED.put(cacheKey, tex);
        return tex;
    }

    private static final int MAX_TILE_SIZE = 64;

    private IGuiTexture createTileTexture() {
        ResourceLocation id = AssetLibrary.tileTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        int tileW = Math.min(dims.width(), MAX_TILE_SIZE);
        int tileH = Math.min(dims.height(), MAX_TILE_SIZE);
        return new TiledGuiTexture(id, tileW, tileH);
    }

    private IGuiTexture createCenterTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return createFullTexture();
        }
        ResourceLocation id = AssetLibrary.staticTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        return new CenterCropTexture(id, dims.width(), dims.height());
    }

    private IGuiTexture createDynamicTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return createFullTexture();
        }
        ResourceLocation id = AssetLibrary.staticTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        return new DynamicClippingTexture(id, dims.width(), dims.height());
    }

    private IGuiTexture createFullTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return AssetLibrary.chapterBackgroundTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
        }
        return AssetLibrary.assetThumbnailTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
    }
}
