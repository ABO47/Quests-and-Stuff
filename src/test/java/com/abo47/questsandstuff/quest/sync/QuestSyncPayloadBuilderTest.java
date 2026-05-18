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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QuestSyncPayloadBuilderTest {
    @TempDir
    Path root;

    @Test
    void editorPayloadSortsPrerequisitesAndFiltersConnectionModes() {
        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "quest_a",
                new QuestDisplay("Title", "Subtitle", java.util.List.of(), Map.of("main", ChapterDefinition.DEFAULT), "minecraft:book", "minecraft:barrier"),
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

        ListTag hidden = payload.getList("hidden_connections", net.minecraft.nbt.Tag.TAG_STRING);
        assertEquals("a_parent", hidden.getString(0));
        assertEquals("z_parent", hidden.getString(1));
    }
}
