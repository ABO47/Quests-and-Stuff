package com.abo47.questsandstuff.quest.runtime.progress;

import com.abo47.questsandstuff.quest.model.storage.IntegerTaskStorage;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class QuestProgressState {
    private final Map<String, Tag> taskProgress = new HashMap<>();
    private final Set<String> claimedRewards = new HashSet<>();
    private boolean unlocked;
    private boolean completed;
    private long completedAt;

    public Map<String, Integer> taskCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Tag> entry : taskProgress.entrySet()) {
            counts.put(entry.getKey(), IntegerTaskStorage.INSTANCE.readInt(entry.getValue()));
        }
        return counts;
    }

    public Map<String, Tag> taskProgress() {
        return taskProgress;
    }

    public Set<String> claimedRewards() {
        return claimedRewards;
    }

    public boolean unlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed, long tick) {
        this.completed = completed;
        if (completed) {
            this.completedAt = tick;
        } else {
            this.completedAt = 0L;
        }
    }

    public long completedAt() {
        return completedAt;
    }

    public int getTaskCount(String taskId) {
        return IntegerTaskStorage.INSTANCE.readInt(taskProgress.get(taskId));
    }

    public void addTaskCount(String taskId, int delta, int cap) {
        taskProgress.put(taskId, IntegerTaskStorage.INSTANCE.add(taskProgress.get(taskId), delta, cap));
    }

    public void setTaskCount(String taskId, int value) {
        taskProgress.put(taskId, IntTag.valueOf(Math.max(0, value)));
    }

    public Tag getTaskProgress(String taskId, QuestTaskDefinition task) {
        Tag progress = taskProgress.get(taskId);
        return progress == null ? task.defaultProgress() : progress.copy();
    }

    public void setTaskProgress(String taskId, QuestTaskDefinition task, Tag progress) {
        Tag next = progress == null ? task.defaultProgress() : progress.copy();
        taskProgress.put(taskId, next);
    }

    public void clearTaskProgress() {
        taskProgress.clear();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("unlocked", unlocked);
        tag.putBoolean("completed", completed);
        tag.putLong("completed_at", completedAt);

        CompoundTag tasksTag = new CompoundTag();
        for (Map.Entry<String, Tag> entry : taskProgress.entrySet()) {
            tasksTag.put(entry.getKey(), entry.getValue().copy());
        }
        tag.put("tasks", tasksTag);

        ListTag claimedTag = new ListTag();
        for (String rewardId : claimedRewards) {
            claimedTag.add(StringTag.valueOf(rewardId));
        }
        tag.put("claimed", claimedTag);
        return tag;
    }

    public static QuestProgressState load(CompoundTag tag) {
        QuestProgressState state = new QuestProgressState();
        state.unlocked = tag.getBoolean("unlocked");
        state.completed = tag.getBoolean("completed");
        state.completedAt = tag.getLong("completed_at");

        CompoundTag tasksTag = tag.getCompound("tasks");
        for (String key : tasksTag.getAllKeys()) {
            Tag progress = tasksTag.get(key);
            if (progress != null) {
                state.taskProgress.put(key, progress.copy());
            }
        }

        ListTag claimedTag = tag.getList("claimed", Tag.TAG_STRING);
        for (int i = 0; i < claimedTag.size(); i++) {
            state.claimedRewards.add(claimedTag.getString(i));
        }
        return state;
    }
}
