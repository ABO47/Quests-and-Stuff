package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

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
            if (quest.getCompound("groups").contains(normalizedGroup)) {
                setQuestHiddenModeLocal(questId, mode);
            }
        });
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
            quest.putString("title", title);
        }
        if (subtitle != null) {
            quest.putString("subtitle", subtitle);
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
        quest.put("description", lines);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        String normalized = icon == null || icon.isBlank() ? "minecraft:book" : icon.trim();
        quest.putString("icon", normalized);
    }

    public static void setQuestAutoClaimLocal(String questId, boolean enabled) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean("auto_claim_rewards", enabled);
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean("repeatable", enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        if (questId == null || questId.isBlank() || hiddenMode == null || hiddenMode.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putString("hidden_mode", hiddenMode.trim());
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putBoolean("visual_hidden", hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        String normalizedSound = sound == null || sound.isBlank() ? "minecraft:ui.toast.challenge_complete" : sound.trim();
        quest.putString("completion_sound", normalizedSound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestState.mutableQuest(questId);
        if (quest == null) {
            return;
        }
        quest.putInt("completion_sound_volume", QuestDisplay.normalizeCompletionSoundVolume(volume));
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
        quest.putBoolean("completed", false);
        quest.putBoolean("claimed", false);
        quest.putFloat("progress", 0.0f);
        CompoundTag tasks = quest.getCompound("tasks");
        for (String taskId : tasks.getAllKeys()) {
            CompoundTag task = tasks.getCompound(taskId);
            task.putFloat("progress", 0.0f);
            task.putBoolean("complete", false);
            task.putInt("count", 0);
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
        quest.putBoolean("claimed", claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        putObjectiveJsonLocal(questId, taskJson, "tasks");
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        putObjectiveJsonLocal(questId, rewardJson, "rewards");
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
        groupTag.putInt("x", x);
        groupTag.putInt("y", y);
    }

    public static void setQuestScaleInGroupLocal(String questId, String group, float scale) {
        CompoundTag groupTag = mutableGroupView(questId, group);
        if (groupTag == null) {
            return;
        }
        float normalized = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat("scale", Math.max(0.5f, normalized));
    }

    public static void createEditorQuestLocal(String questId, String group, int x, int y, String title) {
        String normalizedQuest = questId == null ? "" : questId.trim();
        String normalizedGroup = normalizeGroup(group);
        if (normalizedQuest.isBlank() || normalizedGroup.isBlank()) {
            return;
        }

        String normalizedTitle = title == null ? "" : title.trim();

        CompoundTag quest = new CompoundTag();
        quest.putString("title", normalizedTitle);
        quest.putString("subtitle", "");
        quest.putString("icon", "minecraft:book");
        quest.putString("icon_background", "minecraft:barrier");
        quest.putString("completion_sound", "minecraft:ui.toast.challenge_complete");
        quest.putInt("completion_sound_volume", QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME);
        quest.putBoolean("visual_hidden", false);
        quest.putBoolean("completed", false);
        quest.putBoolean("unlocked", true);
        quest.putBoolean("claimed", false);
        quest.putFloat("progress", 0.0f);
        quest.putBoolean("repeatable", false);
        quest.putBoolean("auto_claim_rewards", false);
        quest.putString("hidden_mode", (ClientChapterState.groupLockUntilUnlocked(normalizedGroup) ? QuestVisibilityMode.LOCKED : QuestVisibilityMode.PREREQUISITES_VISIBLE).serializedName());
        quest.putBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD, true);
        quest.put(QuestDefinition.PREREQUISITES_FIELD, new ListTag());
        quest.put("description", new ListTag());
        quest.put("tasks", new CompoundTag());
        quest.put("rewards", new CompoundTag());

        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean("visible", true);
        groupTag.putInt("x", x);
        groupTag.putInt("y", y);
        groupTag.putFloat("scale", 1.0f);
        groups.put(normalizedGroup, groupTag);
        quest.put("groups", groups);

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

    private static void putObjectiveJsonLocal(String questId, String jsonValue, String bucketName) {
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
        entry.putString("json", jsonValue);
        entry.putString("type", objectiveType(jsonValue, entry.getString("type")));
        bucket.put(id, entry);
        quest.put(bucketName, bucket);
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
        CompoundTag groups = quest.getCompound("groups");
        CompoundTag groupTag = groups.getCompound(group).copy();
        groups.put(group, groupTag);
        return groupTag;
    }

    private static String normalizeGroup(String value) {
        return value == null ? "" : value.trim();
    }

}
