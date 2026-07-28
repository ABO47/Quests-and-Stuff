package com.abo47.questsandstuff.quest.persistence.quest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.abo47.questsandstuff.util.io.JsonFileTree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestDefinitionFilesTest {
    @TempDir
    Path root;

    @Test
    void jsonFilesWalksQuestFoldersRecursively() throws Exception {
        Files.createDirectories(root.resolve("chapter"));
        Path one = root.resolve("chapter").resolve("one.json");
        Path two = root.resolve("two.JSON");
        Files.writeString(one, "{}");
        Files.writeString(two, "{}");
        Files.writeString(root.resolve("ignored.tmp"), "{}");

        assertEquals(Set.of(one, two), Set.copyOf(JsonFileTree.jsonFiles(root)));
    }

    @Test
    void deleteStaleJsonFilesPrunesEmptyParents() throws Exception {
        Path nested = root.resolve("chapter");
        Files.createDirectories(nested);
        Path keep = root.resolve("keep.json").toAbsolutePath().normalize();
        Path stale = nested.resolve("stale.json").toAbsolutePath().normalize();
        Files.writeString(keep, "{}");
        Files.writeString(stale, "{}");

        assertEquals(Set.of(stale), Set.copyOf(JsonFileTree.deleteStaleJsonFiles(root, Set.of(keep), true)));

        assertTrue(Files.exists(keep));
        assertFalse(Files.exists(stale));
        assertFalse(Files.exists(nested));
    }
}
