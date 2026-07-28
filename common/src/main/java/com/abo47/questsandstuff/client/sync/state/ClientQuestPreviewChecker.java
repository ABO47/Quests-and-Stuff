package com.abo47.questsandstuff.client.sync.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.quest.sync.SyncKeys;

final class ClientQuestPreviewChecker {
    private ClientQuestPreviewChecker() {
    }

    static boolean chapterLocked(String chapter) {
        return ClientChapterState.chapterLockUntilUnlocked(chapter) && chapterHasNoUnlockedOrCompletedQuest(chapter);
    }

    static boolean chapterHidden(String chapter) {
        return ClientChapterState.chapterHideUntilUnlocked(chapter) && chapterHasNoUnlockedOrCompletedQuest(chapter);
    }

    static boolean chapterOpenable(String chapter) {
        return !chapterLocked(chapter) && !chapterHidden(chapter);
    }

    static List<String> selectableChapterOrder(boolean canEdit) {
        if (canEdit) {
            return ClientChapterState.chapterOrderSnapshot();
        }
        List<String> chapters = new ArrayList<>();
        for (String chapter : ClientChapterState.chapterOrderSnapshot()) {
            if (chapterOpenable(chapter)) {
                chapters.add(chapter);
            }
        }
        return List.copyOf(chapters);
    }

    static List<String> visibleChapterOrder(boolean canEdit) {
        if (canEdit) {
            return ClientChapterState.chapterOrderSnapshot();
        }
        List<String> chapters = new ArrayList<>();
        for (String chapter : ClientChapterState.chapterOrderSnapshot()) {
            if (!chapterHidden(chapter)) {
                chapters.add(chapter);
            }
        }
        return List.copyOf(chapters);
    }

    static boolean questLocked(CompoundTag quest) {
        return quest != null
                && "locked".equals(quest.getString(SyncKeys.Quest.HIDDEN_MODE))
                && !quest.getBoolean(SyncKeys.Quest.UNLOCKED)
                && !quest.getBoolean(SyncKeys.Quest.COMPLETED);
    }

    static boolean questHidden(CompoundTag quest) {
        return quest != null
                && quest.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN)
                && !quest.getBoolean(SyncKeys.Quest.UNLOCKED)
                && !quest.getBoolean(SyncKeys.Quest.COMPLETED);
    }

    private static boolean chapterHasNoUnlockedOrCompletedQuest(String chapter) {
        for (Map.Entry<String, CompoundTag> entry : ClientQuestState.questEntries()) {
            CompoundTag quest = entry.getValue();
            if (!quest.getCompound(SyncKeys.Quest.CHAPTERS).contains(chapter)) {
                continue;
            }
            if (quest.getBoolean(SyncKeys.Quest.UNLOCKED) || quest.getBoolean(SyncKeys.Quest.COMPLETED)) {
                return false;
            }
        }
        return true;
    }
}
