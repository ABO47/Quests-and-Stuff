package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.icons.QuestIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.screen.TabletUiPerfProfiler;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class TabletAssets {
    private TabletAssets() {
    }

    static int chapterBackgroundFill(String background, int fallback) {
        return switch (background == null ? "" : background) {
            case "hexagons" -> TabletWidgets.withAlpha(ModColors.INTERACTIVE, 86);
            case "octagons" -> TabletWidgets.withAlpha(ModColors.WARNING, 86);
            case "circles" -> TabletWidgets.withAlpha(ModColors.SUCCESS, 86);
            case "diamonds" -> TabletWidgets.withAlpha(ModColors.TEXT_SECONDARY, 86);
            case "gears" -> TabletWidgets.withAlpha(ModColors.BORDER_ACCENT, 92);
            case "hearts" -> TabletWidgets.withAlpha(ModColors.ERROR, 74);
            case "pentagons" -> TabletWidgets.withAlpha(ModColors.INTERACTIVE, 108);
            case "rounded_squares" -> TabletWidgets.withAlpha(ModColors.SURFACE_PANEL_ALT, 120);
            default -> {
                if (background != null && !background.isBlank() && !"default".equals(background) && chapterBackgroundTexture(background) != null) {
                    yield TabletWidgets.withAlpha(ModColors.SURFACE_PANEL_ALT, 90);
                }
                yield fallback;
            }
        };
    }

    static IGuiTexture chapterBackgroundTexture(String background) {
        return AssetLibrary.chapterBackgroundTexture(TabletLayout.ASSETS_ROOT_DIR, background);
    }

    static ItemStackTexture iconTexture(String iconId) {
        return QuestIconProvider.iconTexture(iconId);
    }

    static List<AssetLibrary.AssetEntry> listAssetEntries(String relativeDir) {
        return AssetLibrary.listAssetEntries(TabletLayout.ASSETS_ROOT_DIR, relativeDir);
    }

    static List<AssetLibrary.AssetEntry> searchAssetEntries(String relativeDir, String query) {
        return AssetLibrary.searchAssetEntries(TabletLayout.ASSETS_ROOT_DIR, relativeDir, query);
    }

    static AssetLibrary.AssetDimensions assetDimensions(String relativePath) {
        return AssetLibrary.assetDimensions(TabletLayout.ASSETS_ROOT_DIR, relativePath);
    }

    static IGuiTexture assetThumbnailTexture(String relativePath) {
        return AssetLibrary.assetThumbnailTexture(TabletLayout.ASSETS_ROOT_DIR, relativePath);
    }

    static void ensureAssetsDirs() {
        AssetLibrary.ensureAssetsDirs(TabletLayout.ASSETS_ROOT_DIR);
    }

    static void deleteAssetFile(String relativePath) {
        AssetLibrary.deleteAssetFile(TabletLayout.ASSETS_ROOT_DIR, relativePath);
    }

    static void renameAssetFile(String relativePath, String targetNameRaw) {
        AssetLibrary.renameAssetFile(TabletLayout.ASSETS_ROOT_DIR, relativePath, targetNameRaw);
    }

    static void prewarmClientUiAssets() {
        TabletUiPerfProfiler.profile("ui.prewarm.assetsDirs", TabletAssets::ensureAssetsDirs);
        TabletUiPerfProfiler.profile("ui.prewarm.icons", () -> UiIconAtlas.prewarm(
                "tools", "grid", "editor", "align-center-horizontal", "align-center-vertical", "objects", "entity", "close", "search", "add", "rename", "delete",
                "copy", "paste", "connect", "settings-2", "stat", "recipe", "item_use", "item_interact",
                "icon", "image", "background", "style", "up", "down", "back", "chevron-right", "open", "context_open",
                "size", "opacity", "magnet", "lock", "unlock",
                "background_opacity", "reset_zoom", "reset_quest", "repeat", "repeat-off", "variant", "motion", "properties", "minimap",
                "style_align_left", "style_align_center", "style_align_right",
                "style_bold", "style_italic", "style_color", "themes", "claim_all", "xp", "send-horizontal", "eye", "eye-off", "audio-lines", "play", "pause", "reset"
        ));
        TabletUiPerfProfiler.profile("ui.prewarm.chapterBackgrounds", () -> {
            Set<String> backgrounds = new HashSet<>();
            for (String group : ClientQuestCache.groupOrder()) {
                String background = ClientQuestCache.groupBackground(group);
                if (background == null || background.isBlank() || "default".equals(background)) {
                    continue;
                }
                backgrounds.add(background);
            }
            for (String background : backgrounds) {
                chapterBackgroundTexture(background);
            }
        });
    }
}
