package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.quest.model.GroupDef;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
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
        QuestDisplay display = QuestDisplay.forNewQuest(title, Map.of(group, new GroupDef(true, x, y, 1.0f)));
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
        quest.put(SyncKeys.Quest.PREREQUISITES, new ListTag());
        quest.put(SyncKeys.Quest.CONNECTION_COLORS, new CompoundTag());
        quest.put(SyncKeys.Quest.CONNECTION_MODES, new CompoundTag());
        quest.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, new ListTag());
        quest.put(SyncKeys.Quest.CONNECTION_TEXTURES, new CompoundTag());
        quest.put(SyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS, new CompoundTag());
        quest.put(SyncKeys.Quest.TASKS, new CompoundTag());
        quest.put(SyncKeys.Quest.TASKS_ORDER, new ListTag());
        quest.put(SyncKeys.Quest.REWARDS, new CompoundTag());
        quest.put(SyncKeys.Quest.REWARDS_ORDER, new ListTag());
        return quest;
    }

    private static void putDisplay(CompoundTag quest, QuestDisplay display) {
        QuestDisplay safe = display == null ? QuestDisplay.DEFAULT : display;
        quest.putString(SyncKeys.Quest.TITLE, safe.title());
        quest.putString(SyncKeys.Quest.SUBTITLE, safe.subtitle());
        quest.put(SyncKeys.Quest.DESCRIPTION, stringListTag(safe.description()));
        quest.putString(SyncKeys.Quest.ICON, safe.icon());
        quest.putString(SyncKeys.Quest.ICON_BACKGROUND, safe.iconBackground());
        quest.putString(SyncKeys.Quest.COMPLETION_SOUND, safe.completionSound());
        quest.putInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, safe.completionSoundVolume());
        quest.putString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND, safe.completionHudBackground());
        quest.putBoolean(SyncKeys.Quest.VISUAL_HIDDEN, safe.visualHidden());
        quest.putString(SyncKeys.Quest.QUEST_BACKGROUND, safe.questBackground());
        quest.putBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, safe.questBackgroundGrayscale());
        quest.put(SyncKeys.Quest.GROUPS, groupsTag(safe.groups()));
    }

    private static void putSettingsDefaults(CompoundTag quest, QuestVisibilityMode hiddenMode) {
        QuestVisibilityMode mode = hiddenMode == null ? QuestSettings.DEFAULT.hiddenMode() : hiddenMode;
        quest.putBoolean(SyncKeys.Quest.REPEATABLE, QuestSettings.DEFAULT.repeatable());
        quest.putString(SyncKeys.Quest.HIDDEN_MODE, mode.serializedName());
        quest.putBoolean(SyncKeys.Quest.SHOW_PREREQUISITE_ARROW, QuestSettings.DEFAULT.showPrerequisiteArrow());
    }

    private static void setProgressDefaults(CompoundTag quest, boolean unlocked) {
        quest.putBoolean(SyncKeys.Quest.COMPLETED, false);
        quest.putBoolean(SyncKeys.Quest.UNLOCKED, unlocked);
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
        quest.put(SyncKeys.Quest.TASKS, tasks);
    }

    private static void ensureDisplayDefaults(CompoundTag quest) {
        if (!quest.contains(SyncKeys.Quest.TITLE, Tag.TAG_STRING)) {
            quest.putString(SyncKeys.Quest.TITLE, QuestDisplay.DEFAULT.title());
        }
        if (!quest.contains(SyncKeys.Quest.SUBTITLE, Tag.TAG_STRING)) {
            quest.putString(SyncKeys.Quest.SUBTITLE, QuestDisplay.DEFAULT_SUBTITLE);
        }
        if (!quest.contains(SyncKeys.Quest.DESCRIPTION, Tag.TAG_LIST)) {
            quest.put(SyncKeys.Quest.DESCRIPTION, new ListTag());
        }
        quest.putString(SyncKeys.Quest.ICON, QuestDisplay.normalizeIcon(quest.getString(SyncKeys.Quest.ICON)));
        quest.putString(SyncKeys.Quest.ICON_BACKGROUND, QuestDisplay.normalizeIconBackground(quest.getString(SyncKeys.Quest.ICON_BACKGROUND)));
        String completionSound = quest.getString(SyncKeys.Quest.COMPLETION_SOUND);
        quest.putString(SyncKeys.Quest.COMPLETION_SOUND, completionSound == null || completionSound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : completionSound.trim());
        if (!quest.contains(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, Tag.TAG_INT)) {
            quest.putInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME);
        } else {
            quest.putInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, QuestDisplay.normalizeCompletionSoundVolume(quest.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME)));
        }
        quest.putString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND, QuestDisplay.normalizeCompletionHudBackground(quest.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND)));
        if (!quest.contains(SyncKeys.Quest.VISUAL_HIDDEN, Tag.TAG_BYTE)) {
            quest.putBoolean(SyncKeys.Quest.VISUAL_HIDDEN, QuestDisplay.DEFAULT_VISUAL_HIDDEN);
        }
        quest.putString(SyncKeys.Quest.QUEST_BACKGROUND, QuestDisplay.normalizeQuestBackground(quest.getString(SyncKeys.Quest.QUEST_BACKGROUND)));
        if (!quest.contains(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, Tag.TAG_BYTE)) {
            quest.putBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, QuestDisplay.DEFAULT_QUEST_BACKGROUND_GRAYSCALE);
        }
    }

    private static void ensureDefinitionBuckets(CompoundTag quest) {
        if (!quest.contains(SyncKeys.Quest.PREREQUISITES, Tag.TAG_LIST)) {
            quest.put(SyncKeys.Quest.PREREQUISITES, new ListTag());
        }
        if (!quest.contains(SyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.CONNECTION_COLORS, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.CONNECTION_MODES, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            quest.put(SyncKeys.Quest.HIDDEN_CONNECTIONS, new ListTag());
        }
        if (!quest.contains(SyncKeys.Quest.CONNECTION_TEXTURES, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.CONNECTION_TEXTURES, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.TASKS, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.TASKS, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_LIST)) {
            quest.put(SyncKeys.Quest.TASKS_ORDER, new ListTag());
        }
        if (!quest.contains(SyncKeys.Quest.REWARDS, Tag.TAG_COMPOUND)) {
            quest.put(SyncKeys.Quest.REWARDS, new CompoundTag());
        }
        if (!quest.contains(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_LIST)) {
            quest.put(SyncKeys.Quest.REWARDS_ORDER, new ListTag());
        }
    }

    private static void replaceSingleGroup(CompoundTag quest, String group, int x, int y, float scale) {
        CompoundTag groups = new CompoundTag();
        CompoundTag groupTag = new CompoundTag();
        groupTag.putBoolean(SyncKeys.ChapterView.VISIBLE, true);
        groupTag.putInt(SyncKeys.ChapterView.X, x);
        groupTag.putInt(SyncKeys.ChapterView.Y, y);
        float normalizedScale = Float.isNaN(scale) || Float.isInfinite(scale) ? 1.0f : scale;
        groupTag.putFloat(SyncKeys.ChapterView.SCALE, Math.max(0.5f, normalizedScale));
        groups.put(group, groupTag);
        quest.put(SyncKeys.Quest.GROUPS, groups);
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

    private static CompoundTag groupsTag(Map<String, GroupDef> groups) {
        CompoundTag out = new CompoundTag();
        if (groups == null || groups.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, GroupDef> entry : groups.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
                continue;
            }
            GroupDef view = entry.getValue();
            CompoundTag groupTag = new CompoundTag();
            groupTag.putBoolean(SyncKeys.ChapterView.VISIBLE, view.visible());
            groupTag.putInt(SyncKeys.ChapterView.X, view.x());
            groupTag.putInt(SyncKeys.ChapterView.Y, view.y());
            groupTag.putFloat(SyncKeys.ChapterView.SCALE, view.scale());
            out.put(entry.getKey(), groupTag);
        }
        return out;
    }
}
