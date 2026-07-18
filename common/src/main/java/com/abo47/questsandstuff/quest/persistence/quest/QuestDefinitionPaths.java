package com.abo47.questsandstuff.quest.persistence.quest;

import java.io.IOException;
import java.nio.file.Path;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.util.naming.QuestNaming;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.chapterFolderName;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.primaryChapter;

final class QuestDefinitionPaths {
    private QuestDefinitionPaths() {
    }

    static Path questPath(Path questsDir, QuestDefinition definition) throws IOException {
        String canonicalId = normalizeQuestId(definition.id());
        String chapter = primaryChapter(definition);
        String managedFileName = QuestNaming.managedQuestFileName(canonicalId, chapter);
        Path relative;
        if (!managedFileName.isBlank()) {
            relative = Path.of(QuestNaming.chapterFolderName(chapter)).resolve(managedFileName);
        } else {
            String slug = canonicalId.replace('/', '_');
            relative = Path.of(chapterFolderName(chapter)).resolve(slug + ".json");
        }
        if (relative.isAbsolute()) {
            throw new IOException("Quest id cannot resolve to an absolute path: " + definition.id());
        }

        Path normalizedRoot = questsDir.toAbsolutePath().normalize();
        Path target = questsDir.resolve(relative).toAbsolutePath().normalize();
        if (!target.startsWith(normalizedRoot)) {
            throw new IOException("Quest path escapes quest root: " + definition.id());
        }
        return target;
    }
}
