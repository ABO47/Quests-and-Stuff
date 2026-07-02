package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncChunkAccumulator;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier;
import com.abo47.questsandstuff.quest.model.GroupDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.reward.QuestRewards;
import com.abo47.questsandstuff.quest.model.reward.XpQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTasks;
import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.abo47.questsandstuff.quest.model.task.generic.CheckQuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyncPayloadBuilderTest {
    @TempDir
    Path root;

    @Test
    void editorPayloadSortsPrerequisitesAndFiltersConnectionModes() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "quest_a",
                new QuestDisplay(
                        "Title",
                        "Subtitle",
                        java.util.List.of(),
                        Map.of("main", GroupDef.DEFAULT),
                        QuestDisplay.DEFAULT_ICON,
                        QuestDisplay.DEFAULT_ICON_BACKGROUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME,
                        "hud/quest_a.png",
                        QuestDisplay.DEFAULT_VISUAL_HIDDEN,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND_GRAYSCALE
                ),
                QuestSettings.DEFAULT,
                Set.of("z_parent", "a_parent"),
                Map.of(),
                Map.of("a_parent", "grid", "z_parent", "free"),
                Set.of("z_parent", "a_parent"),
                Map.of(),
                Map.of()
        );

        CompoundTag payload = new SyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition);

        ListTag prerequisites = payload.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
        assertEquals("a_parent", prerequisites.getString(0));
        assertEquals("z_parent", prerequisites.getString(1));

        CompoundTag modes = payload.getCompound(SyncKeys.Quest.CONNECTION_MODES);
        assertEquals("grid", modes.getString("a_parent"));
        assertFalse(modes.contains("z_parent"));
        assertEquals("hud/quest_a.png", payload.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND));

        ListTag hidden = payload.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
        assertEquals("a_parent", hidden.getString(0));
        assertEquals("z_parent", hidden.getString(1));
    }

    @Test
    void editorPayloadUsesSharedSyncKeySchema() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "quest/keyed",
                QuestDisplay.forNewQuest("Keyed", Map.of("main", new GroupDef(true, 12, 34, 1.25f))),
                QuestSettings.DEFAULT,
                Set.of("quest/parent"),
                Map.of("quest/parent", 0x112233),
                Map.of("quest/parent", "grid"),
                Set.of("quest/parent"),
                Map.of(),
                Map.of(),
                List.of("task/check"),
                List.of("reward/xp"),
                Map.of("task/check", new CheckQuestTaskDefinition("task/check", QuestTasks.id("check"), "")),
                Map.of("reward/xp", new XpQuestRewardDefinition("reward/xp", QuestRewards.id("xp"), 7, XpMode.POINTS))
        );
        PlayerQuestState playerState = new PlayerQuestState();
        playerState.quest("quest/keyed").setUnlocked(true);

        CompoundTag payload = new SyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition, playerState);

        assertTrue(payload.contains(SyncKeys.Quest.TITLE, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.SUBTITLE, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.DESCRIPTION, Tag.TAG_LIST));
        assertTrue(payload.contains(SyncKeys.Quest.ICON, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.ICON_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.COMPLETION_SOUND, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.COMPLETION_SOUND_VOLUME, Tag.TAG_INT));
        assertTrue(payload.contains(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.VISUAL_HIDDEN, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.QUEST_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.COMPLETED, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.UNLOCKED, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.CLAIMED, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.PROGRESS, Tag.TAG_FLOAT));
        assertTrue(payload.contains(SyncKeys.Quest.REPEATABLE, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.HIDDEN_MODE, Tag.TAG_STRING));
        assertTrue(payload.contains(SyncKeys.Quest.SHOW_PREREQUISITE_ARROW, Tag.TAG_BYTE));
        assertTrue(payload.contains(SyncKeys.Quest.TASKS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_LIST));
        assertTrue(payload.contains(SyncKeys.Quest.REWARDS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_LIST));
        assertTrue(payload.contains(SyncKeys.Quest.PREREQUISITES, Tag.TAG_LIST));
        assertTrue(payload.contains(SyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(SyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST));
        assertTrue(payload.contains(SyncKeys.Quest.GROUPS, Tag.TAG_COMPOUND));

        assertEquals("task/check", payload.getList(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING).getString(0));
        CompoundTag task = payload.getCompound(SyncKeys.Quest.TASKS).getCompound("task/check");
        assertTrue(task.contains(SyncKeys.Task.TYPE, Tag.TAG_STRING));
        assertTrue(task.contains(SyncKeys.Task.JSON, Tag.TAG_STRING));
        assertTrue(task.contains(SyncKeys.Task.PROGRESS, Tag.TAG_FLOAT));
        assertTrue(task.contains(SyncKeys.Task.COMPLETE, Tag.TAG_BYTE));
        assertTrue(task.contains(SyncKeys.Task.COUNT, Tag.TAG_INT));

        assertEquals("reward/xp", payload.getList(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING).getString(0));
        CompoundTag reward = payload.getCompound(SyncKeys.Quest.REWARDS).getCompound("reward/xp");
        assertTrue(reward.contains(SyncKeys.Task.TYPE, Tag.TAG_STRING));
        assertTrue(reward.contains(SyncKeys.Task.JSON, Tag.TAG_STRING));
        assertTrue(reward.contains(SyncKeys.Task.SELECTABLE, Tag.TAG_BYTE));
        assertTrue(reward.contains(SyncKeys.Task.MASS_CLAIMABLE, Tag.TAG_BYTE));

        CompoundTag groupView = payload.getCompound(SyncKeys.Quest.GROUPS).getCompound("main");
        assertTrue(groupView.getBoolean(SyncKeys.ChapterView.VISIBLE));
        assertEquals(12, groupView.getInt(SyncKeys.ChapterView.X));
        assertEquals(34, groupView.getInt(SyncKeys.ChapterView.Y));
        assertEquals(1.25f, groupView.getFloat(SyncKeys.ChapterView.SCALE), 0.0001f);
    }

    @Test
    void chunkAccumulatorJoinsPayloadsThroughSharedSyncKeys() {
        ClientSyncChunkAccumulator full = new ClientSyncChunkAccumulator(2);
        CompoundTag fullPartA = new CompoundTag();
        ListTag groups = new ListTag();
        groups.add(StringTag.valueOf("main"));
        fullPartA.put(SyncKeys.GROUPS, groups);
        CompoundTag groupProps = new CompoundTag();
        groupProps.put("main", new CompoundTag());
        fullPartA.put(SyncKeys.GROUP_PROPS, groupProps);
        fullPartA.put(SyncKeys.QUESTS, keyedCompound("quest/a"));

        CompoundTag fullPartB = new CompoundTag();
        fullPartB.put(SyncKeys.QUESTS, keyedCompound("quest/b"));
        full.add(0, fullPartA);
        full.add(1, fullPartB);

        CompoundTag joinedFull = full.joinFullPayload();
        assertEquals(QuestDefinition.CURRENT_SCHEMA, joinedFull.getInt(SyncKeys.SCHEMA));
        assertTrue(containsString(joinedFull.getList(SyncKeys.GROUPS, Tag.TAG_STRING), "main"));
        assertTrue(joinedFull.getCompound(SyncKeys.GROUP_PROPS).contains("main", Tag.TAG_COMPOUND));
        assertTrue(joinedFull.getCompound(SyncKeys.QUESTS).contains("quest/a", Tag.TAG_COMPOUND));
        assertTrue(joinedFull.getCompound(SyncKeys.QUESTS).contains("quest/b", Tag.TAG_COMPOUND));

        ClientSyncChunkAccumulator delta = new ClientSyncChunkAccumulator(2);
        CompoundTag deltaPartA = new CompoundTag();
        deltaPartA.put(SyncKeys.GROUPS, groups.copy());
        deltaPartA.put(SyncKeys.GROUP_PROPS, groupProps.copy());
        deltaPartA.put(SyncKeys.CHANGED, keyedCompound("quest/a"));
        deltaPartA.put(SyncKeys.REMOVED, removedCompound("quest/old"));
        CompoundTag deltaPartB = new CompoundTag();
        deltaPartB.put(SyncKeys.CHANGED, keyedCompound("quest/b"));
        deltaPartB.put(SyncKeys.REMOVED, removedCompound("quest/stale"));
        delta.add(0, deltaPartA);
        delta.add(1, deltaPartB);

        CompoundTag joinedDelta = delta.joinDeltaPayload();
        assertTrue(containsString(joinedDelta.getList(SyncKeys.GROUPS, Tag.TAG_STRING), "main"));
        assertTrue(joinedDelta.getCompound(SyncKeys.GROUP_PROPS).contains("main", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(SyncKeys.CHANGED).contains("quest/a", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(SyncKeys.CHANGED).contains("quest/b", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(SyncKeys.REMOVED).getBoolean("quest/old"));
        assertTrue(joinedDelta.getCompound(SyncKeys.REMOVED).getBoolean("quest/stale"));

        ClientSyncChunkAccumulator descriptions = new ClientSyncChunkAccumulator(2);
        descriptions.add(0, descriptionPart("quest/a", "First"));
        descriptions.add(1, descriptionPart("quest/b", "Second"));

        CompoundTag joinedDescriptions = descriptions.joinDescriptionPayload();
        assertEquals("First", joinedDescriptions.getCompound(SyncKeys.DESCRIPTIONS).getList("quest/a", Tag.TAG_STRING).getString(0));
        assertEquals("Second", joinedDescriptions.getCompound(SyncKeys.DESCRIPTIONS).getList("quest/b", Tag.TAG_STRING).getString(0));
    }

    @Test
    void lockedChapterIsSyncedBeforeQuestInsideIsVisible() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        store.upsert(quest("quest/locked", "locked_chapter"));
        store.upsert(quest("quest/open", "open_chapter"));
        store.setGroupLockUntilUnlocked("locked_chapter", true);

        SyncPayloadBuilder builder = new SyncPayloadBuilder(store);
        ListTag lockedGroups = builder.groupsTag(Set.of("quest/open"), false);
        CompoundTag lockedProps = builder.groupPropsTag(Set.of("quest/open"), false);
        assertTrue(containsString(lockedGroups, "locked_chapter"));
        assertTrue(lockedProps.getCompound("locked_chapter").getBoolean(SyncKeys.GroupProps.LOCK_UNTIL_UNLOCKED));

        ListTag visibleGroups = builder.groupsTag(Set.of("quest/open", "quest/locked"), false);
        CompoundTag visibleProps = builder.groupPropsTag(Set.of("quest/open", "quest/locked"), false);
        assertTrue(containsString(visibleGroups, "locked_chapter"));
        assertTrue(visibleProps.getCompound("locked_chapter").getBoolean(SyncKeys.GroupProps.LOCK_UNTIL_UNLOCKED));
    }

    @Test
    void optimisticEditorQuestUsesServerDisplayDefaults() {
        ClientQuestStateFacade.resetStateForTests();
        ClientQuestStateFacade.createEditorQuestLocal("quest/new", "main", 42, 77, " New Quest ");

        CompoundTag client = ClientQuestStateFacade.quest("quest/new");
        QuestDefinition definition = quest("quest/new", "main", 42, 77, 1.0f, "New Quest");
        PlayerQuestState playerState = new PlayerQuestState();
        playerState.quest("quest/new").setUnlocked(true);
        CompoundTag server = new SyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition, playerState);

        assertDefaultSnapshotFields(server, client);
        assertGroupView(client, "main", 42, 77, 1.0f);
        assertTrue(client.getCompound(SyncKeys.Quest.TASKS).isEmpty());
        assertTrue(client.getList(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING).isEmpty());
        assertTrue(client.getCompound(SyncKeys.Quest.REWARDS).isEmpty());
        assertTrue(client.getList(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING).isEmpty());
        assertFalse(client.getBoolean(SyncKeys.Quest.COMPLETED));
        assertTrue(client.getBoolean(SyncKeys.Quest.UNLOCKED));
        assertFalse(client.getBoolean(SyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, client.getFloat(SyncKeys.Quest.PROGRESS), 0.0001f);
    }

    @Test
    void optimisticCopiedQuestKeepsDisplayDefaultsAndClearsProgress() {
        ClientQuestStateFacade.resetStateForTests();
        ClientQuestStateFacade.createEditorQuestLocal("quest/source", "main", 10, 20, "Source");
        CompoundTag source = ClientQuestState.mutableQuest("quest/source");
        source.putBoolean(SyncKeys.Quest.COMPLETED, true);
        source.putBoolean(SyncKeys.Quest.UNLOCKED, true);
        source.putBoolean(SyncKeys.Quest.CLAIMED, true);
        source.putFloat(SyncKeys.Quest.PROGRESS, 1.0f);
        CompoundTag task = new CompoundTag();
        task.putString(SyncKeys.Task.TYPE, "questsandstuff:check");
        task.putString(SyncKeys.Task.JSON, "{}");
        task.putFloat(SyncKeys.Task.PROGRESS, 1.0f);
        task.putBoolean(SyncKeys.Task.COMPLETE, true);
        task.putInt(SyncKeys.Task.COUNT, 4);
        source.getCompound(SyncKeys.Quest.TASKS).put("task/a", task);

        ClientQuestStateFacade.copyQuestLocal("quest/source", "quest/copy", "copied", 100, 120, 1.5f, Map.of());

        CompoundTag copy = ClientQuestStateFacade.quest("quest/copy");
        assertDisplayDefaultsEqual(ClientQuestStateFacade.quest("quest/source"), copy);
        assertGroupView(copy, "copied", 100, 120, 1.5f);
        assertFalse(copy.getBoolean(SyncKeys.Quest.COMPLETED));
        assertFalse(copy.getBoolean(SyncKeys.Quest.UNLOCKED));
        assertFalse(copy.getBoolean(SyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, copy.getFloat(SyncKeys.Quest.PROGRESS), 0.0001f);
        CompoundTag copiedTask = copy.getCompound(SyncKeys.Quest.TASKS).getCompound("task/a");
        assertEquals(0.0f, copiedTask.getFloat(SyncKeys.Task.PROGRESS), 0.0001f);
        assertFalse(copiedTask.getBoolean(SyncKeys.Task.COMPLETE));
        assertEquals(0, copiedTask.getInt(SyncKeys.Task.COUNT));
    }

    @Test
    void optimisticRemoveDropsQuestAndConnectionReferences() {
        ClientQuestStateFacade.resetStateForTests();
        ClientQuestStateFacade.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestStateFacade.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");
        ClientQuestStateFacade.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestStateFacade.setConnectionColorLocal("quest/child", "quest/parent", 0x22AAEE);
        ClientQuestStateFacade.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestStateFacade.setConnectionHiddenLocal("quest/child", "quest/parent", true);

        ClientQuestStateFacade.removeQuestLocal("quest/parent");

        assertFalse(ClientQuestStateFacade.containsQuest("quest/parent"));
        CompoundTag child = ClientQuestStateFacade.quest("quest/child");
        assertTrue(child.getList(SyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(SyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
    }

    @Test
    void serverClipboardCopyKeepsDisplayDefaultsWithNewGroup() {
        QuestDefinition source = quest("quest/source", "main", 0, 0, 1.0f, "Source");

        QuestDefinition copy = ClipboardDefinitionCopier.duplicateDefinition(source, "quest/copy", "copied", 30, 40, 1.25f, Map.of());

        assertEquals(source.display().title(), copy.display().title());
        assertEquals(source.display().subtitle(), copy.display().subtitle());
        assertEquals(source.display().description(), copy.display().description());
        assertEquals(source.display().icon(), copy.display().icon());
        assertEquals(source.display().iconBackground(), copy.display().iconBackground());
        assertEquals(source.display().completionSound(), copy.display().completionSound());
        assertEquals(source.display().completionSoundVolume(), copy.display().completionSoundVolume());
        assertEquals(source.display().completionHudBackground(), copy.display().completionHudBackground());
        assertEquals(source.display().visualHidden(), copy.display().visualHidden());
        assertEquals(source.display().questBackground(), copy.display().questBackground());
        assertEquals(source.display().questBackgroundGrayscale(), copy.display().questBackgroundGrayscale());
        assertEquals(Set.of("copied"), copy.display().groups().keySet());
        GroupDef copiedView = copy.display().groups().get("copied");
        assertEquals(30, copiedView.x());
        assertEquals(40, copiedView.y());
        assertEquals(1.25f, copiedView.scale(), 0.0001f);
    }

    private static QuestDefinition quest(String id, String group) {
        return quest(id, group, 0, 0, 1.0f, id);
    }

    private static QuestDefinition quest(String id, String group, int x, int y, float scale, String title) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                QuestDisplay.forNewQuest(title, Map.of(group, new GroupDef(true, x, y, scale))),
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(),
                Map.of()
        );
    }

    private static boolean containsString(ListTag tag, String value) {
        for (int i = 0; i < tag.size(); i++) {
            if (value.equals(tag.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static CompoundTag keyedCompound(String key) {
        CompoundTag out = new CompoundTag();
        out.put(key, new CompoundTag());
        return out;
    }

    private static CompoundTag removedCompound(String key) {
        CompoundTag out = new CompoundTag();
        out.putBoolean(key, true);
        return out;
    }

    private static CompoundTag descriptionPart(String questId, String line) {
        CompoundTag descriptions = new CompoundTag();
        ListTag description = new ListTag();
        description.add(StringTag.valueOf(line));
        descriptions.put(questId, description);
        CompoundTag part = new CompoundTag();
        part.put(SyncKeys.DESCRIPTIONS, descriptions);
        return part;
    }

    private static void assertDefaultSnapshotFields(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString(SyncKeys.Quest.TITLE), actual.getString(SyncKeys.Quest.TITLE));
        assertEquals(expected.getString(SyncKeys.Quest.SUBTITLE), actual.getString(SyncKeys.Quest.SUBTITLE));
        assertEquals(expected.getList(SyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING), actual.getList(SyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING));
        assertEquals(expected.getString(SyncKeys.Quest.ICON), actual.getString(SyncKeys.Quest.ICON));
        assertEquals(expected.getString(SyncKeys.Quest.ICON_BACKGROUND), actual.getString(SyncKeys.Quest.ICON_BACKGROUND));
        assertEquals(expected.getString(SyncKeys.Quest.COMPLETION_SOUND), actual.getString(SyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(expected.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME), actual.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals(expected.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND), actual.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals(expected.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN), actual.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals(expected.getString(SyncKeys.Quest.QUEST_BACKGROUND), actual.getString(SyncKeys.Quest.QUEST_BACKGROUND));
        assertEquals(expected.getBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE), actual.getBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));
        assertEquals(expected.getBoolean(SyncKeys.Quest.REPEATABLE), actual.getBoolean(SyncKeys.Quest.REPEATABLE));
        assertEquals(expected.getString(SyncKeys.Quest.HIDDEN_MODE), actual.getString(SyncKeys.Quest.HIDDEN_MODE));
        assertEquals(expected.getBoolean(SyncKeys.Quest.SHOW_PREREQUISITE_ARROW), actual.getBoolean(SyncKeys.Quest.SHOW_PREREQUISITE_ARROW));
    }

    private static void assertDisplayDefaultsEqual(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString(SyncKeys.Quest.TITLE), actual.getString(SyncKeys.Quest.TITLE));
        assertEquals(expected.getString(SyncKeys.Quest.SUBTITLE), actual.getString(SyncKeys.Quest.SUBTITLE));
        assertEquals(expected.getString(SyncKeys.Quest.ICON), actual.getString(SyncKeys.Quest.ICON));
        assertEquals(expected.getString(SyncKeys.Quest.ICON_BACKGROUND), actual.getString(SyncKeys.Quest.ICON_BACKGROUND));
        assertEquals(expected.getString(SyncKeys.Quest.COMPLETION_SOUND), actual.getString(SyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(expected.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME), actual.getInt(SyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals(expected.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND), actual.getString(SyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals(expected.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN), actual.getBoolean(SyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals(expected.getString(SyncKeys.Quest.QUEST_BACKGROUND), actual.getString(SyncKeys.Quest.QUEST_BACKGROUND));
        assertEquals(expected.getBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE), actual.getBoolean(SyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));
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
