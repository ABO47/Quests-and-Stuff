package com.abo47.questsandstuff.quest.model.task;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.quest.model.task.generic.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTaskItemLocksTest {
    private static final ResourceLocation TYPE = new ResourceLocation("questsandstuff", "kill_entity");

    @Test
    void normalizeKeepsValidItemsAndTagsInOrder() {
        List<String> normalized = QuestTaskItemLocks.normalize(List.of(
                "minecraft:iron_ingot",
                "#Forge:Ingots",
                " minecraft:gold_ingot ",
                "minecraft:iron_ingot"
        ));

        assertEquals(List.of("minecraft:iron_ingot", "#forge:ingots", "minecraft:gold_ingot"), normalized);
    }

    @Test
    void normalizeDropsBlankAndMalformedEntries() {
        List<String> normalized = QuestTaskItemLocks.normalize(List.of("", "   ", "no colon!", "#", "minecraft:apple"));

        assertEquals(List.of("minecraft:apple"), normalized);
    }

    @Test
    void normalizeReturnsEmptyForNullInput() {
        assertTrue(QuestTaskItemLocks.normalize(null).isEmpty());
    }

    @Test
    void tagDetectionReadsPrefix() {
        assertTrue(QuestTaskItemLocks.isTag("#forge:ingots"));
        assertFalse(QuestTaskItemLocks.isTag("forge:ingots"));
    }

    @Test
    void idParsingSplitsTagsFromItems() {
        assertEquals(new ResourceLocation("forge", "ingots"), QuestTaskItemLocks.tagId("#forge:ingots"));
        assertNull(QuestTaskItemLocks.tagId("minecraft:iron_ingot"));
        assertNull(QuestTaskItemLocks.id("#forge:ingots"));
        assertEquals(new ResourceLocation("minecraft", "iron_ingot"), QuestTaskItemLocks.id("minecraft:iron_ingot"));
    }

    @Test
    void addAppendsNormalizedEntry() {
        List<String> locks = QuestTaskItemLocks.add(List.of("minecraft:iron_ingot"), "  Minecraft:Copper_Ingot ");

        assertEquals(List.of("minecraft:iron_ingot", "minecraft:copper_ingot"), locks);
    }

    @Test
    void addIgnoresInvalidEntry() {
        List<String> locks = QuestTaskItemLocks.add(List.of("minecraft:iron_ingot"), "not an id");

        assertEquals(List.of("minecraft:iron_ingot"), locks);
    }

    @Test
    void removeMatchesCaseInsensitive() {
        List<String> locks = QuestTaskItemLocks.remove(List.of("minecraft:iron_ingot", "#forge:ingots"), "MINECRAFT:IRON_INGOT");

        assertEquals(List.of("#forge:ingots"), locks);
    }

    @Test
    void codecRoundTripsLocksThroughTaskJson() {
        SimpleQuestTaskDefinition task = new SimpleQuestTaskDefinition(
                "t1", TYPE, QuestSignalType.ITEM_CRAFTED, 1, "", "", "", List.of("minecraft:iron_ingot", "#forge:ingots/copper"));

        var encoded = QuestTaskDefinition.CODEC.encodeStart(JsonOps.INSTANCE, task).getOrThrow(false, error -> {});
        var decoded = QuestTaskDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow(false, error -> {});

        assertEquals(List.of("minecraft:iron_ingot", "#forge:ingots/copper"), decoded.itemLocks());
    }

    @Test
    void codecDefaultsToEmptyLocksWhenFieldMissing() {
        SimpleQuestTaskDefinition task = new SimpleQuestTaskDefinition("t1", TYPE, QuestSignalType.ITEM_CRAFTED, 1, "", "", "");

        var decoded = QuestTaskDefinition.CODEC.parse(JsonOps.INSTANCE, taskJson(task)).getOrThrow(false, error -> {});

        assertTrue(decoded.itemLocks().isEmpty());
    }

    private static com.google.gson.JsonObject taskJson(SimpleQuestTaskDefinition task) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("id", task.id());
        json.addProperty("type", task.type().toString());
        return json;
    }
}
