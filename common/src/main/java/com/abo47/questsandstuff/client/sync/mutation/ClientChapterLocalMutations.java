package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientCanvasLayerState;
import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public final class ClientChapterLocalMutations {
    private ClientChapterLocalMutations() {
    }

    public static void createGroupLocal(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (ClientChapterState.addGroup(normalized)) {
            ClientCanvasLayerState.ensureGroup(normalized);
        }
    }

    public static void renameGroupLocal(String from, String to) {
        String source = ClientChapterState.normalizeGroup(from);
        String target = ClientChapterState.normalizeGroup(to);
        if (!ClientChapterState.renameGroup(source, target)) {
            return;
        }
        ClientCanvasLayerState.renameGroup(source, target);
        ClientQuestState.forEachQuest(quest -> {
            CompoundTag groups = quest.getCompound("groups");
            if (!groups.contains(source)) {
                return;
            }
            CompoundTag view = groups.getCompound(source).copy();
            groups.remove(source);
            groups.put(target, view);
        });
    }

    public static void deleteGroupLocal(String group) {
        String normalized = ClientChapterState.normalizeGroup(group);
        if (!ClientChapterState.removeGroup(normalized)) {
            return;
        }
        ClientCanvasLayerState.removeGroup(normalized);
        ClientQuestState.forEachQuest(quest -> {
            CompoundTag groups = quest.getCompound("groups");
            if (groups.contains(normalized)) {
                groups.remove(normalized);
            }
        });
        List<String> groupless = new ArrayList<>();
        ClientQuestState.forEachQuestEntry((questId, quest) -> {
            if (quest.getCompound("groups").isEmpty()) {
                groupless.add(questId);
            }
        });
        for (String questId : groupless) {
            ClientQuestLocalMutations.removeQuestLocal(questId);
        }
    }

    public static void moveGroupLocal(String group, int offset) {
        ClientChapterState.moveGroup(group, offset);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        ClientChapterState.moveGroupToIndex(group, targetIndex);
    }

    public static void setGroupIconLocal(String group, String icon) {
        ClientChapterState.setGroupIcon(group, icon);
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        ClientChapterState.setGroupBackground(group, background);
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        ClientChapterState.setGroupCanvasBackground(group, background);
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        ClientChapterState.setGroupTextAlign(group, align);
    }

    public static void setGroupTextColorLocal(String group, int color) {
        ClientChapterState.setGroupTextColor(group, color);
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        ClientChapterState.setGroupTextStyle(group, style);
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        ClientChapterState.setGroupTextSize(group, size);
    }

    public static void setGroupLockUntilUnlockedLocal(String group, boolean lockUntilUnlocked) {
        ClientChapterState.setGroupLockUntilUnlocked(group, lockUntilUnlocked);
    }

    public static void setGroupHideUntilUnlockedLocal(String group, boolean hideUntilUnlocked) {
        ClientChapterState.setGroupHideUntilUnlocked(group, hideUntilUnlocked);
    }
}
