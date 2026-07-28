package com.abo47.questsandstuff.util;

import org.junit.jupiter.api.Test;

import com.abo47.questsandstuff.util.naming.QuestIdentity;
import com.abo47.questsandstuff.util.naming.SafeNames;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestIdentityTest {
    @Test
    void questIdsTrimNormalizeSlashesAndPreserveCase() {
        assertEquals("", QuestIdentity.questId(null));
        assertEquals("", QuestIdentity.questId("   "));
        assertEquals("Chapter/Quest", QuestIdentity.questId(" \\Chapter//Quest "));
        assertEquals("Chapter/Quest", QuestIdentity.questId("/Chapter/Quest"));
    }

    @Test
    void persistenceQuestIdsUseDefaultOnlyWhenRequested() {
        assertEquals(QuestIdentity.DEFAULT_QUEST_ID, QuestIdentity.questIdOrDefault(" / "));
        assertEquals("chapter/quest", QuestIdentity.questIdOrDefault(" /chapter//quest "));
    }

    @Test
    void chapterNamesStayDisplaySafeUntilFileSafeNameIsRequested() {
        assertEquals("", QuestIdentity.chapterName(null));
        assertEquals("Main Chapter!! 01", QuestIdentity.chapterName(" Main Chapter!! 01 "));
        assertEquals("main_chapter_01", QuestIdentity.chapterFolderName(" Main Chapter!! 01 "));
        assertEquals("dark-ui_theme", SafeNames.fileStem("Dark-UI Theme", "default"));
    }

    @Test
    void uiChapterNamesReplaceLineBreaksAndClampLength() {
        assertEquals("Main  Chapter", QuestIdentity.uiChapterName(" Main\n\rChapter "));
        assertEquals(QuestIdentity.UI_CHAPTER_NAME_MAX_LENGTH, QuestIdentity.uiChapterName("1234567890123456789012345678901234567890suffix").length());
        assertEquals("1234567890123456789012345678901234567890", QuestIdentity.uiChapterName("1234567890123456789012345678901234567890suffix"));
    }
}
