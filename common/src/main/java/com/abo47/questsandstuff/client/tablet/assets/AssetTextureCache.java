package com.abo47.questsandstuff.client.tablet.assets;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class AssetTextureCache {
    private static final Map<String, IGuiTexture> TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, IGuiTexture> THUMBNAIL_CACHE = new HashMap<>();

    private AssetTextureCache() {
    }

    static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background) {
        return chapterBackgroundTexture(assetsRoot, background, false);
    }

    static IGuiTexture chapterBackgroundTexture(Path assetsRoot, String background, boolean grayscale) {
        if (background == null || background.isBlank() || "default".equals(background)) {
            return null;
        }
        AssetPathResolver.ensureAssetsDirs(assetsRoot);
        String cacheKey = textureCacheKey(background, grayscale);
        IGuiTexture cached = TEXTURE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Path path = AssetPathResolver.resolveAssetPath(assetsRoot, background);
        if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
            return null;
        }
        String ext = AssetPathResolver.extension(path.getFileName().toString());
        if (!AssetPathResolver.hasImageThumbnail(background)) {
            return null;
        }
        IGuiTexture out = "gif".equals(ext) ? loadGifTexture(path, cacheKey, grayscale) : loadStaticTexture(path, cacheKey, grayscale);
        if (out != null) {
            TEXTURE_CACHE.put(cacheKey, out);
        }
        return out;
    }

    static AssetLibrary.AssetDimensions assetDimensions(Path assetsRoot, String relativePath) {
        try {
            AssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path path = AssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
            if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
                return null;
            }
            String ext = AssetPathResolver.extension(path.getFileName().toString());
            if (!AssetPathResolver.hasImageThumbnail(relativePath)) {
                return null;
            }
            if ("gif".equals(ext)) {
                AssetLibrary.AssetDimensions gifDimensions = gifDimensions(path);
                if (gifDimensions != null) {
                    return gifDimensions;
                }
            }
            try (var stream = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(stream);
                if (image != null) {
                    return new AssetLibrary.AssetDimensions(image.getWidth(), image.getHeight());
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    static IGuiTexture assetThumbnailTexture(Path assetsRoot, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        AssetPathResolver.ensureAssetsDirs(assetsRoot);
        IGuiTexture cached = THUMBNAIL_CACHE.get(relativePath);
        if (cached != null) {
            return cached;
        }
        Path path = AssetPathResolver.resolveAssetPath(assetsRoot, relativePath);
        if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
            return null;
        }
        String ext = AssetPathResolver.extension(path.getFileName().toString());
        if (!AssetPathResolver.hasImageThumbnail(relativePath)) {
            return null;
        }
        IGuiTexture out = "gif".equals(ext) ? loadGifFallbackStatic(path, relativePath + "_thumb", false) : loadStaticTexture(path, relativePath + "_thumb", false);
        if (out != null) {
            THUMBNAIL_CACHE.put(relativePath, out);
        }
        return out;
    }

    static void clearTextureCache(String key) {
        TEXTURE_CACHE.remove(key);
        TEXTURE_CACHE.remove(textureCacheKey(key, true));
        THUMBNAIL_CACHE.remove(key);
    }

    private static AssetLibrary.AssetDimensions gifDimensions(Path path) {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(input, false, false);
                BufferedImage first = reader.read(0);
                if (first != null) {
                    return new AssetLibrary.AssetDimensions(first.getWidth(), first.getHeight());
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static IGuiTexture loadStaticTexture(Path path, String key, boolean grayscale) {
        try (var stream = Files.newInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            if (image == null) {
                return null;
            }
            if (grayscale) {
                applyGrayscale(image);
            }
            ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "chapter_asset/" + AssetPathResolver.sanitizeAssetId(key));
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            return new DynamicTexture(() -> new ResourceTexture(id));
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed loading texture {}", key, e);
            return null;
        }
    }

    private static IGuiTexture loadGifTexture(Path path, String key, boolean grayscale) {
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            reader.setInput(input, false, false);
            int count = reader.getNumImages(true);
            if (count <= 0) {
                return null;
            }
            List<ResourceTexture> frames = new ArrayList<>();
            List<Integer> delaysMs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                addGifFrame(reader, key, i, frames, delaysMs, grayscale);
            }
            if (frames.isEmpty()) {
                return null;
            }
            return new DynamicTexture(() -> frameAtCurrentTime(frames, delaysMs));
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed loading gif {}", key, e);
            return loadGifFallbackStatic(path, key, grayscale);
        }
    }

    private static void addGifFrame(ImageReader reader, String key, int index, List<ResourceTexture> frames, List<Integer> delaysMs, boolean grayscale) throws java.io.IOException {
        BufferedImage frame = reader.read(index);
        if (frame == null) {
            return;
        }
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        ImageIO.write(frame, "png", pngOut);
        try (var frameIn = new java.io.ByteArrayInputStream(pngOut.toByteArray())) {
            NativeImage image = NativeImage.read(frameIn);
            if (image == null) {
                return;
            }
            if (grayscale) {
                applyGrayscale(image);
            }
            ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "chapter_asset/" + AssetPathResolver.sanitizeAssetId(key + "_f" + index));
            Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            frames.add(new ResourceTexture(id));
            delaysMs.add(Math.max(40, gifDelayMs(reader, index)));
        }
    }

    private static ResourceTexture frameAtCurrentTime(List<ResourceTexture> frames, List<Integer> delaysMs) {
        long loop = 0L;
        for (Integer delay : delaysMs) {
            loop += delay;
        }
        if (loop <= 0L) {
            return frames.get(0);
        }
        long phase = System.currentTimeMillis() % loop;
        long elapsed = 0L;
        for (int i = 0; i < frames.size(); i++) {
            elapsed += delaysMs.get(i);
            if (phase < elapsed) {
                return frames.get(i);
            }
        }
        return frames.get(frames.size() - 1);
    }

    private static String textureCacheKey(String background, boolean grayscale) {
        return grayscale ? background + "#grayscale" : background;
    }

    private static void applyGrayscale(NativeImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixelRGBA(x, y);
                int red = color & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = (color >>> 16) & 0xFF;
                int alpha = (color >>> 24) & 0xFF;
                int gray = Math.round(red * 0.299f + green * 0.587f + blue * 0.114f);
                int grayscale = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                image.setPixelRGBA(x, y, grayscale);
            }
        }
    }

    private static IGuiTexture loadGifFallbackStatic(Path path, String key, boolean grayscale) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                return null;
            }
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOut);
            try (var frameIn = new java.io.ByteArrayInputStream(pngOut.toByteArray())) {
                NativeImage nativeImage = NativeImage.read(frameIn);
                if (nativeImage == null) {
                    return null;
                }
                if (grayscale) {
                    applyGrayscale(nativeImage);
                }
                ResourceLocation id = ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "chapter_asset/" + AssetPathResolver.sanitizeAssetId(key + "_fallback"));
                Minecraft.getInstance().getTextureManager().register(id, new net.minecraft.client.renderer.texture.DynamicTexture(nativeImage));
                return new DynamicTexture(() -> new ResourceTexture(id));
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int gifDelayMs(ImageReader reader, int index) {
        try {
            var metadata = reader.getImageMetadata(index);
            String format = metadata.getNativeMetadataFormatName();
            var root = metadata.getAsTree(format);
            var children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                var node = children.item(i);
                if (!"GraphicControlExtension".equals(node.getNodeName())) {
                    continue;
                }
                var attrs = node.getAttributes();
                var delayNode = attrs.getNamedItem("delayTime");
                if (delayNode == null) {
                    return 100;
                }
                int cs = Integer.parseInt(delayNode.getNodeValue());
                return Math.max(10, cs) * 10;
            }
        } catch (Exception ignored) {
        }
        return 100;
    }
}
