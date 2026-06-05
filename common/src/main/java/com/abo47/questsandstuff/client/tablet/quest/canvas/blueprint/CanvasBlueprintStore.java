package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.util.SafeNames;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CanvasBlueprintStore {
    public static final String BLUEPRINTS_DIR = "blueprints";

    private static String cachedRelative = "";
    private static long cachedModified = Long.MIN_VALUE;
    private static CanvasBlueprint cachedBlueprint = CanvasBlueprint.empty();

    private CanvasBlueprintStore() {
    }

    public static CanvasBlueprint read(String relativePath) {
        Path path = resolve(relativePath);
        if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
            return CanvasBlueprint.empty();
        }
        try {
            long modified = Files.getLastModifiedTime(path).toMillis();
            String relative = normalizeBlueprintPath(relativePath);
            if (relative.equals(cachedRelative) && modified == cachedModified) {
                return cachedBlueprint;
            }
            CanvasBlueprint blueprint = CanvasBlueprint.fromJson(Files.readString(path));
            cachedRelative = relative;
            cachedModified = modified;
            cachedBlueprint = blueprint;
            return blueprint;
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed reading blueprint {}", relativePath, e);
            return CanvasBlueprint.empty();
        }
    }

    public static String save(CanvasBlueprint blueprint, String preferredName) {
        if (blueprint == null || blueprint.isEmpty()) {
            return "";
        }
        try {
            TabletUiFactory.ensureAssetsDirs();
            Path dir = TabletUiFactory.ASSETS_ROOT_DIR.resolve(BLUEPRINTS_DIR);
            Files.createDirectories(dir);
            String stem = SafeNames.fileStem(preferredName, "blueprint");
            Path target = uniquePath(dir, stem);
            Files.writeString(target, blueprint.toJson());
            String relative = BLUEPRINTS_DIR + "/" + target.getFileName();
            cachedRelative = "";
            cachedModified = Long.MIN_VALUE;
            cachedBlueprint = CanvasBlueprint.empty();
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] saved path={} quests={} images={} texts={}",
                    relative, blueprint.quests().size(), blueprint.images().size(), blueprint.texts().size());
            return relative;
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("[QnS:UI] Failed saving blueprint {}", preferredName, e);
            return "";
        }
    }

    public static boolean isBlueprint(String relativePath) {
        String normalized = normalizeBlueprintPath(relativePath);
        return normalized.startsWith(BLUEPRINTS_DIR + "/") && normalized.toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private static Path resolve(String relativePath) {
        String normalized = normalizeBlueprintPath(relativePath);
        if (!isBlueprint(normalized)) {
            return null;
        }
        Path root = TabletUiFactory.ASSETS_ROOT_DIR.normalize();
        Path path = root.resolve(normalized).normalize();
        return path.startsWith(root) ? path : null;
    }

    private static String normalizeBlueprintPath(String relativePath) {
        if (relativePath == null) {
            return "";
        }
        String normalized = relativePath.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static Path uniquePath(Path dir, String stem) {
        Path candidate = dir.resolve(stem + ".json");
        int index = 2;
        while (Files.exists(candidate)) {
            candidate = dir.resolve(stem + "_" + index + ".json");
            index++;
        }
        return candidate;
    }
}
