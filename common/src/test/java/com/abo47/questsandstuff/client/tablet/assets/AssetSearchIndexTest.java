package com.abo47.questsandstuff.client.tablet.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetSearchIndexTest {
    @TempDir
    Path root;

    @Test
    void searchDedupesDirectFilesSeenByNestedWalk() throws Exception {
        Path pics = root.resolve("pics");
        Files.createDirectories(pics);
        Files.writeString(pics.resolve("lost_city.png"), "fake");

        List<AssetLibrary.AssetEntry> entries = AssetLibrary.searchAssetEntries(root, "pics", "lost city");

        assertEquals(1, entries.stream().filter(entry -> entry.relativePath().equals("pics/lost_city.png")).count());
    }

    @Test
    void listSortsDirectoriesBeforeFilesAndFiltersUnsupportedFiles() throws Exception {
        Path pics = root.resolve("pics");
        Files.createDirectories(pics.resolve("nested"));
        Files.writeString(pics.resolve("b.txt"), "ignored");
        Files.writeString(pics.resolve("a.png"), "fake");

        List<AssetLibrary.AssetEntry> entries = AssetLibrary.listAssetEntries(root, "pics");

        assertEquals("nested", entries.get(0).name());
        assertTrue(entries.get(0).directory());
        assertEquals(List.of("nested", "a.png"), entries.stream().map(AssetLibrary.AssetEntry::name).toList());
    }

    @Test
    void listLogsInvalidRelativeDirectories() throws Exception {
        List<AssetDiagnostics.Event> events = new ArrayList<>();

        try (AutoCloseable ignored = AssetDiagnostics.capture(events::add)) {
            assertTrue(AssetLibrary.listAssetEntries(root, "../outside").isEmpty());
        }

        assertDiagnostic(events, "asset.list.invalid_dir");
    }

    @Test
    void listLogsFailedAssetRootPreparation() throws Exception {
        Path rootFile = root.resolve("assets-root-file");
        Files.writeString(rootFile, "not a directory");
        List<AssetDiagnostics.Event> events = new ArrayList<>();

        try (AutoCloseable ignored = AssetDiagnostics.capture(events::add)) {
            assertTrue(AssetLibrary.listAssetEntries(rootFile, "pics").isEmpty());
        }

        assertDiagnostic(events, "asset.root.failed");
    }

    @Test
    void dimensionsLogDecodeFailures() throws Exception {
        Path pics = root.resolve("pics");
        Files.createDirectories(pics);
        Files.writeString(pics.resolve("broken.png"), "not an image");
        List<AssetDiagnostics.Event> events = new ArrayList<>();

        try (AutoCloseable ignored = AssetDiagnostics.capture(events::add)) {
            assertNull(AssetLibrary.assetDimensions(root, "pics/broken.png"));
        }

        assertDiagnostic(events, "asset.dimensions.failed");
    }

    private static void assertDiagnostic(List<AssetDiagnostics.Event> events, String event) {
        assertTrue(
                events.stream().anyMatch(diagnostic -> event.equals(diagnostic.event())),
                () -> "Expected diagnostic " + event + " but got " + events
        );
    }
}
