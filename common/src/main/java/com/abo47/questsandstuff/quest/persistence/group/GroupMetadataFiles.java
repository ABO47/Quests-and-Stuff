package com.abo47.questsandstuff.quest.persistence.group;

import com.abo47.questsandstuff.util.io.JsonFileTree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

final class GroupMetadataFiles {
    private GroupMetadataFiles() {
    }

    static List<Path> jsonFiles(Path directory) throws IOException {
        return JsonFileTree.jsonFiles(directory);
    }

    static void writeAtomic(Path target, String contents) throws IOException {
        JsonFileTree.writeAtomic(target, contents);
    }

    static List<Path> deleteStaleJsonFiles(Path directory, Set<Path> expected) throws IOException {
        return JsonFileTree.deleteStaleJsonFiles(directory, expected, false);
    }
}
