package com.abo47.questsandstuff.client.tablet.assets;

import com.abo47.questsandstuff.QuestsAndStuffMod;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

final class AssetPathResolver {
    private AssetPathResolver() {
    }

    static void ensureAssetsDirs(Path assetsRoot) {
        try {
            Files.createDirectories(assetsRoot);
            Files.createDirectories(assetsRoot.resolve("pics"));
            Files.createDirectories(assetsRoot.resolve("gifs"));
            Files.createDirectories(assetsRoot.resolve("sounds"));
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed creating assets dirs {}", assetsRoot, e);
        }
    }

    static Path resolveAssetPath(Path assetsRoot, String relativePath) {
        if (containsTraversal(relativePath) || containsIllegalPathCharacter(relativePath)) {
            return null;
        }
        String rel = normalizeRelative(relativePath);
        if (rel.isBlank()) {
            return null;
        }
        Path root = assetsRoot.normalize();
        Path direct = resolveSafe(assetsRoot, rel);
        if (direct == null) {
            return null;
        }
        if (!direct.startsWith(root)) {
            return null;
        }
        if (Files.exists(direct)) {
            return direct;
        }
        if (!rel.contains("/")) {
            Path imageFallback = resolveSafe(assetsRoot.resolve("pics"), rel);
            if (imageFallback != null && imageFallback.startsWith(root)) {
                return imageFallback;
            }
        }
        return direct;
    }

    static Path resolveDirectory(Path assetsRoot, String relativeDir) {
        if (containsTraversal(relativeDir) || containsIllegalPathCharacter(relativeDir)) {
            return null;
        }
        String rel = normalizeRelative(relativeDir);
        Path root = assetsRoot.normalize();
        Path dir = rel.isBlank() ? root : resolveSafe(assetsRoot, rel);
        if (dir == null) {
            return null;
        }
        return dir.startsWith(root) ? dir : null;
    }

    static String normalizeRelative(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (containsTraversal(normalized)) {
            return "";
        }
        if (containsIllegalPathCharacter(normalized)) {
            return "";
        }
        return normalized;
    }

    private static Path resolveSafe(Path root, String relativePath) {
        try {
            return root.resolve(relativePath).normalize();
        } catch (InvalidPathException ignored) {
            return null;
        }
    }

    private static boolean containsTraversal(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.replace('\\', '/');
        for (String part : normalized.split("/")) {
            if ("..".equals(part)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIllegalPathCharacter(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 32 || c == '<' || c == '>' || c == ':' || c == '"' || c == '|' || c == '?' || c == '*') {
                return true;
            }
        }
        return false;
    }

    static boolean isSupportedAsset(String relativeDir, String fileName) {
        String dir = normalizeRelative(relativeDir);
        if (dir.equals("sounds") || dir.startsWith("sounds/")) {
            return isSupportedSound(fileName);
        }
        return isSupportedImage(fileName);
    }

    static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot + 1 >= fileName.length()) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    static String parentRelative(String value) {
        int idx = value.lastIndexOf('/');
        return idx < 0 ? "" : value.substring(0, idx);
    }

    static String sanitizeAssetId(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }

    static void deleteAssetFile(Path assetsRoot, String relativePath, Consumer<String> clearCache) {
        try {
            ensureAssetsDirs(assetsRoot);
            Path target = resolveAssetPath(assetsRoot, relativePath);
            if (target != null && Files.exists(target) && Files.isRegularFile(target)) {
                Files.deleteIfExists(target);
                clearCache.accept(relativePath);
            }
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed deleting asset {}", relativePath, e);
        }
    }

    static void renameAssetFile(Path assetsRoot, String relativePath, String targetNameRaw, Consumer<String> clearCache) {
        try {
            ensureAssetsDirs(assetsRoot);
            Path source = resolveAssetPath(assetsRoot, relativePath);
            if (source == null || !Files.exists(source) || !Files.isRegularFile(source)) {
                return;
            }
            String nextName = targetNameRaw == null ? "" : targetNameRaw.trim();
            if (nextName.isBlank() || nextName.contains("/") || nextName.contains("\\")) {
                return;
            }
            if (!nextName.contains(".")) {
                int dot = source.getFileName().toString().lastIndexOf('.');
                if (dot > 0) {
                    nextName = nextName + source.getFileName().toString().substring(dot);
                }
            }
            Path target = source.resolveSibling(nextName).normalize();
            if (!target.startsWith(assetsRoot.normalize())) {
                return;
            }
            Files.move(source, target);
            clearCache.accept(relativePath);
            String base = parentRelative(normalizeRelative(relativePath));
            clearCache.accept(base.isBlank() ? nextName : base + "/" + nextName);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed renaming asset {}", relativePath, e);
        }
    }

    private static boolean isSupportedImage(String fileName) {
        String ext = extension(fileName);
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "webp".equals(ext) || "gif".equals(ext);
    }

    private static boolean isSupportedSound(String fileName) {
        String ext = extension(fileName);
        return "ogg".equals(ext) || "wav".equals(ext);
    }
}
