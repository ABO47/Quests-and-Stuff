package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.List;
import java.util.Map;

final class ClientQuestSnapshotBuilder {
    private ClientQuestSnapshotBuilder() {
    }

    static CompoundTag newEditorQuest(String title, String group, int x, int y, QuestVisibilityMode hiddenMode) {
        QuestDisplay display = QuestDisplay.forNewQuest(title, Map.of(group, new ChapterDefinition(true, x, y, 1.0f)));
        CompoundTag quest = emptyEditorQuest(display, hiddenMode);
        setProgressDefaults(quest, true);
        return quest;
    }

    static void prepareCopiedQuest(CompoundTag quest, String group, int x, int y, float scale) {
        if (quest == null) {
            return;
        }
        ensureDisplayDefaults(quest);
        ensureDefinitionBuckets(quest);
        setProgressDefaults(quest, false);
        replaceSingleGroup(quest, group, x, y, scale);
    }

    private static CompoundTag emptyEditorQuest(QuestDisplay display, QuestVisibilityMode hiddenMode) {
        CompoundTag quest = new CompoundTag();
        putDisplay(quest, display);
        putSettingsDefaults(quest, hiddenMode);
        quest.put(QuestDefinition.PREREQUISITES_FIELD, new ListTag());
        quest.put("connection_colors", new CompoundTag());
        quest.put("connection_modes", new CompoundTag());
        quest.put("hidden_connections", new ListTag());
        quest.put("tasks", new CompoundTag());
        quest.put("tasks_order", new ListTag());
        quest.put("rewards", new CompoundTag());
        quest.put("rewards_order", new ListTag());
        return quest;
    }

    private static void putDisplay(CompoundTag quest, QuestDisplay display) {
        QuestDisplay safe = display == null ? QuestDisplay.DEFAULT : display;
        quest.putString("title", safe.title());
        quest.putString("subtitle", safe.subtitle());
        quest.put("description", stringListTag(safe.description()));
        quest.putString("icon", safe.icon());
        quest.putString("icon_background", safe.iconBackground());
        quest.putString("completion_sound", safe.completionSound());
        quest.putInt("completion_sound_volume", safe.completionSoundVolume());
        quest.putString("completion_hud_background", safe.completionHudBackground());
        quest.putBoolean("visual_hidden", safe.visualHidden());
        quest.putString("quest_background", safe.questBackground());
        quest.putBoolean("quest_background_grayscale", safe.questBackgroundGrayscale());
        quest.put("groups", groupsTag(safe.groups()));
    }

    private static void putSettingsDefaults(CompoundTag quest, QuestVisibilityMode hiddenMode) {
        QuestVisibilityMode mode = hiddenMode == null ? QuestSettings.DEFAULT.hiddenMode() : hiddenMode;
        quest.putBoolean("repeatable", QuestSettings.DEFAULT.repeatable());
        quest.putString("hidden_mode", mode.serializedName());
        quest.putBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD, QuestSettings.DEFAULT.showPrerequisiteArrow());
    }

    private static void setProgressDefaults(CompoundTag quest, boolean unlocked) {
        quest.putBoolean("completed", false);
        quest.putBoolean("unlocked", unlocked);
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
        quest.put("tasks", tasks);
    }

    private static void ensureDisplayDefaults(CompoundTag quest) {
        if (!quest.contains("title", Tag.TAG_STRING)) {
            quest.putString("title", QuestDisplay.DEFAULT.title());
        }
        if (!quest.contains("subtitle", Tag.TAG_STRING)) {
            quest.putString("subtitle", QuestDisplay.DEFAULT_SUBTITLE);
        }
        if (!quest.contains("description", Tag.TAG_LIST)) {
            quest.put("description", new ListTag());
        }
        quest.putString("icon", QuestDisplay.normalizeIcon(quest.getString("icon")));
        quest.putString("icon_background", QuestDisplay.normalizeIconBackground(quest.getString("icon_background")));
        String completionSound = quest.getString("completion_sound");
        quest.putString("completion_sound", completionSound == null || completionSound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : completionSound.trim());
        if (!quest.contains("completion_sound_volume", Tag.TAG_INT)) {
            quest.putInt("completion_sound_volume", QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME);
        } else {
            quest.putInt("completion_sound_volume", QuestDisplay.normalizeCompletionSoundVolume(quest.getInt("completion_sound_volume")));
        }
        quest.putString("completion_hud_background", QuestDisplay.normalizeCompletionHudBackground(quest.getString("completion_hud_background")));
        if (!quest.contains("visual_hidden", Tag.TAG_BYTE)) {
            quest.putBoolean("visual_hidden", QuestDisplay.DEFAULT_VISUAL_HIDDEN);
        }
        quest.putString("quest_background", QuestDisplay.normalizeQuestBackground(quest.getString("quest_background")));
        if (!quest.contains("quest_background_grayscale", Tag.TAG_BYTE)) {
            quest.putBoolean("quest_background_grayscale", QuestDisplay.DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
        }
    }

    private static void ensureDefinitionBuckets(CompoundTag quest) {
        if (!quest.contains(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_LIST)) {
            quest.put(QuestDefinition.PREREQUISITES_FIELD, new ListTag());
        }
        if (!quest.contains("connection_colors", Tag.TAG_COMPOUND)) {
            quest.put("connection_colors", new CompoundTag());
        }
        if (!quest.contains("connection_modes", Tag.TAG_COMPOUND)) {
            quest.put("connection_modes", new CompoundTag());
        }
        if (!quest.contains("hidden_connections", Tag.TAG_LIST)) {
            quest.put("hidden_connections", new ListTag());
        }
        if (!quest.contains("tasks", Tag.TAG_COMPOUND)) {
            quest.put("tasks", new CompoundTag());
        }
        if (!quest.contains("tasks_order", Tag.TAG_LIST)) {
            quest.put("tasks_order", new ListTag());
        }
        if (!quest.contains("rewards", Tag.TAG_COMPOUND)) {
            quest.put("rewards", new CompoundTag());
        }
        if (!quest.contains("rewards_order", Tag.TAG_LIST)) {
            quest.put("rewards_order", new ListTag());
        }
    }

    private static void replaceSingleGroup(CompoundTag quest, String group, int x, int y, float scale) {
        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean("visible", true);
        groupTag.putInt("x", x);
        groupTag.putInt("y", y);
        float normalizedScale = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat("scale", Math.max(0.5f, normalizedScale));
        groups.put(group, groupTag);
        quest.put("groups", groups);
    }

    private static ListTag stringListTag(List<String> values) {
        ListTag out = new ListTag();
        if (values == null) {
            return out;
        }
        for (String value : values) {
            if (value != null) {
                out.add(StringTag.valueOf(value));
            }
        }
        return out;
    }

    private static CompoundTag groupsTag(Map<String, ChapterDefinition> groups) {
        CompoundTag out = new CompoundTag();
        if (groups == null || groups.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, ChapterDefinition> entry : groups.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            ChapterDefinition view = entry.getValue();
            CompoundTag groupTag = new CompoundTag();
            groupTag.putBoolean("visible", view.visible());
            groupTag.putInt("x", view.x());
            groupTag.putInt("y", view.y());
            groupTag.putFloat("scale", view.scale());
            out.put(entry.getKey(), groupTag);
        }
        return out;
    }
}
