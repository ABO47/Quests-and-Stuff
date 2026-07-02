package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.util.naming.QuestNaming;

import java.io.IOException;
import java.nio.file.Path;

import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.groupFolderName;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.normalizeQuestId;
import static com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionNormalizer.primaryGroup;

final class QuestDefinitionPaths {
    private QuestDefinitionPaths() {
    }

    static Path questPath(Path questsDir, QuestDefinition definition) throws IOException {
        String canonicalId = normalizeQuestId(definition.id());
        String group = primaryGroup(definition);
        String managedFileName = QuestNaming.managedQuestFileName(canonicalId, group);
        Path relative;
        if (!managedFileName.isBlank()) {
            relative = Path.of(QuestNaming.chapterFolderName(group)).resolve(managedFileName);
        } else {
            String slug = canonicalId.replace('/', '_');
            relative = Path.of(groupFolderName(group)).resolve(slug + ".json");
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
