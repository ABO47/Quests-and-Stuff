package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary.AssetKind;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public record SkinFillOverride(String mode, String path, int leftEdge, int rightEdge, int topEdge, int bottomEdge) {
    private static final String SEP = "|";
    private static final Map<String, IGuiTexture> CACHED = new HashMap<>();

    public SkinFillOverride(String mode, String path) {
        this(mode, path, 0, 0, 0, 0);
    }

    public SkinFillOverride(String mode, int leftEdge, int rightEdge, String path) {
        this(mode, path, leftEdge, rightEdge, 0, 0);
    }

    public SkinFillOverride(String mode, int leftEdge, int rightEdge, int topEdge, int bottomEdge, String path) {
        this(mode, path, leftEdge, rightEdge, topEdge, bottomEdge);
    }

    public static SkinFillOverride parse(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        int pipeIdx = encoded.indexOf(SEP);
        if (pipeIdx >= 0) {
            String modePart = encoded.substring(0, pipeIdx);
            String path = encoded.substring(pipeIdx + 1);
            if (path.isEmpty()) return null;
            String mode = modePart;
            int left = 0;
            int right = 0;
            int top = 0;
            int bottom = 0;
            if (modePart.indexOf(':') > 0) {
                String[] bits = modePart.split(":");
                if (bits.length >= 5) {
                    try {
                        mode = bits[0];
                        left = Integer.parseInt(bits[1]);
                        right = Integer.parseInt(bits[2]);
                        top = Integer.parseInt(bits[3]);
                        bottom = Integer.parseInt(bits[4]);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (bits.length >= 3) {
                    try {
                        mode = bits[0];
                        left = Integer.parseInt(bits[1]);
                        right = Integer.parseInt(bits[2]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return new SkinFillOverride(mode, path, left, right, top, bottom);
        }
        return new SkinFillOverride("stretch", encoded);
    }

    public String encode() {
        if ("dynamic".equals(mode) && (leftEdge != 0 || rightEdge != 0 || topEdge != 0 || bottomEdge != 0)) {
            return mode + ":" + leftEdge + ":" + rightEdge + ":" + topEdge + ":" + bottomEdge + SEP + path;
        }
        if (("hrstretch".equals(mode) || "tile".equals(mode) || "tile_size".equals(mode)) && (leftEdge != 0 || rightEdge != 0)) {
            return mode + ":" + leftEdge + ":" + rightEdge + SEP + path;
        }
        return "stretch".equals(mode) ? path : mode + SEP + path;
    }

    public static void clearCache() {
        CACHED.clear();
    }

    public IGuiTexture createTexture() {
        if (path == null || path.isBlank()) return null;
        String cacheKey = mode + ":" + leftEdge + ":" + rightEdge + ":" + topEdge + ":" + bottomEdge + ":" + path;
        IGuiTexture cached = CACHED.get(cacheKey);
        if (cached != null) {
            QuestsAndStuffMod.debugLog("[QnS:Skin] createTexture CACHED: mode={}, path={}, class={}", mode, path, cached.getClass().getSimpleName());
            return cached;
        }

        IGuiTexture tex;
        if ("tile".equals(mode) && (leftEdge != 0 || rightEdge != 0)) {
            tex = createTileSizeTexture();
        } else if ("tile".equals(mode)) {
            tex = createTileTexture();
        } else if ("center".equals(mode)) {
            tex = createCenterTexture();
        } else if ("dynamic".equals(mode)) {
            tex = createDynamicTexture();
        } else if ("hrstretch".equals(mode)) {
            tex = createHorizontalRepeatTexture();
        } else if ("tile_size".equals(mode)) {
            tex = createTileSizeTexture();
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

    private IGuiTexture createTileTexture() {
        ResourceLocation id = AssetLibrary.tileTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        int tileW = dims.width();
        int tileH = dims.height();
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
        return new DynamicClippingTexture(id, dims.width(), dims.height(), leftEdge, rightEdge, topEdge, bottomEdge);
    }

    private IGuiTexture createHorizontalRepeatTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return createFullTexture();
        }
        ResourceLocation id = AssetLibrary.staticTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        return new HorizontalRepeatTexture(id, dims.width(), dims.height(), leftEdge, rightEdge);
    }

    private IGuiTexture createTileSizeTexture() {
        ResourceLocation id = AssetLibrary.tileTextureLocation(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (id == null) return null;
        AssetLibrary.AssetDimensions dims = AssetLibrary.assetDimensions(TabletUiFactory.ASSETS_ROOT_DIR, path);
        if (dims == null) return null;
        int tileW = leftEdge > 0 ? leftEdge : dims.width();
        int tileH = rightEdge > 0 ? rightEdge : dims.height();
        return new TiledGuiTexture(id, tileW, tileH);
    }

    private IGuiTexture createFullTexture() {
        AssetKind kind = AssetLibrary.assetKind(path);
        if (kind == AssetKind.GIF) {
            return AssetLibrary.chapterBackgroundTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
        }
        return AssetLibrary.assetThumbnailTexture(TabletUiFactory.ASSETS_ROOT_DIR, path);
    }
}
