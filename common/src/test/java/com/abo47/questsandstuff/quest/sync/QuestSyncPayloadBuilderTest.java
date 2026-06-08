package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncChunkAccumulator;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.reward.QuestRewards;
import com.abo47.questsandstuff.quest.model.reward.XpQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTasks;
import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.abo47.questsandstuff.quest.model.task.progress.CheckQuestTaskDefinition;
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

class QuestSyncPayloadBuilderTest {
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
                        Map.of("main", ChapterDefinition.DEFAULT),
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

        CompoundTag payload = new QuestSyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition);

        ListTag prerequisites = payload.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING);
        assertEquals("a_parent", prerequisites.getString(0));
        assertEquals("z_parent", prerequisites.getString(1));

        CompoundTag modes = payload.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES);
        assertEquals("grid", modes.getString("a_parent"));
        assertFalse(modes.contains("z_parent"));
        assertEquals("hud/quest_a.png", payload.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND));

        ListTag hidden = payload.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
        assertEquals("a_parent", hidden.getString(0));
        assertEquals("z_parent", hidden.getString(1));
    }

    @Test
    void editorPayloadUsesSharedSyncKeySchema() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "quest/keyed",
                QuestDisplay.forNewQuest("Keyed", Map.of("main", new ChapterDefinition(true, 12, 34, 1.25f))),
                QuestSettings.DEFAULT,
                Set.of("quest/parent"),
                Map.of("quest/parent", 0x112233),
                Map.of("quest/parent", "grid"),
                Set.of("quest/parent"),
                List.of("task/check"),
                List.of("reward/xp"),
                Map.of("task/check", new CheckQuestTaskDefinition("task/check", QuestTasks.id("check"), "")),
                Map.of("reward/xp", new XpQuestRewardDefinition("reward/xp", QuestRewards.id("xp"), 7, XpMode.POINTS))
        );
        PlayerQuestState playerState = new PlayerQuestState();
        playerState.quest("quest/keyed").setUnlocked(true);

        CompoundTag payload = new QuestSyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition, playerState);

        assertTrue(payload.contains(QuestSyncKeys.Quest.TITLE, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.SUBTITLE, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.DESCRIPTION, Tag.TAG_LIST));
        assertTrue(payload.contains(QuestSyncKeys.Quest.ICON, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.ICON_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.COMPLETION_SOUND, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME, Tag.TAG_INT));
        assertTrue(payload.contains(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.VISUAL_HIDDEN, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.QUEST_BACKGROUND, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.COMPLETED, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.UNLOCKED, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.CLAIMED, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.PROGRESS, Tag.TAG_FLOAT));
        assertTrue(payload.contains(QuestSyncKeys.Quest.REPEATABLE, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.HIDDEN_MODE, Tag.TAG_STRING));
        assertTrue(payload.contains(QuestSyncKeys.Quest.SHOW_PREREQUISITE_ARROW, Tag.TAG_BYTE));
        assertTrue(payload.contains(QuestSyncKeys.Quest.TASKS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(QuestSyncKeys.Quest.TASKS_ORDER, Tag.TAG_LIST));
        assertTrue(payload.contains(QuestSyncKeys.Quest.REWARDS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(QuestSyncKeys.Quest.REWARDS_ORDER, Tag.TAG_LIST));
        assertTrue(payload.contains(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_LIST));
        assertTrue(payload.contains(QuestSyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(QuestSyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND));
        assertTrue(payload.contains(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST));
        assertTrue(payload.contains(QuestSyncKeys.Quest.GROUPS, Tag.TAG_COMPOUND));

        assertEquals("task/check", payload.getList(QuestSyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING).getString(0));
        CompoundTag task = payload.getCompound(QuestSyncKeys.Quest.TASKS).getCompound("task/check");
        assertTrue(task.contains(QuestSyncKeys.Objective.TYPE, Tag.TAG_STRING));
        assertTrue(task.contains(QuestSyncKeys.Objective.JSON, Tag.TAG_STRING));
        assertTrue(task.contains(QuestSyncKeys.Objective.PROGRESS, Tag.TAG_FLOAT));
        assertTrue(task.contains(QuestSyncKeys.Objective.COMPLETE, Tag.TAG_BYTE));
        assertTrue(task.contains(QuestSyncKeys.Objective.COUNT, Tag.TAG_INT));

        assertEquals("reward/xp", payload.getList(QuestSyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING).getString(0));
        CompoundTag reward = payload.getCompound(QuestSyncKeys.Quest.REWARDS).getCompound("reward/xp");
        assertTrue(reward.contains(QuestSyncKeys.Objective.TYPE, Tag.TAG_STRING));
        assertTrue(reward.contains(QuestSyncKeys.Objective.JSON, Tag.TAG_STRING));
        assertTrue(reward.contains(QuestSyncKeys.Objective.SELECTABLE, Tag.TAG_BYTE));
        assertTrue(reward.contains(QuestSyncKeys.Objective.MASS_CLAIMABLE, Tag.TAG_BYTE));

        CompoundTag groupView = payload.getCompound(QuestSyncKeys.Quest.GROUPS).getCompound("main");
        assertTrue(groupView.getBoolean(QuestSyncKeys.ChapterView.VISIBLE));
        assertEquals(12, groupView.getInt(QuestSyncKeys.ChapterView.X));
        assertEquals(34, groupView.getInt(QuestSyncKeys.ChapterView.Y));
        assertEquals(1.25f, groupView.getFloat(QuestSyncKeys.ChapterView.SCALE), 0.0001f);
    }

    @Test
    void chunkAccumulatorJoinsPayloadsThroughSharedSyncKeys() {
        ClientSyncChunkAccumulator full = new ClientSyncChunkAccumulator(2);
        CompoundTag fullPartA = new CompoundTag();
        ListTag groups = new ListTag();
        groups.add(StringTag.valueOf("main"));
        fullPartA.put(QuestSyncKeys.GROUPS, groups);
        CompoundTag groupProps = new CompoundTag();
        groupProps.put("main", new CompoundTag());
        fullPartA.put(QuestSyncKeys.GROUP_PROPS, groupProps);
        fullPartA.put(QuestSyncKeys.QUESTS, keyedCompound("quest/a"));

        CompoundTag fullPartB = new CompoundTag();
        fullPartB.put(QuestSyncKeys.QUESTS, keyedCompound("quest/b"));
        full.add(0, fullPartA);
        full.add(1, fullPartB);

        CompoundTag joinedFull = full.joinFullPayload();
        assertEquals(QuestDefinition.CURRENT_SCHEMA, joinedFull.getInt(QuestSyncKeys.SCHEMA));
        assertTrue(containsString(joinedFull.getList(QuestSyncKeys.GROUPS, Tag.TAG_STRING), "main"));
        assertTrue(joinedFull.getCompound(QuestSyncKeys.GROUP_PROPS).contains("main", Tag.TAG_COMPOUND));
        assertTrue(joinedFull.getCompound(QuestSyncKeys.QUESTS).contains("quest/a", Tag.TAG_COMPOUND));
        assertTrue(joinedFull.getCompound(QuestSyncKeys.QUESTS).contains("quest/b", Tag.TAG_COMPOUND));

        ClientSyncChunkAccumulator delta = new ClientSyncChunkAccumulator(2);
        CompoundTag deltaPartA = new CompoundTag();
        deltaPartA.put(QuestSyncKeys.GROUPS, groups.copy());
        deltaPartA.put(QuestSyncKeys.GROUP_PROPS, groupProps.copy());
        deltaPartA.put(QuestSyncKeys.CHANGED, keyedCompound("quest/a"));
        deltaPartA.put(QuestSyncKeys.REMOVED, removedCompound("quest/old"));
        CompoundTag deltaPartB = new CompoundTag();
        deltaPartB.put(QuestSyncKeys.CHANGED, keyedCompound("quest/b"));
        deltaPartB.put(QuestSyncKeys.REMOVED, removedCompound("quest/stale"));
        delta.add(0, deltaPartA);
        delta.add(1, deltaPartB);

        CompoundTag joinedDelta = delta.joinDeltaPayload();
        assertTrue(containsString(joinedDelta.getList(QuestSyncKeys.GROUPS, Tag.TAG_STRING), "main"));
        assertTrue(joinedDelta.getCompound(QuestSyncKeys.GROUP_PROPS).contains("main", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(QuestSyncKeys.CHANGED).contains("quest/a", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(QuestSyncKeys.CHANGED).contains("quest/b", Tag.TAG_COMPOUND));
        assertTrue(joinedDelta.getCompound(QuestSyncKeys.REMOVED).getBoolean("quest/old"));
        assertTrue(joinedDelta.getCompound(QuestSyncKeys.REMOVED).getBoolean("quest/stale"));

        ClientSyncChunkAccumulator descriptions = new ClientSyncChunkAccumulator(2);
        descriptions.add(0, descriptionPart("quest/a", "First"));
        descriptions.add(1, descriptionPart("quest/b", "Second"));

        CompoundTag joinedDescriptions = descriptions.joinDescriptionPayload();
        assertEquals("First", joinedDescriptions.getCompound(QuestSyncKeys.DESCRIPTIONS).getList("quest/a", Tag.TAG_STRING).getString(0));
        assertEquals("Second", joinedDescriptions.getCompound(QuestSyncKeys.DESCRIPTIONS).getList("quest/b", Tag.TAG_STRING).getString(0));
    }

    @Test
    void lockedChapterIsSyncedBeforeQuestInsideIsVisible() {
        QuestDefinitionStore store = new QuestDefinitionStore(root);
        store.upsert(quest("quest/locked", "locked_chapter"));
        store.upsert(quest("quest/open", "open_chapter"));
        store.setGroupLockUntilUnlocked("locked_chapter", true);

        QuestSyncPayloadBuilder builder = new QuestSyncPayloadBuilder(store);
        ListTag lockedGroups = builder.groupsTag(Set.of("quest/open"), false);
        CompoundTag lockedProps = builder.groupPropsTag(Set.of("quest/open"), false);
        assertTrue(containsString(lockedGroups, "locked_chapter"));
        assertTrue(lockedProps.getCompound("locked_chapter").getBoolean(QuestSyncKeys.GroupProps.LOCK_UNTIL_UNLOCKED));

        ListTag visibleGroups = builder.groupsTag(Set.of("quest/open", "quest/locked"), false);
        CompoundTag visibleProps = builder.groupPropsTag(Set.of("quest/open", "quest/locked"), false);
        assertTrue(containsString(visibleGroups, "locked_chapter"));
        assertTrue(visibleProps.getCompound("locked_chapter").getBoolean(QuestSyncKeys.GroupProps.LOCK_UNTIL_UNLOCKED));
    }

    @Test
    void optimisticEditorQuestUsesServerDisplayDefaults() {
        ClientQuestCache.resetStateForTests();
        ClientQuestCache.createEditorQuestLocal("quest/new", "main", 42, 77, " New Quest ");

        CompoundTag client = ClientQuestCache.quest("quest/new");
        QuestDefinition definition = quest("quest/new", "main", 42, 77, 1.0f, "New Quest");
        PlayerQuestState playerState = new PlayerQuestState();
        playerState.quest("quest/new").setUnlocked(true);
        CompoundTag server = new QuestSyncPayloadBuilder(new QuestDefinitionStore(root)).editorQuestPayload(definition, playerState);

        assertDefaultSnapshotFields(server, client);
        assertGroupView(client, "main", 42, 77, 1.0f);
        assertTrue(client.getCompound(QuestSyncKeys.Quest.TASKS).isEmpty());
        assertTrue(client.getList(QuestSyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING).isEmpty());
        assertTrue(client.getCompound(QuestSyncKeys.Quest.REWARDS).isEmpty());
        assertTrue(client.getList(QuestSyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING).isEmpty());
        assertFalse(client.getBoolean(QuestSyncKeys.Quest.COMPLETED));
        assertTrue(client.getBoolean(QuestSyncKeys.Quest.UNLOCKED));
        assertFalse(client.getBoolean(QuestSyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, client.getFloat(QuestSyncKeys.Quest.PROGRESS), 0.0001f);
    }

    @Test
    void optimisticCopiedQuestKeepsDisplayDefaultsAndClearsProgress() {
        ClientQuestCache.resetStateForTests();
        ClientQuestCache.createEditorQuestLocal("quest/source", "main", 10, 20, "Source");
        CompoundTag source = ClientQuestState.mutableQuest("quest/source");
        source.putBoolean(QuestSyncKeys.Quest.COMPLETED, true);
        source.putBoolean(QuestSyncKeys.Quest.UNLOCKED, true);
        source.putBoolean(QuestSyncKeys.Quest.CLAIMED, true);
        source.putFloat(QuestSyncKeys.Quest.PROGRESS, 1.0f);
        CompoundTag task = new CompoundTag();
        task.putString(QuestSyncKeys.Objective.TYPE, "questsandstuff:check");
        task.putString(QuestSyncKeys.Objective.JSON, "{}");
        task.putFloat(QuestSyncKeys.Objective.PROGRESS, 1.0f);
        task.putBoolean(QuestSyncKeys.Objective.COMPLETE, true);
        task.putInt(QuestSyncKeys.Objective.COUNT, 4);
        source.getCompound(QuestSyncKeys.Quest.TASKS).put("task/a", task);

        ClientQuestCache.copyQuestLocal("quest/source", "quest/copy", "copied", 100, 120, 1.5f, Map.of());

        CompoundTag copy = ClientQuestCache.quest("quest/copy");
        assertDisplayDefaultsEqual(ClientQuestCache.quest("quest/source"), copy);
        assertGroupView(copy, "copied", 100, 120, 1.5f);
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.COMPLETED));
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.UNLOCKED));
        assertFalse(copy.getBoolean(QuestSyncKeys.Quest.CLAIMED));
        assertEquals(0.0f, copy.getFloat(QuestSyncKeys.Quest.PROGRESS), 0.0001f);
        CompoundTag copiedTask = copy.getCompound(QuestSyncKeys.Quest.TASKS).getCompound("task/a");
        assertEquals(0.0f, copiedTask.getFloat(QuestSyncKeys.Objective.PROGRESS), 0.0001f);
        assertFalse(copiedTask.getBoolean(QuestSyncKeys.Objective.COMPLETE));
        assertEquals(0, copiedTask.getInt(QuestSyncKeys.Objective.COUNT));
    }

    @Test
    void optimisticRemoveDropsQuestAndConnectionReferences() {
        ClientQuestCache.resetStateForTests();
        ClientQuestCache.createEditorQuestLocal("quest/parent", "main", 0, 0, "Parent");
        ClientQuestCache.createEditorQuestLocal("quest/child", "main", 80, 0, "Child");
        ClientQuestCache.setQuestPrerequisiteLocal("quest/child", "quest/parent", true);
        ClientQuestCache.setConnectionColorLocal("quest/child", "quest/parent", 0x22AAEE);
        ClientQuestCache.setConnectionModeLocal("quest/child", "quest/parent", true);
        ClientQuestCache.setConnectionHiddenLocal("quest/child", "quest/parent", true);

        ClientQuestCache.removeQuestLocal("quest/parent");

        assertFalse(ClientQuestCache.containsQuest("quest/parent"));
        CompoundTag child = ClientQuestCache.quest("quest/child");
        assertTrue(child.getList(QuestSyncKeys.Quest.PREREQUISITES, Tag.TAG_STRING).isEmpty());
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS).contains("quest/parent"));
        assertFalse(child.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES).contains("quest/parent"));
        assertTrue(child.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING).isEmpty());
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
        ChapterDefinition copiedView = copy.display().groups().get("copied");
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
                QuestDisplay.forNewQuest(title, Map.of(group, new ChapterDefinition(true, x, y, scale))),
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
        part.put(QuestSyncKeys.DESCRIPTIONS, descriptions);
        return part;
    }

    private static void assertDefaultSnapshotFields(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString(QuestSyncKeys.Quest.TITLE), actual.getString(QuestSyncKeys.Quest.TITLE));
        assertEquals(expected.getString(QuestSyncKeys.Quest.SUBTITLE), actual.getString(QuestSyncKeys.Quest.SUBTITLE));
        assertEquals(expected.getList(QuestSyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING), actual.getList(QuestSyncKeys.Quest.DESCRIPTION, Tag.TAG_STRING));
        assertEquals(expected.getString(QuestSyncKeys.Quest.ICON), actual.getString(QuestSyncKeys.Quest.ICON));
        assertEquals(expected.getString(QuestSyncKeys.Quest.ICON_BACKGROUND), actual.getString(QuestSyncKeys.Quest.ICON_BACKGROUND));
        assertEquals(expected.getString(QuestSyncKeys.Quest.COMPLETION_SOUND), actual.getString(QuestSyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(expected.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME), actual.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals(expected.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND), actual.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN), actual.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals(expected.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND), actual.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE), actual.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.REPEATABLE), actual.getBoolean(QuestSyncKeys.Quest.REPEATABLE));
        assertEquals(expected.getString(QuestSyncKeys.Quest.HIDDEN_MODE), actual.getString(QuestSyncKeys.Quest.HIDDEN_MODE));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.SHOW_PREREQUISITE_ARROW), actual.getBoolean(QuestSyncKeys.Quest.SHOW_PREREQUISITE_ARROW));
    }

    private static void assertDisplayDefaultsEqual(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString(QuestSyncKeys.Quest.TITLE), actual.getString(QuestSyncKeys.Quest.TITLE));
        assertEquals(expected.getString(QuestSyncKeys.Quest.SUBTITLE), actual.getString(QuestSyncKeys.Quest.SUBTITLE));
        assertEquals(expected.getString(QuestSyncKeys.Quest.ICON), actual.getString(QuestSyncKeys.Quest.ICON));
        assertEquals(expected.getString(QuestSyncKeys.Quest.ICON_BACKGROUND), actual.getString(QuestSyncKeys.Quest.ICON_BACKGROUND));
        assertEquals(expected.getString(QuestSyncKeys.Quest.COMPLETION_SOUND), actual.getString(QuestSyncKeys.Quest.COMPLETION_SOUND));
        assertEquals(expected.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME), actual.getInt(QuestSyncKeys.Quest.COMPLETION_SOUND_VOLUME));
        assertEquals(expected.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND), actual.getString(QuestSyncKeys.Quest.COMPLETION_HUD_BACKGROUND));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN), actual.getBoolean(QuestSyncKeys.Quest.VISUAL_HIDDEN));
        assertEquals(expected.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND), actual.getString(QuestSyncKeys.Quest.QUEST_BACKGROUND));
        assertEquals(expected.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE), actual.getBoolean(QuestSyncKeys.Quest.QUEST_BACKGROUND_GRAYSCALE));
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
