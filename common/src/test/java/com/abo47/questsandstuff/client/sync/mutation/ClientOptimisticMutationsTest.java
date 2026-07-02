package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
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
        ClientQuestMutator.createEditorQuestLocal("quest/new", "main", 12, 34, " New Quest ");

        ClientQuestMutator.setQuestDisplayLocal("quest/new", "Better Title", "Better Subtitle");
        ClientQuestMutator.setQuestDescriptionLocal("quest/new", List.of("line one", "line two"));
        ClientQuestMutator.setQuestIconLocal("quest/new", " minecraft:diamond ");
        ClientQuestMutator.setQuestRepeatableLocal("quest/new", true);
        ClientQuestMutator.setQuestVisualHiddenLocal("quest/new", true);
        ClientQuestMutator.setQuestCompletionSoundLocal("quest/new", " minecraft:block.note_block.pling ");
        ClientQuestMutator.setQuestCompletionSoundVolumeLocal("quest/new", 250);
        ClientQuestMutator.setQuestCompletionHudBackgroundLocal("quest/new", " hud/rare ");
        ClientQuestMutator.setQuestBackgroundLocal("quest/new", " quest/rare ", true);
        ClientQuestMutator.setQuestPositionInGroupLocal("quest/new", "main", 90, 120);
        ClientQuestMutator.setQuestScaleInGroupLocal("quest/new", "main", -4.0f);

        ClientQuestMutator.putQuestTaskJsonLocal("quest/new", taskJson("task/a", "First"));
        ClientQuestMutator.putQuestTaskJsonLocal("quest/new", taskJson("task/b", "Second"));
        ClientQuestMutator.putQuestTaskJsonLocal("quest/new", taskJson("task/a", "Updated"));
        ClientQuestMutator.putQuestRewardJsonLocal("quest/new", rewardJson("reward/a"));

        CompoundTag quest = ClientQuestCache.quest("quest/new");
        assertEquals("Better Title", quest.getString(SyncKeys.Quest.TITLE));
        assertEquals("Better Subtitle", quest.getString(SyncKeys.Quest.SUBTITLE));
        assertIterableEquals(List.of("line one", "line two"), strings(quest.getList(SyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING)));
        assertEquals("minecraft:diamond", quest.getString(SyncKeys.Quest.ICON));
        assertTrue(quest.getBoolean(SyncKeys.Quest.REPEATABLE));
        assertTrue(quest.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals("minecraft:block.note_block.pling", quest.getString(SyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(QuestDisplay.normalizeCompletionSoundVolume(250), quest.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals("hud/rare", quest.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals("quest/rare", quest.getString(SyncKeys.Quest.QUEST_BACKGROUND));
        assertTrue(quest.getBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));

        CompoundTag mainView = quest.getCompound(SyncKeys.Quest.GROUPS).getCompound("main");
        assertEquals(90, mainView.getInt(SyncKeys.ChapterView.X));
        assertEquals(120, mainView.getInt(SyncKeys.ChapterView.Y));
        assertEquals(0.5f, mainView.getFloat(SyncKeys.ChapterView.SCALE), 0.0001f);

        CompoundTag tasks = quest.getCompound(SyncKeys.Quest.TASKS);
        assertEquals(taskJson("task/a", "Updated"), tasks.getCompound("task/a").getString(SyncKeys.Objective.JSON));
        assertEquals("questsandstuff:check", tasks.getCompound("task/a").getString(SyncKeys.Objective.TYPE));
        assertEquals(taskJson("task/b", "Second"), tasks.getCompound("task/b").getString(SyncKeys.Objective.JSON));
        assertIterableEquals(List.of("task/a", "task/b"), strings(quest.getList(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING)));

        CompoundTag rewards = quest.getCompound(SyncKeys.Quest.REWARDS);
        assertEquals(rewardJson("reward/a"), rewards.getCompound("reward/a").getString(SyncKeys.Objective.JSON));
        assertEquals("questsandstuff:xp", rewards.getCompound("reward/a").getString(SyncKeys.Objective.TYPE));
        assertIterableEquals(List.of("reward/a"), strings(quest.getList(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING)));
    }

    @Test
    void prerequisiteMutationsUpdateUnlockStateAndConnectionMetadata() {
        ClientQuestMutator.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestMutator.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");

        ClientQuestMutator.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestMutator.setConnectionColorLocal("quest/child", "quest/parent", 0x112233);
        ClientQuestMutator.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestMutator.setConnectionHiddenLocal("quest/child", "quest/parent", true);

        CompoundTag child = ClientQuestCache.quest("quest/child");
        assertIterableEquals(List.of("quest/parent"), strings(child.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING)));
        assertFalse(child.getBoolean(SyncKeys.Quest.UNLOCKED));
        assertEquals(0x112233, child.getCompound(SyncKeys.Quest.CONNECTION_COLORS).getInt("quest/parent"));
        assertEquals("grid", child.getCompound(SyncKeys.Quest.CONNECTION_MODES).getString("quest/parent"));
        assertIterableEquals(List.of("quest/parent"), strings(child.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING)));

        ClientQuestState.mutableQuest("quest/parent").putBoolean(SyncKeys.Quest.COMPLETED, true);
        ClientQuestConnectionMutator.refreshLocalUnlockState(ClientQuestState.mutableQuest("quest/child"));
        assertTrue(ClientQuestCache.quest("quest/child").getBoolean(SyncKeys.Quest.UNLOCKED));

        ClientQuestMutator.setQuestPrerequisiteLocal("quest/child", "quest/parent", false);
        child = ClientQuestCache.quest("quest/child");
        assertTrue(child.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertTrue(child.getBoolean(SyncKeys.Quest.UNLOCKED));
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
    }

    @Test
    void copyAndRemoveMutationsKeepReferencesAndPinnedStateConsistent() {
        ClientQuestMutator.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestMutator.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");
        ClientQuestMutator.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestMutator.setConnectionColorLocal("quest/child", "quest/parent", 0x445566);
        ClientQuestMutator.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestMutator.setConnectionHiddenLocal("quest/child", "quest/parent", true);
        ClientQuestMutator.setQuestClaimedLocal("quest/child", true);
        ClientQuestState.mutableQuest("quest/child").putBoolean(SyncKeys.Quest.COMPLETED, true);
        ClientQuestState.mutableQuest("quest/child").putFloat(SyncKeys.Quest.PROGRESS, 1.0f);
        ClientQuestCache.togglePinnedLocal("quest/parent");

        ClientQuestMutator.copyQuestLocal(
                "quest/child",
                "quest/child_copy",
                "copies",
                16,
                24,
                1.25f,
                Map.of("quest/parent", "quest/parent_copy", "quest/child", "quest/child_copy")
        );

        CompoundTag copy = ClientQuestCache.quest("quest/child_copy");
        assertIterableEquals(List.of("quest/parent_copy"), strings(copy.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING)));
        assertEquals(0x445566, copy.getCompound(SyncKeys.Quest.CONNECTION_COLORS).getInt("quest/parent_copy"));
        assertEquals("grid", copy.getCompound(SyncKeys.Quest.CONNECTION_MODES).getString("quest/parent_copy"));
        assertIterableEquals(List.of("quest/parent_copy"), strings(copy.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING)));
        assertFalse(copy.getBoolean(SyncKeys.Quest.COMPLETED));
        assertFalse(copy.getBoolean(SyncKeys.Quest.UNLOCKED));
        assertFalse(copy.getBoolean(SyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, copy.getFloat(SyncKeys.Quest.PROGRESS), 0.0001f);
        assertGroupView(copy, "copies", 16, 24, 1.25f);

        ClientQuestMutator.removeQuestLocal("quest/parent");
        CompoundTag child = ClientQuestCache.quest("quest/child");
        assertFalse(ClientQuestCache.containsQuest("quest/parent"));
        assertEquals(Set.of(), ClientQuestCache.pinned());
        assertTrue(child.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
    }

    @Test
    void canvasMutationsMaintainLayerStateAndOrder() {
        ClientQuestMutator.createGroupLocal("main");
        CanvasImageLayer image = new CanvasImageLayer("image/a", "item:minecraft:diamond", 10, 20, 40, 50, 0);
        CanvasTextLayer text = new CanvasTextLayer("text/a", "Label", 60, 70, 80, 30, 0, "center", "bold", 0xFFFFFF);

        ClientCanvasMutator.putCanvasImageLocal(" main ", image);
        ClientCanvasMutator.putCanvasTextLocal("main", text);

        assertEquals(List.of(image), ClientQuestCache.canvasImages("main"));
        assertEquals(List.of(text), ClientQuestCache.canvasTexts("main"));
        assertIterableEquals(List.of("image:image/a", "text:text/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasMutator.setCanvasLayerOrderLocal("main", Arrays.asList("text:text/a", "", null, "image:image/a", "text:text/a"));
        assertIterableEquals(List.of("text:text/a", "image:image/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasMutator.removeCanvasImageLocal("main", "image/a");
        assertTrue(ClientQuestCache.canvasImages("main").isEmpty());
        assertIterableEquals(List.of("text:text/a"), ClientQuestCache.canvasLayerOrder("main"));

        ClientCanvasMutator.removeCanvasTextLocal("main", "text/a");
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
        CompoundTag groups = quest.getCompound(SyncKeys.Quest.GROUPS);
        assertEquals(Set.of(group), groups.getAllKeys());
        CompoundTag view = groups.getCompound(group);
        assertTrue(view.getBoolean(SyncKeys.ChapterView.VISIBLE));
        assertEquals(x, view.getInt(SyncKeys.ChapterView.X));
        assertEquals(y, view.getInt(SyncKeys.ChapterView.Y));
        assertEquals(scale, view.getFloat(SyncKeys.ChapterView.SCALE), 0.0001f);
    }
}
