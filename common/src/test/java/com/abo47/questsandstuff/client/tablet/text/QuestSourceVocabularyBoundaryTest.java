package com.abo47.questsandstuff.client.tablet.text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestSourceVocabularyBoundaryTest {
    private static final List<Path> ROOTS = List.of(
            Path.of("src/main/java"),
            Path.of("src/test/java"),
            Path.of("src/main/resources"),
            Path.of("art/source-icons"),
            Path.of(".agents/skills")
    );
    private static final String OLD_UNLOCK_EDGE = "dependenc";
    private static final String OLD_MEDIA_NODE = "pictur";
    private static final String OLD_EDGE_VERB = "lin";
    private static final Set<String> RETIRED_SOURCE_TERMS = Set.of(
            OLD_UNLOCK_EDGE + "y",
            OLD_UNLOCK_EDGE + "ies",
            OLD_MEDIA_NODE + "e",
            OLD_MEDIA_NODE + "es",
            OLD_EDGE_VERB + "k",
            OLD_EDGE_VERB + "ks",
            "un" + OLD_EDGE_VERB + "k",
            "un" + OLD_EDGE_VERB + "ks"
    );
    private static final Set<String> ALLOWED_FILES = Set.of(
            normalized("src/main/resources/META-INF/mods.toml")
    );
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            ".gitkeep",
            ".java",
            ".json",
            ".mcmeta",
            ".md",
            ".snbt",
            ".svg",
            ".toml",
            ".txt",
            ".yaml",
            ".yml"
    );

    @Test
    void retiredSourceTermsStayInsideApprovedBoundaries() throws Exception {
        Set<String> drift = new LinkedHashSet<>();
        for (Path root : ROOTS) {
            if (!Files.exists(root)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path file : paths.filter(Files::isRegularFile).filter(QuestSourceVocabularyBoundaryTest::isTextFile).toList()) {
                    if (allowed(file)) {
                        continue;
                    }
                    scan(file, drift);
                }
            }
        }
        assertTrue(
                drift.isEmpty(),
                "Retired source vocabulary escaped the approved metadata boundary: " + drift
        );
    }

    private static void scan(Path file, Set<String> drift) throws Exception {
        String text = Files.readString(file).toLowerCase(Locale.ROOT);
        String[] tokens = text.split("[^a-z]+");
        for (String token : tokens) {
            if (RETIRED_SOURCE_TERMS.contains(token)) {
                drift.add(file + " uses '" + token + "'");
            }
        }
    }

    private static boolean allowed(Path file) {
        return ALLOWED_FILES.contains(normalized(file.toString()));
    }

    private static boolean isTextFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String extension : TEXT_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static String normalized(String value) {
        return value.replace('\\', '/');
    }
}
