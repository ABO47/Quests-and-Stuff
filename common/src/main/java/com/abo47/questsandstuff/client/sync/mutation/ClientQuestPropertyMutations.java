package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.state.ClientCanvasLayerState;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import com.abo47.questsandstuff.util.naming.QuestIdentity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.List;
import java.util.Map;

public final class ClientQuestPropertyMutations {
    private ClientQuestPropertyMutations() {
    }

    public static void createGroupLocal(String group) {
        ClientChapterMutator.createGroupLocal(group);
    }

    public static void renameGroupLocal(String from, String to) {
        ClientChapterMutator.renameGroupLocal(from, to);
    }

    public static void deleteGroupLocal(String group) {
        ClientChapterMutator.deleteGroupLocal(group);
    }

    public static void moveGroupLocal(String group, int offset) {
        ClientChapterMutator.moveGroupLocal(group, offset);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        ClientChapterMutator.moveGroupToIndexLocal(group, targetIndex);
    }

    public static void setGroupIconLocal(String group, String icon) {
        ClientChapterMutator.setGroupIconLocal(group, icon);
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        ClientChapterMutator.setGroupBackgroundLocal(group, background);
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        ClientChapterMutator.setGroupCanvasBackgroundLocal(group, background);
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        ClientChapterMutator.setGroupTextAlignLocal(group, align);
    }

    public static void setGroupTextColorLocal(String group, int color) {
        ClientChapterMutator.setGroupTextColorLocal(group, color);
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        ClientChapterMutator.setGroupTextStyleLocal(group, style);
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        ClientChapterMutator.setGroupTextSizeLocal(group, size);
    }

    public static void setGroupLockUntilUnlockedLocal(String group, boolean lockUntilUnlocked) {
        String normalizedGroup = ClientChapterState.normalizeGroup(group);
        if (normalizedGroup.isBlank()) {
            return;
        }
        ClientChapterMutator.setGroupLockUntilUnlockedLocal(normalizedGroup, lockUntilUnlocked);
        String mode = (lockUntilUnlocked ? QuestVisibilityMode.LOCKED : QuestVisibilityMode.PREREQUISITES_VISIBLE).serializedName();
        ClientQuestState.forEachQuestEntry((questId, quest) -> {
            if (quest.getCompound(SyncKeys.Quest.GROUPS).contains(normalizedGroup)) {
                setQuestHiddenModeLocal(questId, mode);
            }
        });
    }

    public static void setGroupHideUntilUnlockedLocal(String group, boolean hideUntilUnlocked) {
        ClientChapterMutator.setGroupHideUntilUnlockedLocal(group, hideUntilUnlocked);
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        ClientCanvasMutator.putCanvasImageLocal(group, image);
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        ClientCanvasMutator.removeCanvasImageLocal(group, imageId);
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        ClientCanvasMutator.putCanvasTextLocal(group, text);
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        ClientCanvasMutator.removeCanvasTextLocal(group, textId);
    }

    public static void putCanvasExclusiveChoiceLocal(String group, CanvasExclusiveChoice ec) {
        ClientCanvasMutator.putCanvasExclusiveChoiceLocal(group, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String group, String ecId) {
        ClientCanvasMutator.removeCanvasExclusiveChoiceLocal(group, ecId);
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        ClientCanvasMutator.setCanvasLayerOrderLocal(group, order);
    }

    public static int completedCount() {
        return ClientQuestState.completedCount();
    }

    public static int totalCount() {
        return ClientQuestState.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        if (title != null) {
            quest.putString(SyncKeys.Quest.TITLE, title);
        }
        if (subtitle != null) {
            quest.putString(SyncKeys.Quest.SUBTITLE, subtitle);
        }
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        ListTag lines = new ListTag();
        if (description != null) {
            for (String line : description) {
                if (line != null) {
                    lines.add(StringTag.valueOf(line));
                }
            }
        }
        quest.put(SyncKeys.Quest.DESCRIPTION, lines);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(SyncKeys.Quest.ICON, QuestDisplay.normalizeIcon(icon));
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean(SyncKeys.Quest.REPEATABLE, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        if (hiddenMode == null || hiddenMode.isBlank()) {
            return;
        }
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(SyncKeys.Quest.HIDDEN_MODE, hiddenMode.trim());
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean(SyncKeys.Quest.VISUAL_HIDDEN, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        quest.putString(SyncKeys.Quest.COMPLETION_SOUND, normalizedSound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.normalizeCompletionSoundVolume(volume));
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND, QuestDisplay.normalizeCompletionHudBackground(background));
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        CompoundTag quest = mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(SyncKeys.Quest.QUEST_BACKGROUND, QuestDisplay.normalizeQuestBackground(background));
        quest.putBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalized);
        if (quest == null) {
            return;
        }
        quest.putBoolean(SyncKeys.Quest.COMPLETED, false);
        quest.putBoolean(SyncKeys.Quest.CLAIMED, false);
        quest.putFloat(SyncKeys.Quest.PROGRESS, 0.0f);
        CompoundTag tasks = quest.getCompound(SyncKeys.Quest.TASKS);
        for (String taskId : tasks.getAllKeys()) {
            CompoundTag task = tasks.getCompound(taskId);
            task.putFloat(SyncKeys.Objective.PROGRESS, 0.0f);
            task.putBoolean(SyncKeys.Objective.COMPLETE, false);
            task.putInt(SyncKeys.Objective.COUNT, 0);
            tasks.put(taskId, task);
        }
        ClientQuestState.forEachQuest(ClientQuestConnectionMutator::refreshLocalUnlockState);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalized);
        if (quest == null) {
            return;
        }
        quest.putBoolean(SyncKeys.Quest.CLAIMED, claimed);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestConnectionMutator.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestConnectionMutator.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestConnectionMutator.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestConnectionMutator.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ClientQuestConnectionMutator.setConnectionTextureLocal(questId, prerequisiteId, texture);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ClientQuestConnectionMutator.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
    }

    public static void setQuestPositionInGroupLocal(String questId, String group, int x, int y) {
        CompoundTag groupTag = mutableGroupView(questId, group);
        if (groupTag == null) {
            return;
        }
        groupTag.putInt(SyncKeys.ChapterView.X, x);
        groupTag.putInt(SyncKeys.ChapterView.Y, y);
    }

    public static void setQuestScaleInGroupLocal(String questId, String group, float scale) {
        CompoundTag groupTag = mutableGroupView(questId, group);
        if (groupTag == null) {
            return;
        }
        float normalized = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat(SyncKeys.ChapterView.SCALE, Math.max(0.5f, normalized));
    }

    public static void createEditorQuestLocal(String questId, String group, int x, int y, String title) {
        String normalizedQuest = normalizeQuestId(questId);
        String normalizedGroup = normalizeGroup(group);
        if (normalizedQuest.isBlank() || normalizedGroup.isBlank()) {
            return;
        }
        String normalizedTitle = title == null ? "" : title.trim();
        QuestVisibilityMode hiddenMode = ClientChapterState.groupLockUntilUnlocked(normalizedGroup)
                ? QuestVisibilityMode.LOCKED
                : QuestVisibilityMode.PREREQUISITES_VISIBLE;
        CompoundTag quest = ClientQuestSnapshotBuilder.newEditorQuest(normalizedTitle, normalizedGroup, x, y, hiddenMode);
        ClientQuestState.putQuest(normalizedQuest, quest);
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestCopyMutator.copyQuestLocal(sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestCopyMutator.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestCopyMutator.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank() || !ClientQuestState.removeQuest(normalized)) {
            return;
        }
        ClientQuestState.removePinned(normalized);
        ClientQuestConnectionMutator.removeQuestReferences(normalized);
        for (Map.Entry<String, List<CanvasExclusiveChoice>> entry : ClientCanvasLayerState.exclusiveChoicesByGroup().entrySet()) {
            String group = entry.getKey();
            for (CanvasExclusiveChoice ec : entry.getValue()) {
                boolean changed = false;
                if (ec.connectionQuestIds().contains(normalized)) {
                    ec = ec.removeConnection(normalized);
                    changed = true;
                }
                if (ec.prerequisiteQuestIds().contains(normalized)) {
                    ec = ec.removePrerequisite(normalized);
                    changed = true;
                }
                if (changed) {
                    ClientCanvasLayerState.putExclusiveChoice(group, ec);
                }
            }
        }
    }

    private static CompoundTag mutableGroupView(String questId, String group) {
        String normalizedQuest = normalizeQuestId(questId);
        String normalizedGroup = normalizeGroup(group);
        if (normalizedQuest.isBlank() || normalizedGroup.isBlank()) {
            return null;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalizedQuest);
        if (quest == null) {
            return null;
        }
        CompoundTag groups = quest.getCompound(SyncKeys.Quest.GROUPS);
        CompoundTag groupTag = groups.getCompound(normalizedGroup).copy();
        groups.put(normalizedGroup, groupTag);
        return groupTag;
    }

    static CompoundTag mutableQuest(String questId) {
        String normalized = normalizeQuestId(questId);
        return normalized.isBlank() ? null : ClientQuestState.mutableQuest(normalized);
    }

    static String normalizeQuestId(String value) {
        return QuestIdentity.questId(value);
    }

    private static String normalizeGroup(String value) {
        return QuestIdentity.groupName(value);
    }
}
