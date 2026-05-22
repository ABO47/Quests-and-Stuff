package com.abo47.questsandstuff.gametest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.task.item.CollectionMode;
import com.abo47.questsandstuff.quest.model.task.item.GatherItemQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTasks;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestTaskDefinitionGameTests {
    private QuestTaskDefinitionGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void defaultTaskTypesEvaluateDeterministically(GameTestHelper helper) {
        QuestTasks.bootstrapDefaults();
        QuestProgressState current = new QuestProgressState();

        requirePositive(simple("adv", "advancement", "minecraft:story/mine_stone"),
                signal(QuestSignalType.ADVANCEMENT, "minecraft:story/mine_stone", 1), current);
        requirePositive(simple("bio", "biome", "minecraft:plains"),
                signal(QuestSignalType.BIOME_ENTER, "minecraft:plains", 1), current);
        requirePositive(simple("block", "block_interact", "minecraft:crafting_table"),
                signal(QuestSignalType.BLOCK_INTERACT, "minecraft:crafting_table", 1), current);
        requirePositive(simple("dim", "changed_dimension", "minecraft:the_nether"),
                signal(QuestSignalType.DIMENSION_CHANGED, "minecraft:the_nether", 1), current);
        requirePositive(simple("check", "check", "creator/check"),
                signal(QuestSignalType.MANUAL_CHECK, "creator/check", 1), current);
        requirePositive(simple("dummy", "dummy", "creator/dummy"),
                signal(QuestSignalType.MANUAL_CHECK, "creator/dummy", 1), current);
        requirePositive(simple("entity", "entity_interact", "minecraft:villager"),
                signal(QuestSignalType.ENTITY_INTERACT, "minecraft:villager", 1), current);

        QuestTaskDefinition gatherAutomatic = task("gather_auto", "item", "minecraft:oak_log", Map.of());
        requirePositive(gatherAutomatic, signal(QuestSignalType.ITEM_COLLECTED, "minecraft:oak_log", 4), current);
        assertPickupSnapshotDoesNotDoubleCount();
        QuestTaskDefinition gatherManual = task("gather_manual", "item", "minecraft:oak_log", Map.of("collection_mode", "manual"));
        requirePositive(gatherManual, signal(QuestSignalType.MANUAL_ITEM_SUBMIT, "minecraft:oak_log", 2), current);
        QuestTaskDefinition gatherConsume = task("gather_consume", "item", "minecraft:oak_log", Map.of("collection_mode", "consume"));
        requirePositive(gatherConsume, signal(QuestSignalType.MANUAL_ITEM_SUBMIT, "minecraft:oak_log", 2), current);

        requirePositive(simple("item_interact", "item_interact", "minecraft:book"),
                signal(QuestSignalType.ITEM_INTERACT, "minecraft:book", 1), current);
        requirePositive(simple("item_use", "item_use", "minecraft:bread"),
                signal(QuestSignalType.ITEM_USED, "minecraft:bread", 1), current);
        requirePositive(simple("kill", "kill_entity", "minecraft:zombie"),
                signal(QuestSignalType.ENTITY_KILLED, "minecraft:zombie", 1), current);

        QuestTaskDefinition location = task("loc", "location", "minecraft:overworld", Map.of("mode", "dimension"));
        requirePositive(location, signal(QuestSignalType.LOCATION_TICK, "", 1), current);

        requirePositive(simple("recipe", "recipe", "minecraft:oak_planks"),
                signal(QuestSignalType.ITEM_CRAFTED, "minecraft:oak_planks", 1), current);

        QuestTaskDefinition stat = QuestGameTestDefinitions.task("stat", "stat", 10, "minecraft:mined:minecraft:stone", Map.of());
        current.addTaskCount("stat", 2, Integer.MAX_VALUE);
        requirePositive(stat, signal(QuestSignalType.STAT_CHANGE, "minecraft:mined:minecraft:stone", 7), current);

        requirePositive(simple("structure", "structure", "minecraft:village"),
                signal(QuestSignalType.STRUCTURE_ENTER, "minecraft:village", 1), current);

        QuestTaskDefinition xpPoints = task("xp_points", "xp", "", Map.of("mode", "points", "collection_mode", "automatic"));
        requirePositive(xpPoints, signal(QuestSignalType.XP_CHANGE, "", 5), current);
        QuestTaskDefinition xpLevel = QuestGameTestDefinitions.task("xp_level", "xp", 10, "", Map.of("mode", "level", "collection_mode", "automatic"));
        current.addTaskCount("xp_level", 2, Integer.MAX_VALUE);
        requirePositive(xpLevel, signal(QuestSignalType.XP_SNAPSHOT, "level", 6), current);
        QuestTaskDefinition xpManual = task("xp_manual", "xp", "", Map.of("mode", "points", "collection_mode", "manual"));
        requirePositive(xpManual, signal(QuestSignalType.MANUAL_XP_SUBMIT, "", 3), current);

        QuestTaskDefinition composite = task("composite", "composite", "", Map.of("children", "a,b", "required", "1"));
        int compositeDelta = evaluate(composite, signal(QuestSignalType.ITEM_COLLECTED, "minecraft:oak_log", 1), current);
        if (compositeDelta != 0) {
            throw new GameTestAssertException("Composite task should be deterministic no-op in signal stage");
        }

        for (String requiredType : List.of(
                "advancement", "biome", "block_interact", "changed_dimension", "check", "composite", "dummy",
                "entity_interact", "item_interact", "item_use", "kill_entity",
                "location", "recipe", "stat", "structure", "xp"
        )) {
            if (QuestTasks.get(id(requiredType)) == null) {
                throw new GameTestAssertException("Missing default task type registration: " + requiredType);
            }
        }

        helper.succeed();
    }

    private static QuestTaskDefinition simple(String id, String type, String target) {
        return task(id, type, target, Map.of());
    }

    private static QuestTaskDefinition task(String id, String type, String target, Map<String, String> args) {
        return QuestGameTestDefinitions.task(id, type, 1, target, args);
    }

    private static void requirePositive(QuestTaskDefinition definition, QuestSignal signal, QuestProgressState current) {
        int delta = evaluate(definition, signal, current);
        if (delta <= 0) {
            throw new GameTestAssertException("Expected positive delta for task type " + definition.type() + " but got " + delta);
        }
    }

    private static int evaluate(QuestTaskDefinition definition, QuestSignal signal, QuestProgressState current) {
        Tag before = current.getTaskProgress(definition.id(), definition);
        Tag after = definition.test(before, signal);
        return Math.max(0, Math.round((definition.getProgress(after) - definition.getProgress(before)) * definition.safeGoal()));
    }

    private static void assertPickupSnapshotDoesNotDoubleCount() {
        QuestTaskDefinition logs = new GatherItemQuestTaskDefinition(
                "tagged_logs",
                id("item"),
                ResourceLocation.tryParse("minecraft:air"),
                "minecraft:logs",
                "",
                10,
                CollectionMode.AUTOMATIC,
                "",
                ""
        );
        Tag afterPickup = logs.test(logs.defaultProgress(), signal(QuestSignalType.ITEM_COLLECTED, "minecraft:oak_log", 2));
        Tag afterSnapshot = logs.test(afterPickup, signal(QuestSignalType.INVENTORY_CHANGED, "minecraft:oak_log", 2));
        Tag afterRepeatPickup = logs.test(afterSnapshot, signal(QuestSignalType.ITEM_COLLECTED, "minecraft:oak_log", 2));
        int count = Math.round(logs.getProgress(afterRepeatPickup) * logs.safeGoal());
        if (count != 2) {
            throw new GameTestAssertException("Tagged log pickup should count 2, got " + count);
        }
    }

    private static QuestSignal signal(QuestSignalType type, String key, int amount) {
        return new QuestSignal(type, null, key, amount, BlockPos.ZERO, Level.OVERWORLD);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, path);
    }
}
