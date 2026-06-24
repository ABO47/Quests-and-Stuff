package com.abo47.questsandstuff.client.sync.cache;

import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ClientQuestPreviewRules {
    private ClientQuestPreviewRules() {
    }

    static boolean groupLocked(String group) {
        return ClientChapterState.groupLockUntilUnlocked(group) && groupHasNoUnlockedOrCompletedQuest(group);
    }

    static boolean groupHidden(String group) {
        return ClientChapterState.groupHideUntilUnlocked(group) && groupHasNoUnlockedOrCompletedQuest(group);
    }

    static boolean groupOpenable(String group) {
        return !groupLocked(group) && !groupHidden(group);
    }

    static List<String> selectableGroupOrder(boolean canEdit) {
        if (canEdit) {
            return ClientChapterState.groupOrderSnapshot();
        }
        List<String> groups = new ArrayList<>();
        for (String group : ClientChapterState.groupOrderSnapshot()) {
            if (groupOpenable(group)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }

    static List<String> visibleGroupOrder(boolean canEdit) {
        if (canEdit) {
            return ClientChapterState.groupOrderSnapshot();
        }
        List<String> groups = new ArrayList<>();
        for (String group : ClientChapterState.groupOrderSnapshot()) {
            if (!groupHidden(group)) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }

    static boolean questLocked(CompoundTag quest) {
        return quest != null
                && "locked".equals(quest.getString(QuestSyncKeys.Quest.HIDDEN_MODE))
                && !quest.getBoolean(QuestSyncKeys.Quest.UNLOCKED)
                && !quest.getBoolean(QuestSyncKeys.Quest.COMPLETED);
    }

    static boolean questHidden(CompoundTag quest) {
        return quest != null
                && quest.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN)
                && !quest.getBoolean(QuestSyncKeys.Quest.UNLOCKED)
                && !quest.getBoolean(QuestSyncKeys.Quest.COMPLETED);
    }

    private static boolean groupHasNoUnlockedOrCompletedQuest(String group) {
        for (Map.Entry<String, CompoundTag> entry : ClientQuestState.questEntries()) {
            CompoundTag quest = entry.getValue();
            if (!quest.getCompound(QuestSyncKeys.Quest.GROUPS).contains(group)) {
                continue;
            }
            if (quest.getBoolean(QuestSyncKeys.Quest.UNLOCKED) || quest.getBoolean(QuestSyncKeys.Quest.COMPLETED)) {
                return false;
            }
        }
        return true;
    }
}
