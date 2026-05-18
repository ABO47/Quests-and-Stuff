package com.abo47.questsandstuff.client.tablet.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
