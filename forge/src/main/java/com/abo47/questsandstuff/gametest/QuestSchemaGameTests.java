package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.task.progress.CheckQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.item.CollectionMode;
import com.abo47.questsandstuff.quest.model.reward.CommandQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.item.GatherItemQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.reward.ItemQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.LootTableQuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.reward.QuestRewards;
import com.abo47.questsandstuff.quest.model.QuestSchemaMigrator;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTasks;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.model.task.progress.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.XpMode;
import com.abo47.questsandstuff.quest.model.reward.XpQuestRewardDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.util.StableIdAllocator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestSchemaGameTests {
    private QuestSchemaGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void schemaRoundtrip(GameTestHelper helper) {
        QuestTaskDefinition task = QuestGameTestDefinitions.task("collect", "item", 8, "minecraft:oak_log", Map.of());

        QuestRewardDefinition reward = QuestGameTestDefinitions.reward("xp", "xp", 3, "", false, Map.of());

        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "test/roundtrip",
                new QuestDisplay(
                        "Roundtrip",
                        "",
                        List.of(),
                        Map.of("Main", ChapterDefinition.DEFAULT),
                        "minecraft:book",
                        "minecraft:barrier",
                        QuestDisplay.DEFAULT_COMPLETION_SOUND,
                        QuestDisplay.DEFAULT_COMPLETION_SOUND_VOLUME,
                        "hud/roundtrip.png",
                        false,
                        QuestDisplay.DEFAULT_QUEST_BACKGROUND,
                        false
                ),
                new QuestSettings(false, QuestVisibilityMode.PREREQUISITES_VISIBLE, false, false, false, true),
                Set.of(),
                Map.of(task.id(), task),
                Map.of(reward.id(), reward)
        );

        JsonElement encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Encode failed: " + message);
                });

        QuestDefinition decoded = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Decode failed: " + message);
                });

        if (!definition.id().equals(decoded.id()) || decoded.tasks().isEmpty() || decoded.rewards().isEmpty()) {
            throw new GameTestAssertException("Quest schema roundtrip mismatch");
        }
        if (!"hud/roundtrip.png".equals(decoded.display().completionHudBackground())) {
            throw new GameTestAssertException("Completion HUD background did not roundtrip");
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void objectiveUiFieldsRoundtrip(GameTestHelper helper) {
        QuestTaskDefinition itemTask = new GatherItemQuestTaskDefinition(
                "item",
                QuestTasks.id("item"),
                mc("dirt"),
                "",
                "",
                4,
                CollectionMode.AUTOMATIC,
                "Collect dirt",
                "minecraft:dirt"
        );
        QuestTaskDefinition checkTask = new CheckQuestTaskDefinition(
                "manual",
                QuestTasks.id("check"),
                "manual",
                "Manual step",
                "minecraft:lever"
        );
        QuestTaskDefinition biomeTask = new SimpleQuestTaskDefinition(
                "biome",
                QuestTasks.id("biome"),
                QuestSignalType.BIOME_ENTER,
                1,
                "minecraft:desert",
                "minecraft:sand",
                "Find desert"
        );
        QuestRewardDefinition itemReward = new ItemQuestRewardDefinition(
                "reward_item",
                QuestRewards.id("item"),
                mc("diamond"),
                2,
                "",
                "Reward diamond",
                "minecraft:diamond"
        );
        QuestRewardDefinition xpReward = new XpQuestRewardDefinition(
                "reward_xp",
                QuestRewards.id("xp"),
                12,
                XpMode.POINTS,
                "Reward XP",
                "minecraft:experience_bottle"
        );
        QuestRewardDefinition commandReward = new CommandQuestRewardDefinition(
                "reward_command",
                QuestRewards.id("command"),
                "say done",
                "Reward command",
                "minecraft:command_block"
        );
        QuestRewardDefinition lootReward = new LootTableQuestRewardDefinition(
                "reward_loot",
                QuestRewards.id("loot_table"),
                mc("chests/simple_dungeon"),
                "Reward loot",
                "minecraft:chest"
        );

        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "test/objective_ui",
                new QuestDisplay("Objective UI", "", List.of(), Map.of("Main", ChapterDefinition.DEFAULT), "minecraft:book", "minecraft:barrier"),
                QuestSettings.DEFAULT,
                Set.of(),
                Map.of(itemTask.id(), itemTask, checkTask.id(), checkTask, biomeTask.id(), biomeTask),
                Map.of(itemReward.id(), itemReward, xpReward.id(), xpReward, commandReward.id(), commandReward, lootReward.id(), lootReward)
        );

        JsonElement encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Encode failed: " + message);
                });
        QuestDefinition decoded = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Decode failed: " + message);
                });

        assertUiFields(decoded.tasks().get("item"), "Collect dirt", "minecraft:dirt");
        assertUiFields(decoded.tasks().get("manual"), "Manual step", "minecraft:lever");
        assertUiFields(decoded.tasks().get("biome"), "Find desert", "minecraft:sand");
        assertUiFields(decoded.rewards().get("reward_item"), "Reward diamond", "minecraft:diamond");
        assertUiFields(decoded.rewards().get("reward_xp"), "Reward XP", "minecraft:experience_bottle");
        assertUiFields(decoded.rewards().get("reward_command"), "Reward command", "minecraft:command_block");
        assertUiFields(decoded.rewards().get("reward_loot"), "Reward loot", "minecraft:chest");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void objectiveOrderRoundtrip(GameTestHelper helper) {
        QuestTaskDefinition firstTask = QuestGameTestDefinitions.task("first", "check", 1, "a", Map.of());
        QuestTaskDefinition secondTask = QuestGameTestDefinitions.task("second", "check", 1, "b", Map.of());
        QuestRewardDefinition firstReward = QuestGameTestDefinitions.reward("first_reward", "xp", 3, "", false, Map.of());
        QuestRewardDefinition secondReward = QuestGameTestDefinitions.reward("second_reward", "xp", 5, "", false, Map.of());
        Map<String, QuestTaskDefinition> tasks = new LinkedHashMap<>();
        tasks.put(secondTask.id(), secondTask);
        tasks.put(firstTask.id(), firstTask);
        Map<String, QuestRewardDefinition> rewards = new LinkedHashMap<>();
        rewards.put(secondReward.id(), secondReward);
        rewards.put(firstReward.id(), firstReward);

        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                "test/objective_order",
                new QuestDisplay("Objective Order", "", List.of(), Map.of("Main", ChapterDefinition.DEFAULT), "minecraft:book", "minecraft:barrier"),
                QuestSettings.DEFAULT,
                Set.of(),
                tasks,
                rewards
        );

        JsonElement encoded = QuestDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Encode failed: " + message);
                });
        QuestDefinition decoded = QuestDefinition.CODEC.parse(JsonOps.INSTANCE, encoded)
                .getOrThrow(false, message -> {
                    throw new GameTestAssertException("Decode failed: " + message);
                });

        if (!List.of("second", "first").equals(decoded.tasksOrder())
                || !List.of("second_reward", "first_reward").equals(decoded.rewardsOrder())
                || !List.of("second", "first").equals(new java.util.ArrayList<>(decoded.tasks().keySet()))) {
            throw new GameTestAssertException("Objective order did not roundtrip");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void schemaMigratorStampsFreshVersion(GameTestHelper helper) {
        JsonObject source = JsonParser.parseString("""
                {
                  "schema": 99,
                  "schema_version": 99,
                  "id": "test/fresh_schema",
                  "display": {
                    "title": "Fresh",
                    "subtitle": "",
                    "description": [],
                    "groups": {},
                    "icon": "minecraft:book",
                    "icon_background": "minecraft:barrier"
                  },
                  "settings": {
                    "individual_progress": false,
                    "hidden_mode": "prerequisites_visible",
                    "repeatable": false,
                    "auto_claim_rewards": false,
                    "unlock_notification": false,
                    "%s": true
                  },
                  "%s": [],
                  "tasks": {},
                  "rewards": {}
                }
                """.formatted(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD, QuestDefinition.PREREQUISITES_FIELD)).getAsJsonObject();

        JsonObject migrated = QuestSchemaMigrator.migrate(source);
        if (!migrated.has("schema") || migrated.get("schema").getAsInt() != QuestDefinition.CURRENT_SCHEMA) {
            throw new GameTestAssertException("Schema should be stamped with the current version");
        }
        if (!migrated.has("schema_version") || migrated.get("schema_version").getAsInt() != QuestDefinition.CURRENT_SCHEMA) {
            throw new GameTestAssertException("Schema version should be stamped with the current version");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void stableIdAllocatorUsesNextAvailableSuffix(GameTestHelper helper) {
        String next = StableIdAllocator.nextId("Text Box", List.of("text_box_0001", "text_box_0002"));
        if (!"text_box_0003".equals(next)) {
            throw new GameTestAssertException("Stable ID allocator should use the next available suffix");
        }
        String fallback = StableIdAllocator.nextId("", List.of("id_0001"));
        if (!"id_0002".equals(fallback)) {
            throw new GameTestAssertException("Stable ID allocator should normalize blank prefixes");
        }
        helper.succeed();
    }

    private static void assertUiFields(Object value, String title, String icon) {
        boolean matches = false;
        if (value instanceof GatherItemQuestTaskDefinition task) {
            matches = title.equals(task.title()) && icon.equals(task.icon());
        } else if (value instanceof CheckQuestTaskDefinition task) {
            matches = title.equals(task.title()) && icon.equals(task.icon());
        } else if (value instanceof SimpleQuestTaskDefinition task) {
            matches = title.equals(task.title()) && icon.equals(task.icon());
        } else if (value instanceof ItemQuestRewardDefinition reward) {
            matches = title.equals(reward.title()) && icon.equals(reward.icon());
        } else if (value instanceof XpQuestRewardDefinition reward) {
            matches = title.equals(reward.title()) && icon.equals(reward.icon());
        } else if (value instanceof CommandQuestRewardDefinition reward) {
            matches = title.equals(reward.title()) && icon.equals(reward.icon());
        } else if (value instanceof LootTableQuestRewardDefinition reward) {
            matches = title.equals(reward.title()) && icon.equals(reward.icon());
        }
        if (!matches) {
            throw new GameTestAssertException("Objective UI fields did not roundtrip for " + value);
        }
    }

    private static ResourceLocation mc(String path) {
        return ResourceLocation.tryBuild("minecraft", path);
    }
}
