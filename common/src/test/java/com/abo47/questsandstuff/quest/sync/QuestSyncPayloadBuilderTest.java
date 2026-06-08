package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

        ListTag prerequisites = payload.getList(QuestDefinition.PREREQUISITES_FIELD, net.minecraft.nbt.Tag.TAG_STRING);
        assertEquals("a_parent", prerequisites.getString(0));
        assertEquals("z_parent", prerequisites.getString(1));

        CompoundTag modes = payload.getCompound("connection_modes");
        assertEquals("grid", modes.getString("a_parent"));
        assertFalse(modes.contains("z_parent"));
        assertEquals("hud/quest_a.png", payload.getString("completion_hud_background"));

        ListTag hidden = payload.getList("hidden_connections", net.minecraft.nbt.Tag.TAG_STRING);
        assertEquals("a_parent", hidden.getString(0));
        assertEquals("z_parent", hidden.getString(1));
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
        assertTrue(lockedProps.getCompound("locked_chapter").getBoolean("lock_until_unlocked"));

        ListTag visibleGroups = builder.groupsTag(Set.of("quest/open", "quest/locked"), false);
        CompoundTag visibleProps = builder.groupPropsTag(Set.of("quest/open", "quest/locked"), false);
        assertTrue(containsString(visibleGroups, "locked_chapter"));
        assertTrue(visibleProps.getCompound("locked_chapter").getBoolean("lock_until_unlocked"));
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
        assertTrue(client.getCompound("tasks").isEmpty());
        assertTrue(client.getList("tasks_order", Tag.TAG_STRING).isEmpty());
        assertTrue(client.getCompound("rewards").isEmpty());
        assertTrue(client.getList("rewards_order", Tag.TAG_STRING).isEmpty());
        assertFalse(client.getBoolean("completed"));
        assertTrue(client.getBoolean("unlocked"));
        assertFalse(client.getBoolean("claimed"));
        assertEquals(0.0f, client.getFloat("progress"), 0.0001f);
    }

    @Test
    void optimisticCopiedQuestKeepsDisplayDefaultsAndClearsProgress() {
        ClientQuestCache.resetStateForTests();
        ClientQuestCache.createEditorQuestLocal("quest/source", "main", 10, 20, "Source");
        CompoundTag source = ClientQuestState.mutableQuest("quest/source");
        source.putBoolean("completed", true);
        source.putBoolean("unlocked", true);
        source.putBoolean("claimed", true);
        source.putFloat("progress", 1.0f);
        CompoundTag task = new CompoundTag();
        task.putString("type", "questsandstuff:check");
        task.putString("json", "{}");
        task.putFloat("progress", 1.0f);
        task.putBoolean("complete", true);
        task.putInt("count", 4);
        source.getCompound("tasks").put("task/a", task);

        ClientQuestCache.copyQuestLocal("quest/source", "quest/copy", "copied", 100, 120, 1.5f, Map.of());

        CompoundTag copy = ClientQuestCache.quest("quest/copy");
        assertDisplayDefaultsEqual(ClientQuestCache.quest("quest/source"), copy);
        assertGroupView(copy, "copied", 100, 120, 1.5f);
        assertFalse(copy.getBoolean("completed"));
        assertFalse(copy.getBoolean("unlocked"));
        assertFalse(copy.getBoolean("claimed"));
        assertEquals(0.0f, copy.getFloat("progress"), 0.0001f);
        CompoundTag copiedTask = copy.getCompound("tasks").getCompound("task/a");
        assertEquals(0.0f, copiedTask.getFloat("progress"), 0.0001f);
        assertFalse(copiedTask.getBoolean("complete"));
        assertEquals(0, copiedTask.getInt("count"));
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
        assertTrue(child.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING).isEmpty());
        assertFalse(child.getCompound("connection_colors").contains("quest/parent"));
        assertFalse(child.getCompound("connection_modes").contains("quest/parent"));
        assertTrue(child.getList("hidden_connections", Tag.TAG_STRING).isEmpty());
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

    private static void assertDefaultSnapshotFields(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString("title"), actual.getString("title"));
        assertEquals(expected.getString("subtitle"), actual.getString("subtitle"));
        assertEquals(expected.getList("description", Tag.TAG_STRING), actual.getList("description", Tag.TAG_STRING));
        assertEquals(expected.getString("icon"), actual.getString("icon"));
        assertEquals(expected.getString("icon_background"), actual.getString("icon_background"));
        assertEquals(expected.getString("completion_sound"), actual.getString("completion_sound"));
        assertEquals(expected.getInt("completion_sound_volume"), actual.getInt("completion_sound_volume"));
        assertEquals(expected.getString("completion_hud_background"), actual.getString("completion_hud_background"));
        assertEquals(expected.getBoolean("visual_hidden"), actual.getBoolean("visual_hidden"));
        assertEquals(expected.getString("quest_background"), actual.getString("quest_background"));
        assertEquals(expected.getBoolean("quest_background_grayscale"), actual.getBoolean("quest_background_grayscale"));
        assertEquals(expected.getBoolean("repeatable"), actual.getBoolean("repeatable"));
        assertEquals(expected.getString("hidden_mode"), actual.getString("hidden_mode"));
        assertEquals(expected.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD), actual.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD));
    }

    private static void assertDisplayDefaultsEqual(CompoundTag expected, CompoundTag actual) {
        assertEquals(expected.getString("title"), actual.getString("title"));
        assertEquals(expected.getString("subtitle"), actual.getString("subtitle"));
        assertEquals(expected.getString("icon"), actual.getString("icon"));
        assertEquals(expected.getString("icon_background"), actual.getString("icon_background"));
        assertEquals(expected.getString("completion_sound"), actual.getString("completion_sound"));
        assertEquals(expected.getInt("completion_sound_volume"), actual.getInt("completion_sound_volume"));
        assertEquals(expected.getString("completion_hud_background"), actual.getString("completion_hud_background"));
        assertEquals(expected.getBoolean("visual_hidden"), actual.getBoolean("visual_hidden"));
        assertEquals(expected.getString("quest_background"), actual.getString("quest_background"));
        assertEquals(expected.getBoolean("quest_background_grayscale"), actual.getBoolean("quest_background_grayscale"));
    }

    private static void assertGroupView(CompoundTag quest, String group, int x, int y, float scale) {
        CompoundTag groups = quest.getCompound("groups");
        assertEquals(Set.of(group), groups.getAllKeys());
        CompoundTag view = groups.getCompound(group);
        assertTrue(view.getBoolean("visible"));
        assertEquals(x, view.getInt("x"));
        assertEquals(y, view.getInt("y"));
        assertEquals(scale, view.getFloat("scale"), 0.0001f);
    }
}
