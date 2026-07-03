package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public final class ClientQuestMutator {
    private ClientQuestMutator() {
    }

    public static void createChapterLocal(String chapter) {
        ClientQuestPropertyMutator.createChapterLocal(chapter);
    }

    public static void renameChapterLocal(String from, String to) {
        ClientQuestPropertyMutator.renameChapterLocal(from, to);
    }

    public static void deleteChapterLocal(String chapter) {
        ClientQuestPropertyMutator.deleteChapterLocal(chapter);
    }

    public static void moveChapterLocal(String chapter, int offset) {
        ClientQuestPropertyMutator.moveChapterLocal(chapter, offset);
    }

    public static void moveChapterToIndexLocal(String chapter, int targetIndex) {
        ClientQuestPropertyMutator.moveChapterToIndexLocal(chapter, targetIndex);
    }

    public static void setChapterIconLocal(String chapter, String icon) {
        ClientQuestPropertyMutator.setChapterIconLocal(chapter, icon);
    }

    public static void setChapterBackgroundLocal(String chapter, String background) {
        ClientQuestPropertyMutator.setChapterBackgroundLocal(chapter, background);
    }

    public static void setChapterCanvasBackgroundLocal(String chapter, String background) {
        ClientQuestPropertyMutator.setChapterCanvasBackgroundLocal(chapter, background);
    }

    public static void setChapterTextAlignLocal(String chapter, String align) {
        ClientQuestPropertyMutator.setChapterTextAlignLocal(chapter, align);
    }

    public static void setChapterTextColorLocal(String chapter, int color) {
        ClientQuestPropertyMutator.setChapterTextColorLocal(chapter, color);
    }

    public static void setChapterTextStyleLocal(String chapter, String style) {
        ClientQuestPropertyMutator.setChapterTextStyleLocal(chapter, style);
    }

    public static void setChapterTextSizeLocal(String chapter, int size) {
        ClientQuestPropertyMutator.setChapterTextSizeLocal(chapter, size);
    }

    public static void setChapterLockUntilUnlockedLocal(String chapter, boolean lockUntilUnlocked) {
        ClientQuestPropertyMutator.setChapterLockUntilUnlockedLocal(chapter, lockUntilUnlocked);
    }

    public static void setChapterHideUntilUnlockedLocal(String chapter, boolean hideUntilUnlocked) {
        ClientQuestPropertyMutator.setChapterHideUntilUnlockedLocal(chapter, hideUntilUnlocked);
    }

    public static void putCanvasExclusiveChoiceLocal(String chapter, CanvasExclusiveChoice ec) {
        ClientQuestPropertyMutator.putCanvasExclusiveChoiceLocal(chapter, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String chapter, String ecId) {
        ClientQuestPropertyMutator.removeCanvasExclusiveChoiceLocal(chapter, ecId);
    }

    public static void putCanvasImageLocal(String chapter, CanvasImageLayer image) {
        ClientQuestPropertyMutator.putCanvasImageLocal(chapter, image);
    }

    public static void removeCanvasImageLocal(String chapter, String imageId) {
        ClientQuestPropertyMutator.removeCanvasImageLocal(chapter, imageId);
    }

    public static void putCanvasTextLocal(String chapter, CanvasTextLayer text) {
        ClientQuestPropertyMutator.putCanvasTextLocal(chapter, text);
    }

    public static void removeCanvasTextLocal(String chapter, String textId) {
        ClientQuestPropertyMutator.removeCanvasTextLocal(chapter, textId);
    }

    public static void setCanvasLayerOrderLocal(String chapter, List<String> order) {
        ClientQuestPropertyMutator.setCanvasLayerOrderLocal(chapter, order);
    }

    public static int completedCount() {
        return ClientQuestPropertyMutator.completedCount();
    }

    public static int totalCount() {
        return ClientQuestPropertyMutator.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        ClientQuestPropertyMutator.setQuestDisplayLocal(questId, title, subtitle);
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        ClientQuestPropertyMutator.setQuestDescriptionLocal(questId, description);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        ClientQuestPropertyMutator.setQuestIconLocal(questId, icon);
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        ClientQuestPropertyMutator.setQuestRepeatableLocal(questId, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        ClientQuestPropertyMutator.setQuestHiddenModeLocal(questId, hiddenMode);
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        ClientQuestPropertyMutator.setQuestVisualHiddenLocal(questId, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        ClientQuestPropertyMutator.setQuestCompletionSoundLocal(questId, sound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        ClientQuestPropertyMutator.setQuestCompletionSoundVolumeLocal(questId, volume);
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        ClientQuestPropertyMutator.setQuestCompletionHudBackgroundLocal(questId, background);
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        ClientQuestPropertyMutator.setQuestBackgroundLocal(questId, background, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        ClientQuestPropertyMutator.resetQuestProgressLocal(questId);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        ClientQuestPropertyMutator.setQuestClaimedLocal(questId, claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        ClientQuestTaskMutator.putQuestTaskJsonLocal(questId, taskJson);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        ClientQuestTaskMutator.putQuestRewardJsonLocal(questId, rewardJson);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestPropertyMutator.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestPropertyMutator.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestPropertyMutator.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestPropertyMutator.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ClientQuestPropertyMutator.setConnectionTextureLocal(questId, prerequisiteId, texture);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ClientQuestPropertyMutator.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
    }

    public static void setQuestPositionInChapterLocal(String questId, String chapter, int x, int y) {
        ClientQuestPropertyMutator.setQuestPositionInChapterLocal(questId, chapter, x, y);
    }

    public static void setQuestScaleInChapterLocal(String questId, String chapter, float scale) {
        ClientQuestPropertyMutator.setQuestScaleInChapterLocal(questId, chapter, scale);
    }

    public static void createEditorQuestLocal(String questId, String chapter, int x, int y, String title) {
        ClientQuestPropertyMutator.createEditorQuestLocal(questId, chapter, x, y, title);
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestPropertyMutator.copyQuestLocal(sourceQuestId, newQuestId, chapter, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestPropertyMutator.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, chapter, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestPropertyMutator.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        ClientQuestPropertyMutator.removeQuestLocal(questId);
    }
}
