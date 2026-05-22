package com.abo47.questsandstuff.quest.persistence.chapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterMetadataFilesTest {
    @TempDir
    Path root;

    @Test
    void jsonFilesWalksNestedMetadataFilesOnly() throws Exception {
        Files.createDirectories(root.resolve("nested"));
        Path one = root.resolve("one.json");
        Path two = root.resolve("nested").resolve("two.JSON");
        Files.writeString(one, "{}");
        Files.writeString(two, "{}");
        Files.writeString(root.resolve("notes.txt"), "ignored");

        Set<Path> found = Set.copyOf(ChapterMetadataFiles.jsonFiles(root));

        assertEquals(Set.of(one, two), found);
    }

    @Test
    void writeAtomicReplacesExistingContent() throws Exception {
        Path file = root.resolve("chapter.json");
        Files.writeString(file, "old");

        ChapterMetadataFiles.writeAtomic(file, "new");

        assertEquals("new", Files.readString(file));
        assertFalse(Files.exists(root.resolve("chapter.json.tmp")));
    }

    @Test
    void deleteStaleJsonFilesKeepsExpectedFiles() throws Exception {
        Path keep = root.resolve("keep.json").toAbsolutePath().normalize();
        Path stale = root.resolve("stale.json").toAbsolutePath().normalize();
        Files.writeString(keep, "{}");
        Files.writeString(stale, "{}");

        assertEquals(Set.of(stale), Set.copyOf(ChapterMetadataFiles.deleteStaleJsonFiles(root, Set.of(keep))));

        assertTrue(Files.exists(keep));
        assertFalse(Files.exists(stale));
    }
}
