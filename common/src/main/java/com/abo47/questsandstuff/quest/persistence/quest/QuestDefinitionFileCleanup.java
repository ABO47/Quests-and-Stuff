package com.abo47.questsandstuff.quest.persistence.quest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.util.io.JsonFileTree;

final class QuestDefinitionFileCleanup {
    private QuestDefinitionFileCleanup() {
    }

    static void deleteQuestFile(Path questsDir, String questId, QuestDefinition definition) {
        try {
            if (definition == null) {
                return;
            }
            Path removedPath = QuestDefinitionPaths.questPath(questsDir, definition);
            Files.deleteIfExists(removedPath);
            JsonFileTree.pruneEmptyParents(removedPath.getParent(), questsDir.toAbsolutePath().normalize());
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed deleting quest file {}", questId, e);
        }
    }

    static void cleanupStaleQuestFiles(Path questsDir, Map<String, QuestDefinition> quests) {
        try {
            Files.createDirectories(questsDir);
            Set<Path> expected = new HashSet<>();
            for (QuestDefinition definition : quests.values()) {
                if (definition != null) {
                    expected.add(QuestDefinitionPaths.questPath(questsDir, definition).toAbsolutePath().normalize());
                }
            }

            JsonFileTree.deleteStaleJsonFiles(questsDir, expected, true);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed stale quest cleanup", e);
        }
    }
}
