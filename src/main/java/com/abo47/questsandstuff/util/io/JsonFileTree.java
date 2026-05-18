package com.abo47.questsandstuff.util.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class JsonFileTree {
    private JsonFileTree() {
    }

    public static List<Path> jsonFiles(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (var files = Files.walk(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(JsonFileTree::isJsonFile)
                    .toList();
        }
    }

    public static void writeAtomic(Path target, String contents) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = target.resolveSibling(target.getFileName().toString() + ".tmp");
        Files.writeString(temp, contents, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static List<Path> deleteStaleJsonFiles(Path directory, Set<Path> expected, boolean recursive) throws IOException {
        List<Path> deleted = new ArrayList<>();
        Path root = directory.toAbsolutePath().normalize();
        List<Path> candidates = recursive ? jsonFiles(directory) : directJsonFiles(directory);
        for (Path path : candidates) {
            Path absolute = path.toAbsolutePath().normalize();
            if (expected.contains(absolute)) {
                continue;
            }
            Files.deleteIfExists(absolute);
            deleted.add(absolute);
            if (recursive) {
                pruneEmptyParents(absolute.getParent(), root);
            }
        }
        return deleted;
    }

    public static void pruneEmptyParents(Path start, Path stopAt) throws IOException {
        if (start == null) {
            return;
        }
        Path limit = stopAt.toAbsolutePath().normalize();
        Path current = start.toAbsolutePath().normalize();
        while (current != null && current.startsWith(limit) && !current.equals(limit)) {
            if (!Files.isDirectory(current)) {
                break;
            }
            try (var stream = Files.list(current)) {
                if (stream.findAny().isPresent()) {
                    break;
                }
            }
            Files.deleteIfExists(current);
            current = current.getParent();
        }
    }

    private static List<Path> directJsonFiles(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(JsonFileTree::isJsonFile)
                    .toList();
        }
    }

    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }
}
