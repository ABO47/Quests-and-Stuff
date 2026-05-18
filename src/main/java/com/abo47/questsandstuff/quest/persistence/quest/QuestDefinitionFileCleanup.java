package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            QuestDefinitionFiles.pruneEmptyParents(removedPath.getParent(), questsDir.toAbsolutePath().normalize());
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

            QuestDefinitionFiles.deleteStaleJsonFiles(questsDir, expected);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed stale quest cleanup", e);
        }
    }
}
