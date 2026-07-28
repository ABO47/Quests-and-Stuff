package com.abo47.questsandstuff.client.tablet.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;

final class AssetSearchIndex {
    private AssetSearchIndex() {
    }

    static List<AssetLibrary.AssetEntry> listAssetEntries(Path assetsRoot, String relativeDir) {
        List<AssetLibrary.AssetEntry> result = new ArrayList<>();
        try {
            AssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path dir = AssetPathResolver.resolveDirectory(assetsRoot, relativeDir);
            if (!availableDirectory("asset.list", assetsRoot, relativeDir, dir)) {
                return result;
            }
            String base = AssetPathResolver.normalizeRelative(relativeDir);
            try (Stream<Path> files = Files.list(dir)) {
                files.forEach(path -> addDirectEntry(result, base, path));
            }
            sortByName(result);
        } catch (Exception exception) {
            AssetDiagnostics.debug(
                    "asset.list.failed",
                    "[QnS:UI] asset list failed root={} dir={} error={}",
                    assetsRoot,
                    relativeDir,
                    exception.toString()
            );
        }
        return result;
    }

    static List<AssetLibrary.AssetEntry> searchAssetEntries(Path assetsRoot, String relativeDir, String query) {
        String normalizedQuery = SearchFilter.normalize(query);
        if (normalizedQuery.isBlank()) {
            return listAssetEntries(assetsRoot, relativeDir);
        }
        Map<String, AssetLibrary.AssetEntry> result = new LinkedHashMap<>();
        try {
            AssetPathResolver.ensureAssetsDirs(assetsRoot);
            Path dir = AssetPathResolver.resolveDirectory(assetsRoot, relativeDir);
            if (!availableDirectory("asset.search", assetsRoot, relativeDir, dir)) {
                return List.of();
            }
            String base = AssetPathResolver.normalizeRelative(relativeDir);
            try (Stream<Path> direct = Files.list(dir)) {
                direct.forEach(path -> addMatchingDirectEntry(result, base, path, normalizedQuery));
            }
            try (Stream<Path> nested = Files.walk(dir)) {
                nested.filter(path -> !path.equals(dir))
                        .filter(Files::isRegularFile)
                        .forEach(path -> addMatchingNestedEntry(result, base, dir, path, normalizedQuery));
            }
        } catch (Exception exception) {
            AssetDiagnostics.debug(
                    "asset.search.failed",
                    "[QnS:UI] asset search failed root={} dir={} query={} error={}",
                    assetsRoot,
                    relativeDir,
                    normalizedQuery,
                    exception.toString()
            );
        }
        List<AssetLibrary.AssetEntry> values = new ArrayList<>(result.values());
        sortByRelativePath(values);
        return values;
    }

    private static boolean availableDirectory(String eventPrefix, Path assetsRoot, String relativeDir, Path dir) {
        String key = assetsRoot + "|" + relativeDir;
        if (dir == null) {
            AssetDiagnostics.debugOnce(
                    eventPrefix + ".invalid_dir",
                    key,
                    "[QnS:UI] asset directory skipped root={} dir={} reason=invalid_dir",
                    assetsRoot,
                    relativeDir
            );
            return false;
        }
        if (!Files.exists(dir)) {
            AssetDiagnostics.debugOnce(
                    eventPrefix + ".missing_dir",
                    key,
                    "[QnS:UI] asset directory skipped root={} dir={} resolved={} reason=missing_dir",
                    assetsRoot,
                    relativeDir,
                    dir
            );
            return false;
        }
        if (!Files.isDirectory(dir)) {
            AssetDiagnostics.debugOnce(
                    eventPrefix + ".not_directory",
                    key,
                    "[QnS:UI] asset directory skipped root={} dir={} resolved={} reason=not_directory",
                    assetsRoot,
                    relativeDir,
                    dir
            );
            return false;
        }
        return true;
    }

    private static void addDirectEntry(List<AssetLibrary.AssetEntry> result, String base, Path path) {
        String name = path.getFileName().toString();
        boolean isDir = Files.isDirectory(path);
        if (!isDir && !AssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String rel = base.isBlank() ? name : (base + "/" + name);
        result.add(new AssetLibrary.AssetEntry(name, rel, isDir));
    }

    private static void addMatchingDirectEntry(Map<String, AssetLibrary.AssetEntry> result, String base, Path path, String query) {
        String name = path.getFileName().toString();
        boolean isDir = Files.isDirectory(path);
        if (!isDir && !AssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String rel = base.isBlank() ? name : (base + "/" + name);
        if (SearchFilter.matches(query, rel, name)) {
            result.putIfAbsent(rel, new AssetLibrary.AssetEntry(name, rel, isDir));
        }
    }

    private static void addMatchingNestedEntry(Map<String, AssetLibrary.AssetEntry> result, String base, Path dir, Path path, String query) {
        String name = path.getFileName().toString();
        if (!AssetPathResolver.isSupportedAsset(base, name)) {
            return;
        }
        String relativeFromDir = dir.relativize(path).toString().replace('\\', '/');
        String rel = base.isBlank() ? relativeFromDir : (base + "/" + relativeFromDir);
        if (SearchFilter.matches(query, rel, name)) {
            result.putIfAbsent(rel, new AssetLibrary.AssetEntry(name, rel, false));
        }
    }

    private static void sortByName(List<AssetLibrary.AssetEntry> result) {
        result.sort((a, b) -> {
            if (a.directory() != b.directory()) {
                return a.directory() ? -1 : 1;
            }
            return a.name().compareToIgnoreCase(b.name());
        });
    }

    private static void sortByRelativePath(List<AssetLibrary.AssetEntry> result) {
        result.sort((a, b) -> {
            if (a.directory() != b.directory()) {
                return a.directory() ? -1 : 1;
            }
            return a.relativePath().compareToIgnoreCase(b.relativePath());
        });
    }
}
