package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;

import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTaskTypeCatalogTest {
    @Test
    void taskCatalogExposesItemInteractionTasks() {
        Set<String> types = QuestTaskTypeCatalog.taskChoices()
                .stream()
                .map(QuestDetailsTypeChoice::type)
                .collect(Collectors.toSet());

        assertTrue(types.contains("item_interact"));
        assertTrue(types.contains("item_use"));
    }

    @Test
    void catalogDefaultsParseThroughServerCodecs() {
        for (QuestDetailsTypeChoice choice : QuestTaskTypeCatalog.taskChoices()) {
            String id = "task_" + choice.type();
            JsonObject json = choice.defaultJson(id);

            assertChoiceMetadata(choice, id, json);
            QuestTaskDefinition parsed = parseTask(json);
            assertEquals(id, parsed.id(), choice.type());
        }

        for (QuestDetailsTypeChoice choice : QuestTaskTypeCatalog.rewardChoices()) {
            String id = "reward_" + choice.type();
            JsonObject json = choice.defaultJson(id);

            assertChoiceMetadata(choice, id, json);
            QuestRewardDefinition parsed = parseReward(json);
            assertEquals(id, parsed.id(), choice.type());
        }
    }

    @Test
    void catalogLookupAcceptsNamespacedTypes() {
        assertEquals("item_interact", QuestTaskTypeCatalog.taskChoice("questsandstuff:item_interact").type());
        assertEquals("loot_table", QuestTaskTypeCatalog.rewardChoice("questsandstuff:loot_table").type());
    }

    private static void assertChoiceMetadata(QuestDetailsTypeChoice choice, String id, JsonObject json) {
        assertNotNull(choice.editFlow(), choice.type());
        assertFalse(choice.labelKey().isBlank(), choice.type());
        assertFalse(choice.icon().isBlank(), choice.type());
        assertEquals(id, json.get("id").getAsString(), choice.type());
        assertEquals(choice.fullType(), json.get("type").getAsString(), choice.type());
        for (String requiredField : choice.requiredJsonFields()) {
            assertTrue(json.has(requiredField), () -> choice.type() + " default missing " + requiredField);
        }
    }

    private static QuestTaskDefinition parseTask(JsonObject json) {
        List<String> errors = new ArrayList<>();
        QuestTaskDefinition parsed = QuestTaskDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(errors::add)
                .orElse(null);
        assertNotNull(parsed, () -> "Task default did not parse: " + json + " errors=" + errors);
        return parsed;
    }

    private static QuestRewardDefinition parseReward(JsonObject json) {
        List<String> errors = new ArrayList<>();
        QuestRewardDefinition parsed = QuestRewardDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(errors::add)
                .orElse(null);
        assertNotNull(parsed, () -> "Reward default did not parse: " + json + " errors=" + errors);
        return parsed;
    }
}
