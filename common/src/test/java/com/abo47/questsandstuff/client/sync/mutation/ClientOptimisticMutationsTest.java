package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientOptimisticMutationsTest {
    @BeforeEach
    void resetClientState() {
        ClientQuestCache.resetStateForTests();
    }

    @Test
    void questFieldAndObjectiveMutationsKeepLocalSnapshotCoherent() {
        ClientQuestLocalMutations.createEditorQuestLocal("quest/new", "main", 12, 34, " New Quest ");

        ClientQuestLocalMutations.setQuestDisplayLocal("quest/new", "Better Title", "Better Subtitle");
        ClientQuestLocalMutations.setQuestDescriptionLocal("quest/new", List.of("line one", "line two"));
        ClientQuestLocalMutations.setQuestIconLocal("quest/new", " minecraft:diamond ");
        ClientQuestLocalMutations.setQuestRepeatableLocal("quest/new", true);
        ClientQuestLocalMutations.setQuestVisualHiddenLocal("quest/new", true);
        ClientQuestLocalMutations.setQuestCompletionSoundLocal("quest/new", " minecraft:block.note_block.pling ");
        ClientQuestLocalMutations.setQuestCompletionSoundVolumeLocal("quest/new", 250);
        ClientQuestLocalMutations.setQuestCompletionHudBackgroundLocal("quest/new", " hud/rare ");
        ClientQuestLocalMutations.setQuestBackgroundLocal("quest/new", " quest/rare ", true);
        ClientQuestLocalMutations.setQuestPositionInGroupLocal("quest/new", "main", 90, 120);
        ClientQuestLocalMutations.setQuestScaleInGroupLocal("quest/new", "main", -4.0f);

        ClientQuestLocalMutations.putQuestTaskJsonLocal("quest/new", taskJson("task/a", "First"));
        ClientQuestLocalMutations.putQuestTaskJsonLocal("quest/new", taskJson("task/b", "Second"));
        ClientQuestLocalMutations.putQuestTaskJsonLocal("quest/new", taskJson("task/a", "Updated"));
        ClientQuestLocalMutations.putQuestRewardJsonLocal("quest/new", rewardJson("reward/a"));

        CompoundTag quest = ClientQuestCache.quest("quest/new");
        assertEquals("Better Title", quest.getString(QuestSyncKeys.Quest.TITLE));
        assertEquals("Better Subtitle", quest.getString(QuestSyncKeys.Quest.SUBTITLE));
        assertIterableEquals(List.of("line one", "line two"), strings(quest.getList(QuestSyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING)));
        assertEquals("minecraft:diamond", quest.getString(QuestSyncKeys.Quest.ICON));
        assertTrue(quest.getBoolean(QuestSyncKeys.Quest.REPEATABLE));
        assertTrue(quest.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals("minecraft:block.note_block.pling", quest.getString(QuestSyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(QuestDisplay.normalizeCompletionSoundVolume(250), quest.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals("hud/rare", quest.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals("quest/rare", quest.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND));
        assertTrue(quest.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));

        CompoundTag mainView = quest.getCompound(QuestSyncKeys.Quest.GROUPS).getCompound("main");
        assertEquals(90, mainView.getInt(QuestSyncKeys.ChapterView.X));
        assertEquals(120, mainView.getInt(QuestSyncKeys.ChapterView.Y));
        assertEquals(0.5f, mainView.getFloat(QuestSyncKeys.ChapterView.SCALE), 0.0001f);

        CompoundTag tasks = quest.getCompound(QuestSyncKeys.Quest.TASKS);
        assertEquals(taskJson("task/a", "Updated"), tasks.getCompound("task/a").getString(QuestSyncKeys.Objective.JSON));
        assertEquals("questsandstuff:check", tasks.getCompound("task/a").getString(QuestSyncKeys.Objective.TYPE));
        assertEquals(taskJson("task/b", "Second"), tasks.getCompound("task/b").getString(QuestSyncKeys.Objective.JSON));
        assertIterableEquals(List.of("task/a", "task/b"), strings(quest.getList(QuestSyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING)));

        CompoundTag rewards = quest.getCompound(QuestSyncKeys.Quest.REWARDS);
        assertEquals(rewardJson("reward/a"), rewards.getCompound("reward/a").getString(QuestSyncKeys.Objective.JSON));
        assertEquals("questsandstuff:xp", rewards.getCompound("reward/a").getString(QuestSyncKeys.Objective.TYPE));
        assertIterableEquals(List.of("reward/a"), strings(quest.getList(QuestSyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING)));
    }

    @Test
    void prerequisiteMutationsUpdateUnlockStateAndConnectionMetadata() {
        ClientQuestLocalMutations.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestLocalMutations.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");

        ClientQuestLocalMutations.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestLocalMutations.setConnectionColorLocal("quest/child", "quest/parent", 0x112233);
        ClientQuestLocalMutations.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestLocalMutations.setConnectionHiddenLocal("quest/child", "quest/parent", true);

        CompoundTag child = ClientQuestCache.quest("quest/child");
        assertIterableEquals(List.of("quest/parent"), strings(child.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING)));
        assertFalse(child.getBoolean(QuestSyncKeys.Quest.UNLOCKED));
        assertEquals(0x112233, child.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).getInt("quest/parent"));
        assertEquals("grid", child.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).getString("quest/parent"));
        assertIterableEquals(List.of("quest/parent"), strings(child.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING)));

        ClientQuestState.mutableQuest("quest/parent").putBoolean(QuestSyncKeys.Quest.COMPLETED, true);
        ClientQuestConnectionMutations.refreshLocalUnlockState(ClientQuestState.mutableQuest("quest/child"));
        assertTrue(ClientQuestCache.quest("quest/child").getBoolean(QuestSyncKeys.Quest.UNLOCKED));

        ClientQuestLocalMutations.setQuestPrerequisiteLocal("quest/child", "quest/parent", false);
        child = ClientQuestCache.quest("quest/child");
        assertTrue(child.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertTrue(child.getBoolean(QuestSyncKeys.Quest.UNLOCKED));
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
    }

    @Test
    void copyAndRemoveMutationsKeepReferencesAndPinnedStateConsistent() {
        ClientQuestLocalMutations.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestLocalMutations.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");
        ClientQuestLocalMutations.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestLocalMutations.setConnectionColorLocal("quest/child", "quest/parent", 0x445566);
        ClientQuestLocalMutations.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestLocalMutations.setConnectionHiddenLocal("quest/child", "quest/parent", true);
        ClientQuestLocalMutations.setQuestClaimedLocal("quest/child", true);
        ClientQuestState.mutableQuest("quest/child").putBoolean(QuestSyncKeys.Quest.COMPLETED, true);
        ClientQuestState.mutableQuest("quest/child").putFloat(QuestSyncKeys.Quest.PROGRESS, 1.0f);
        ClientQuestCache.togglePinnedLocal("quest/parent");

        ClientQuestLocalMutations.copyQuestLocal(
                "quest/child",
                "quest/child_copy",
                "copies",
                16,
                24,
                1.25f,
                Map.of("quest/parent", "quest/parent_copy", "quest/child", "quest/child_copy")
        );

        CompoundTag copy = ClientQuestCache.quest("quest/child_copy");
        assertIterableEquals(List.of("quest/parent_copy"), strings(copy.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING)));
        assertEquals(0x445566, copy.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).getInt("quest/parent_copy"));
        assertEquals("grid", copy.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).getString("quest/parent_copy"));
        assertIterableEquals(List.of("quest/parent_copy"), strings(copy.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING)));
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.COMPLETED));
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.UNLOCKED));
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, copy.getFloat(QuestSyncKeys.Quest.PROGRESS), 0.0001f);
        assertGroupView(copy, "copies", 16, 24, 1.25f);

        ClientQuestLocalMutations.removeQuestLocal("quest/parent");
        CompoundTag child = ClientQuestCache.quest("quest/child");
        assertFalse(ClientQuestCache.containsQuest("quest/parent"));
        assertEquals(Set.of(), ClientQuestCache.pinned());
        assertTrue(child.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
    }

    @Test
    void canvasMutationsMaintainLayerStateAndOrder() {
        ClientQuestLocalMutations.createGroupLocal("main");
        CanvasImageLayer image = new CanvasImageLayer("image/a", "item:minecraft:diamond", 10, 20, 40, 50, 0);
        CanvasTextLayer text = new CanvasTextLayer("text/a", "Label", 60, 70, 80, 30, 0, "center", "bold", 0xFFFFFF);

        ClientCanvasLocalMutations.putCanvasImageLocal(" main ", image);
        ClientCanvasLocalMutations.putCanvasTextLocal("main", text);

        assertEquals(List.of(image), ClientQuestCache.canvasImages("main"));
        assertEquals(List.of(text), ClientQuestCache.canvasTexts("main"));
        assertIterableEquals(List.of("image:image/a", "text:text/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasLocalMutations.setCanvasLayerOrderLocal("main", Arrays.asList("text:text/a", "", null, "image:image/a", "text:text/a"));
        assertIterableEquals(List.of("text:text/a", "image:image/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasLocalMutations.removeCanvasImageLocal("main", "image/a");
        assertTrue(ClientQuestCache.canvasImages("main").isEmpty());
        assertIterableEquals(List.of("text:text/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasLocalMutations.removeCanvasTextLocal("main", "text/a");
        assertTrue(ClientQuestCache.canvasTexts("main").isEmpty());
        assertTrue(ClientQuestCache.canvasLayerOrder("main").isEmpty());
    }

    private static String taskJson(String id, String label) {
        return "{\"id\":\"" + id + "\",\"type\":\"questsandstuff:check\",\"label\":\"" + label + "\"}";
    }

    private static String rewardJson(String id) {
        return "{\"id\":\"" + id + "\",\"type\":\"questsandstuff:xp\",\"amount\":5,\"mode\":\"points\"}";
    }

    private static List<String> strings(ListTag tag) {
        return tag.stream()
                .map(value -> value.getAsString())
                .toList();
    }

    private static void assertGroupView(CompoundTag quest, String group, int x, int y, float scale) {
        CompoundTag groups = quest.getCompound(QuestSyncKeys.Quest.GROUPS);
        assertEquals(Set.of(group), groups.getAllKeys());
        CompoundTag view = groups.getCompound(group);
        assertTrue(view.getBoolean(QuestSyncKeys.ChapterView.VISIBLE));
        assertEquals(x, view.getInt(QuestSyncKeys.ChapterView.X));
        assertEquals(y, view.getInt(QuestSyncKeys.ChapterView.Y));
        assertEquals(scale, view.getFloat(QuestSyncKeys.ChapterView.SCALE), 0.0001f);
    }
}
