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

    public static void createGroupLocal(String group) {
        ClientQuestPropertyMutations.createGroupLocal(group);
    }

    public static void renameGroupLocal(String from, String to) {
        ClientQuestPropertyMutations.renameGroupLocal(from, to);
    }

    public static void deleteGroupLocal(String group) {
        ClientQuestPropertyMutations.deleteGroupLocal(group);
    }

    public static void moveGroupLocal(String group, int offset) {
        ClientQuestPropertyMutations.moveGroupLocal(group, offset);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        ClientQuestPropertyMutations.moveGroupToIndexLocal(group, targetIndex);
    }

    public static void setGroupIconLocal(String group, String icon) {
        ClientQuestPropertyMutations.setGroupIconLocal(group, icon);
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        ClientQuestPropertyMutations.setGroupBackgroundLocal(group, background);
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        ClientQuestPropertyMutations.setGroupCanvasBackgroundLocal(group, background);
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        ClientQuestPropertyMutations.setGroupTextAlignLocal(group, align);
    }

    public static void setGroupTextColorLocal(String group, int color) {
        ClientQuestPropertyMutations.setGroupTextColorLocal(group, color);
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        ClientQuestPropertyMutations.setGroupTextStyleLocal(group, style);
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        ClientQuestPropertyMutations.setGroupTextSizeLocal(group, size);
    }

    public static void setGroupLockUntilUnlockedLocal(String group, boolean lockUntilUnlocked) {
        ClientQuestPropertyMutations.setGroupLockUntilUnlockedLocal(group, lockUntilUnlocked);
    }

    public static void setGroupHideUntilUnlockedLocal(String group, boolean hideUntilUnlocked) {
        ClientQuestPropertyMutations.setGroupHideUntilUnlockedLocal(group, hideUntilUnlocked);
    }

    public static void putCanvasExclusiveChoiceLocal(String group, CanvasExclusiveChoice ec) {
        ClientQuestPropertyMutations.putCanvasExclusiveChoiceLocal(group, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String group, String ecId) {
        ClientQuestPropertyMutations.removeCanvasExclusiveChoiceLocal(group, ecId);
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        ClientQuestPropertyMutations.putCanvasImageLocal(group, image);
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        ClientQuestPropertyMutations.removeCanvasImageLocal(group, imageId);
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        ClientQuestPropertyMutations.putCanvasTextLocal(group, text);
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        ClientQuestPropertyMutations.removeCanvasTextLocal(group, textId);
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        ClientQuestPropertyMutations.setCanvasLayerOrderLocal(group, order);
    }

    public static int completedCount() {
        return ClientQuestPropertyMutations.completedCount();
    }

    public static int totalCount() {
        return ClientQuestPropertyMutations.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        ClientQuestPropertyMutations.setQuestDisplayLocal(questId, title, subtitle);
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        ClientQuestPropertyMutations.setQuestDescriptionLocal(questId, description);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        ClientQuestPropertyMutations.setQuestIconLocal(questId, icon);
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        ClientQuestPropertyMutations.setQuestRepeatableLocal(questId, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        ClientQuestPropertyMutations.setQuestHiddenModeLocal(questId, hiddenMode);
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        ClientQuestPropertyMutations.setQuestVisualHiddenLocal(questId, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        ClientQuestPropertyMutations.setQuestCompletionSoundLocal(questId, sound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        ClientQuestPropertyMutations.setQuestCompletionSoundVolumeLocal(questId, volume);
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        ClientQuestPropertyMutations.setQuestCompletionHudBackgroundLocal(questId, background);
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        ClientQuestPropertyMutations.setQuestBackgroundLocal(questId, background, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        ClientQuestPropertyMutations.resetQuestProgressLocal(questId);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        ClientQuestPropertyMutations.setQuestClaimedLocal(questId, claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        ClientQuestTaskMutations.putQuestTaskJsonLocal(questId, taskJson);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        ClientQuestTaskMutations.putQuestRewardJsonLocal(questId, rewardJson);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestPropertyMutations.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestPropertyMutations.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestPropertyMutations.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestPropertyMutations.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ClientQuestPropertyMutations.setConnectionTextureLocal(questId, prerequisiteId, texture);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ClientQuestPropertyMutations.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
    }

    public static void setQuestPositionInGroupLocal(String questId, String group, int x, int y) {
        ClientQuestPropertyMutations.setQuestPositionInGroupLocal(questId, group, x, y);
    }

    public static void setQuestScaleInGroupLocal(String questId, String group, float scale) {
        ClientQuestPropertyMutations.setQuestScaleInGroupLocal(questId, group, scale);
    }

    public static void createEditorQuestLocal(String questId, String group, int x, int y, String title) {
        ClientQuestPropertyMutations.createEditorQuestLocal(questId, group, x, y, title);
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestPropertyMutations.copyQuestLocal(sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestPropertyMutations.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestPropertyMutations.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        ClientQuestPropertyMutations.removeQuestLocal(questId);
    }
}
