package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.util.naming.QuestIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestDefinitionNormalizerTest {
    @Test
    void questDefinitionPersistenceUsesDefaultingQuestIds() {
        assertEquals(QuestIdentity.DEFAULT_QUEST_ID, QuestDefinitionNormalizer.normalizeQuestId(" / "));
        assertEquals("Chapter/Quest", QuestDefinitionNormalizer.normalizeQuestId(" \\Chapter//Quest "));
    }

    @Test
    void groupFoldersUseFileSafeNamesNotDisplayNames() {
        assertEquals("main_chapter", QuestDefinitionNormalizer.chapterFolderName(" Main Chapter "));
        assertEquals("ungrouped", QuestDefinitionNormalizer.chapterFolderName("!!!"));
    }
}
