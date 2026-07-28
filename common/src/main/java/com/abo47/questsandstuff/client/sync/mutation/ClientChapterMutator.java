package com.abo47.questsandstuff.client.sync.mutation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.sync.state.ClientCanvasLayerState;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.sync.SyncKeys;

public final class ClientChapterMutator {
    private ClientChapterMutator() {
    }

    public static void createChapterLocal(String chapter) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (ClientChapterState.addChapter(normalized)) {
            ClientCanvasLayerState.ensureChapter(normalized);
        }
    }

    public static void renameChapterLocal(String from, String to) {
        String source = ClientChapterState.normalizeChapter(from);
        String target = ClientChapterState.normalizeChapter(to);
        if (!ClientChapterState.renameChapter(source, target)) {
            return;
        }
        ClientCanvasLayerState.renameChapter(source, target);
        ClientQuestState.forEachQuest(quest -> {
            CompoundTag groups = quest.getCompound(SyncKeys.Quest.CHAPTERS);
            if (!groups.contains(source)) {
                return;
            }
            CompoundTag view = groups.getCompound(source).copy();
            groups.remove(source);
            groups.put(target, view);
        });
    }

    public static void deleteChapterLocal(String chapter) {
        String normalized = ClientChapterState.normalizeChapter(chapter);
        if (!ClientChapterState.removeChapter(normalized)) {
            return;
        }
        ClientCanvasLayerState.removeChapter(normalized);
        ClientQuestState.forEachQuest(quest -> {
            CompoundTag groups = quest.getCompound(SyncKeys.Quest.CHAPTERS);
            if (groups.contains(normalized)) {
                groups.remove(normalized);
            }
        });
        List<String> chapterless = new ArrayList<>();
        ClientQuestState.forEachQuestEntry((questId, quest) -> {
            if (quest.getCompound(SyncKeys.Quest.CHAPTERS).isEmpty()) {
                chapterless.add(questId);
            }
        });
        for (String questId : chapterless) {
            ClientQuestMutator.removeQuestLocal(questId);
        }
    }

    public static void moveChapterLocal(String chapter, int offset) {
        ClientChapterState.moveChapter(chapter, offset);
    }

    public static void moveChapterToIndexLocal(String chapter, int targetIndex) {
        ClientChapterState.moveChapterToIndex(chapter, targetIndex);
    }

    public static void setChapterIconLocal(String chapter, String icon) {
        ClientChapterState.setChapterIcon(chapter, icon);
    }

    public static void setChapterBackgroundLocal(String chapter, String background) {
        ClientChapterState.setChapterBackground(chapter, background);
    }

    public static void setChapterCanvasBackgroundLocal(String chapter, String background) {
        ClientChapterState.setChapterCanvasBackground(chapter, background);
    }

    public static void setChapterTextAlignLocal(String chapter, String align) {
        ClientChapterState.setChapterTextAlign(chapter, align);
    }

    public static void setChapterTextColorLocal(String chapter, int color) {
        ClientChapterState.setChapterTextColor(chapter, color);
    }

    public static void setChapterTextStyleLocal(String chapter, String style) {
        ClientChapterState.setChapterTextStyle(chapter, style);
    }

    public static void setChapterTextSizeLocal(String chapter, int size) {
        ClientChapterState.setChapterTextSize(chapter, size);
    }

    public static void setChapterLockUntilUnlockedLocal(String chapter, boolean lockUntilUnlocked) {
        ClientChapterState.setChapterLockUntilUnlocked(chapter, lockUntilUnlocked);
    }

    public static void setChapterHideUntilUnlockedLocal(String chapter, boolean hideUntilUnlocked) {
        ClientChapterState.setChapterHideUntilUnlocked(chapter, hideUntilUnlocked);
    }
}
