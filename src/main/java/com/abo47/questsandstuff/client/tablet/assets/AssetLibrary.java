package com.abo47.questsandstuff.client.tablet.assets;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.nio.file.Path;
import java.util.List;

public final class AssetLibrary {
    private AssetLibrary() {
    }

    public record AssetEntry(String name, String relativePath, boolean directory) {
    }

    public record AssetDimensions(int width, int height) {
    }

    public static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background) {
        return AssetTextureCache.chapterBackgroundTexture(assetsRoot, background);
    }

    public static List<AssetEntry> listAssetEntries(Path assetsRoot, String relativeDir) {
        return AssetSearchIndex.listAssetEntries(assetsRoot, relativeDir);
    }

    public static List<AssetEntry> searchAssetEntries(Path assetsRoot, String relativeDir, String query) {
        return AssetSearchIndex.searchAssetEntries(assetsRoot, relativeDir, query);
    }

    public static AssetDimensions assetDimensions(Path assetsRoot, String relativePath) {
        return AssetTextureCache.assetDimensions(assetsRoot, relativePath);
    }

    public static IGuiTexture assetThumbnailTexture(Path assetsRoot, String relativePath) {
        return AssetTextureCache.assetThumbnailTexture(assetsRoot, relativePath);
    }

    public static void ensureAssetsDirs(Path assetsRoot) {
        AssetPathResolver.ensureAssetsDirs(assetsRoot);
    }

    public static void deleteAssetFile(Path assetsRoot, String relativePath) {
        AssetPathResolver.deleteAssetFile(assetsRoot, relativePath, AssetTextureCache::clearTextureCache);
    }

    public static void renameAssetFile(Path assetsRoot, String relativePath, String targetNameRaw) {
        AssetPathResolver.renameAssetFile(assetsRoot, relativePath, targetNameRaw, AssetTextureCache::clearTextureCache);
    }
}
