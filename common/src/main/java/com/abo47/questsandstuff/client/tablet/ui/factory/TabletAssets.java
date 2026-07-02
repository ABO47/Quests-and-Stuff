package com.abo47.questsandstuff.client.tablet.ui.factory;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.assets.AssetLibrary;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.icons.UiIconRegistry;
import com.abo47.questsandstuff.client.tablet.modal.RecipeChoiceIndex;
import com.abo47.questsandstuff.client.tablet.quest.canvas.recipe.CanvasRecipeCardRecipes;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletLayout;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

final class TabletAssets {
    private TabletAssets() {
    }

    static int chapterBackgroundFill(String background, int fallback) {
        return switch (background == null ? "" : background) {
            case "hexagons" -> withAlpha(ModColors.INTERACTIVE, 86);
            case "octagons" -> withAlpha(ModColors.WARNING, 86);
            case "circles" -> withAlpha(ModColors.SUCCESS, 86);
            case "diamonds" -> withAlpha(ModColors.TEXT_SECONDARY, 86);
            case "gears" -> withAlpha(ModColors.BORDER_ACCENT, 92);
            case "hearts" -> withAlpha(ModColors.ERROR, 74);
            case "pentagons" -> withAlpha(ModColors.INTERACTIVE, 108);
            case "rounded_squares" -> withAlpha(ModColors.SURFACE_PANEL_ALT, 120);
            default -> {
                if (background != null && !background.isBlank() && !"default".equals(background) && chapterBackgroundTexture(background) != null) {
                    yield withAlpha(ModColors.SURFACE_PANEL_ALT, 90);
                }
                yield fallback;
            }
        };
    }

    static IGuiTexture chapterBackgroundTexture(String background) {
        return AssetLibrary.chapterBackgroundTexture(TabletLayout.ASSETS_ROOT_DIR, background);
    }

    static IGuiTexture chapterBackgroundTexture(String background, boolean grayscale) {
        return AssetLibrary.chapterBackgroundTexture(TabletLayout.ASSETS_ROOT_DIR, background, grayscale);
    }

    static ItemStackTexture iconTexture(String iconId) {
        return DisplayIconProvider.iconTexture(iconId);
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
        TabletUiPerfProfiler.profile("ui.prewarm.icons", () -> UiIconAtlas.prewarm(UiIconRegistry.preloadKeys()));
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
        TabletUiPerfProfiler.profile("ui.prewarm.assetThumbnails", TabletAssets::prewarmAssetThumbnails);
        TabletUiPerfProfiler.profile("ui.prewarm.modLogo", TabletAssets::prewarmModLogo);
        TabletUiPerfProfiler.profile("ui.prewarm.fluidEntries", () -> DisplayIconProvider.prewarmFluidEntries());
        TabletUiPerfProfiler.profile("ui.prewarm.entityPreviews", () -> EntityPreviewRenderer.prewarmEntityCache());
        TabletUiPerfProfiler.profile("ui.prewarm.recipeIndex", () -> RecipeChoiceIndex.prewarm());
        TabletUiPerfProfiler.profile("ui.prewarm.recipeCards", () -> CanvasRecipeCardRecipes.prewarm());
    }

    private static void prewarmAssetThumbnails() {
        Path root = TabletLayout.ASSETS_ROOT_DIR;
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                Path relative = root.relativize(path);
                String rel = relative.toString().replace('\\', '/');
                if (AssetLibrary.assetKind(rel).hasImageThumbnail()) {
                    assetThumbnailTexture(rel);
                }
            });
        } catch (Exception e) {
            QuestsAndStuffMod.debugLog("[QnS:UI] failed to prewarm asset thumbnails");
        }
    }

    private static void prewarmModLogo() {
        try (InputStream is = TabletAssets.class.getClassLoader().getResourceAsStream("questsandstuff.png")) {
            if (is != null) {
                NativeImage image = NativeImage.read(is);
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation id = new ResourceLocation("questsandstuff", "textures/gui/questsandstuff.png");
                Minecraft.getInstance().getTextureManager().register(id, texture);
            }
        } catch (Exception e) {
            QuestsAndStuffMod.debugLog("[QnS:UI] failed to prewarm mod logo texture");
        }
    }
}
