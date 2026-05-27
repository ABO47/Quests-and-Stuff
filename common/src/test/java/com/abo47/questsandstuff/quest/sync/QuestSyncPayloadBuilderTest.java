package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
                        "minecraft:book",
                        "minecraft:barrier",
                        QuestDisplay.DEFAULT_COMPLETION_SOUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME,
                        "hud/quest_a.png",
                        false,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND,
                        false
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

    private static QuestDefinition quest(String id, String group) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(id, "", List.of(), Map.of(group, ChapterDefinition.DEFAULT), "minecraft:book", "minecraft:barrier"),
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
}
