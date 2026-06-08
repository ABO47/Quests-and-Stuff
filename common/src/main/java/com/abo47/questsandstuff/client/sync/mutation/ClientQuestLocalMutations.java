package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Map;

public final class ClientQuestLocalMutations {
    private ClientQuestLocalMutations() {
    }

    public static void createGroupLocal(String group) {
        ClientChapterLocalMutations.createGroupLocal(group);
    }

    public static void renameGroupLocal(String from, String to) {
        ClientChapterLocalMutations.renameGroupLocal(from, to);
    }

    public static void deleteGroupLocal(String group) {
        ClientChapterLocalMutations.deleteGroupLocal(group);
    }

    public static void moveGroupLocal(String group, int offset) {
        ClientChapterLocalMutations.moveGroupLocal(group, offset);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        ClientChapterLocalMutations.moveGroupToIndexLocal(group, targetIndex);
    }

    public static void setGroupIconLocal(String group, String icon) {
        ClientChapterLocalMutations.setGroupIconLocal(group, icon);
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        ClientChapterLocalMutations.setGroupBackgroundLocal(group, background);
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        ClientChapterLocalMutations.setGroupCanvasBackgroundLocal(group, background);
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        ClientChapterLocalMutations.setGroupTextAlignLocal(group, align);
    }

    public static void setGroupTextColorLocal(String group, int color) {
        ClientChapterLocalMutations.setGroupTextColorLocal(group, color);
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        ClientChapterLocalMutations.setGroupTextStyleLocal(group, style);
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        ClientChapterLocalMutations.setGroupTextSizeLocal(group, size);
    }

    public static void setGroupLockUntilUnlockedLocal(String group, boolean lockUntilUnlocked) {
        String normalizedGroup = ClientChapterState.normalizeGroup(group);
        if (normalizedGroup.isBlank()) {
            return;
        }
        ClientChapterLocalMutations.setGroupLockUntilUnlockedLocal(normalizedGroup, lockUntilUnlocked);
        String mode = (lockUntilUnlocked ? QuestVisibilityMode.LOCKED : QuestVisibilityMode.PREREQUISITES_VISIBLE).serializedName();
        ClientQuestState.forEachQuestEntry((questId, quest) -> {
            if (quest.getCompound(QuestSyncKeys.Quest.GROUPS).contains(normalizedGroup)) {
                setQuestHiddenModeLocal(questId, mode);
            }
        });
    }

    public static void setGroupHideUntilUnlockedLocal(String group, boolean hideUntilUnlocked) {
        ClientChapterLocalMutations.setGroupHideUntilUnlockedLocal(group, hideUntilUnlocked);
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        ClientCanvasLocalMutations.putCanvasImageLocal(group, image);
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        ClientCanvasLocalMutations.removeCanvasImageLocal(group, imageId);
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        ClientCanvasLocalMutations.putCanvasTextLocal(group, text);
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        ClientCanvasLocalMutations.removeCanvasTextLocal(group, textId);
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        ClientCanvasLocalMutations.setCanvasLayerOrderLocal(group, order);
    }

    public static int completedCount() {
        return ClientQuestState.completedCount();
    }

    public static int totalCount() {
        return ClientQuestState.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        if (title != null) {
            quest.putString(QuestSyncKeys.Quest.TITLE, title);
        }
        if (subtitle != null) {
            quest.putString(QuestSyncKeys.Quest.SUBTITLE, subtitle);
        }
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        ListTag lines = new ListTag();
        if (description != null) {
            for (String line : description) {
                if (line != null) {
                    lines.add(net.minecraft.nbt.StringTag.valueOf(line));
                }
            }
        }
        quest.put(QuestSyncKeys.Quest.DESCRIPTION, lines);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(QuestSyncKeys.Quest.ICON, QuestDisplay.normalizeIcon(icon));
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean(QuestSyncKeys.Quest.REPEATABLE, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        if (questId == null || questId.isBlank() || hiddenMode == null || hiddenMode.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(QuestSyncKeys.Quest.HIDDEN_MODE, hiddenMode.trim());
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        quest.putString(QuestSyncKeys.Quest.COMPLETION_SOUND, normalizedSound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.normalizeCompletionSoundVolume(volume));
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND, QuestDisplay.normalizeCompletionHudBackground(background));
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString(QuestSyncKeys.Quest.QUEST_BACKGROUND, QuestDisplay.normalizeQuestBackground(background));
        quest.putBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        if (normalized.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalized);
        if (quest == null) {
            return;
        }
        quest.putBoolean(QuestSyncKeys.Quest.COMPLETED, false);
        quest.putBoolean(QuestSyncKeys.Quest.CLAIMED, false);
        quest.putFloat(QuestSyncKeys.Quest.PROGRESS, 0.0f);
        CompoundTag tasks = quest.getCompound(QuestSyncKeys.Quest.TASKS);
        for (String taskId : tasks.getAllKeys()) {
            CompoundTag task = tasks.getCompound(taskId);
            task.putFloat(QuestSyncKeys.Objective.PROGRESS, 0.0f);
            task.putBoolean(QuestSyncKeys.Objective.COMPLETE, false);
            task.putInt(QuestSyncKeys.Objective.COUNT, 0);
            tasks.put(taskId, task);
        }
        ClientQuestState.forEachQuest(ClientQuestConnectionMutations::refreshLocalUnlockState);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        String normalized = questId == null ? "" : questId.trim();
        if (normalized.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalized);
        if (quest == null) {
            return;
        }
        quest.putBoolean(QuestSyncKeys.Quest.CLAIMED, claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        putObjectiveJsonLocal(questId, taskJson, QuestSyncKeys.Quest.TASKS, QuestSyncKeys.Quest.TASKS_ORDER);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        putObjectiveJsonLocal(questId, rewardJson, QuestSyncKeys.Quest.REWARDS, QuestSyncKeys.Quest.REWARDS_ORDER);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestConnectionMutations.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestConnectionMutations.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestConnectionMutations.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestConnectionMutations.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setQuestPositionInGroupLocal(String questId, String group, int x, int y) {
        CompoundTag groupTag = mutableGroupView(questId, group);
        if (groupTag == null) {
            return;
        }
        groupTag.putInt(QuestSyncKeys.ChapterView.X, x);
        groupTag.putInt(QuestSyncKeys.ChapterView.Y, y);
    }

    public static void setQuestScaleInGroupLocal(String questId, String group, float scale) {
        CompoundTag groupTag = mutableGroupView(questId, group);
        if (groupTag == null) {
            return;
        }
        float normalized = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat(QuestSyncKeys.ChapterView.SCALE, Math.max(0.5f, normalized));
    }

    public static void createEditorQuestLocal(String questId, String group, int x, int y, String title) {
        String normalizedQuest = questId == null ? "" : questId.trim();
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
        ClientQuestCopyMutations.copyQuestLocal(sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestCopyMutations.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestCopyMutations.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        String normalized = questId == null ? "" : questId.trim();
        if (normalized.isBlank() || !ClientQuestState.removeQuest(normalized)) {
            return;
        }
        ClientQuestState.removePinned(normalized);
        ClientQuestConnectionMutations.removeQuestReferences(normalized);
    }

    private static void putObjectiveJsonLocal(String questId, String jsonValue, String bucketName, String orderName) {
        String normalizedQuest = questId == null ? "" : questId.trim();
        if (normalizedQuest.isBlank() || jsonValue == null || jsonValue.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(normalizedQuest);
        if (quest == null) {
            return;
        }
        String id = objectiveId(jsonValue);
        if (id.isBlank()) {
            return;
        }
        CompoundTag bucket = quest.getCompound(bucketName);
        CompoundTag entry = bucket.getCompound(id);
        entry.putString(QuestSyncKeys.Objective.JSON, jsonValue);
        entry.putString(QuestSyncKeys.Objective.TYPE, objectiveType(jsonValue, entry.getString(QuestSyncKeys.Objective.TYPE)));
        bucket.put(id, entry);
        quest.put(bucketName, bucket);
        appendObjectiveOrder(quest, orderName, id);
    }

    private static void appendObjectiveOrder(CompoundTag quest, String orderName, String id) {
        if (quest == null || orderName == null || orderName.isBlank() || id == null || id.isBlank()) {
            return;
        }
        ListTag current = quest.getList(orderName, Tag.TAG_STRING);
        ListTag next = new ListTag();
        boolean found = false;
        for (int i = 0; i < current.size(); i++) {
            String value = current.getString(i);
            if (id.equals(value)) {
                found = true;
            }
            next.add(current.get(i).copy());
        }
        if (!found) {
            next.add(net.minecraft.nbt.StringTag.valueOf(id));
        }
        quest.put(orderName, next);
    }

    private static String objectiveId(String jsonValue) {
        try {
            JsonObject json = JsonParser.parseString(jsonValue).getAsJsonObject();
            return json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString().trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String objectiveType(String jsonValue, String fallback) {
        try {
            JsonObject json = JsonParser.parseString(jsonValue).getAsJsonObject();
            return json.has("type") && !json.get("type").isJsonNull() ? json.get("type").getAsString().trim() : fallback;
        } catch (Exception ignored) {
            return fallback == null ? "" : fallback;
        }
    }

    private static CompoundTag mutableGroupView(String questId, String group) {
        if (questId == null || questId.isBlank() || group == null || group.isBlank()) {
            return null;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return null;
        }
        CompoundTag groups = quest.getCompound(QuestSyncKeys.Quest.GROUPS);
        CompoundTag groupTag = groups.getCompound(group).copy();
        groups.put(group, groupTag);
        return groupTag;
    }

    private static String normalizeGroup(String value) {
        return value == null ? "" : value.trim();
    }

}
