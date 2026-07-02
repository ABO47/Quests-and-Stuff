package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.BiFunction;

final class QuestObjectiveTypeCatalog {
    private static final List<QuestDetailsTypeChoice> TASK_CHOICES = List.of(
            task(QuestTranslationKeys.TYPE_ACQUIRE_ITEM, "item", "icon", QuestObjectiveEditFlow.ITEM_SOURCE_PICKER, TaskJsonFactory::itemTask, "item"),
            task(QuestTranslationKeys.TYPE_USE_ITEM, "item_use", "item_use", QuestObjectiveEditFlow.SIMPLE_ICON_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_INTERACT_ITEM, "item_interact", "item_interact", QuestObjectiveEditFlow.SIMPLE_ICON_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_KILL_ENTITY, "kill_entity", "kill_entity", QuestObjectiveEditFlow.ENTITY_ICON_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_INTERACT_ENTITY, "entity_interact", "entity", QuestObjectiveEditFlow.ENTITY_ICON_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_ADVANCEMENT, "advancement", "trophy", QuestObjectiveEditFlow.ADVANCEMENT_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_RECIPE, "recipe", "recipe", QuestObjectiveEditFlow.RECIPE_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_VISIT_STRUCTURE, "structure", "pyramid", QuestObjectiveEditFlow.STRUCTURE_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_INTERACT_BLOCK, "block_interact", "box", QuestObjectiveEditFlow.BLOCK_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_STAT, "stat", "stat", QuestObjectiveEditFlow.STAT_PICKER, TaskJsonFactory::statTask, "target"),
            task(QuestTranslationKeys.TYPE_XP, "xp", "xp", QuestObjectiveEditFlow.XP_PICKER, TaskJsonFactory::xpTask, "amount", "mode", "collection"),
            task(QuestTranslationKeys.TYPE_MANUAL_CHECK, "check", "manual_check", QuestObjectiveEditFlow.DIRECT_JSON, TaskJsonFactory::checkTask, "target"),
            task(QuestTranslationKeys.TYPE_VISIT_BIOME, "biome", "biome", QuestObjectiveEditFlow.BIOME_PICKER, TaskJsonFactory::simpleDefaultTask, "target"),
            task(QuestTranslationKeys.TYPE_VISIT_LOCATION, "location", "orbit", QuestObjectiveEditFlow.DIMENSION_PICKER, TaskJsonFactory::locationTask, "dimension")
    );
    private static final List<QuestDetailsTypeChoice> REWARD_CHOICES = List.of(
            reward(QuestTranslationKeys.TYPE_GIVE_ITEM, "item", "icon", QuestObjectiveEditFlow.ITEM_SOURCE_PICKER, TaskJsonFactory::itemReward, "item"),
            reward(QuestTranslationKeys.TYPE_XP, "xp", "xp", QuestObjectiveEditFlow.XP_PICKER, TaskJsonFactory::xpReward, "amount", "mode"),
            reward(QuestTranslationKeys.TYPE_COMMAND, "command", "open", QuestObjectiveEditFlow.COMMAND_EDITOR, TaskJsonFactory::commandReward, "command"),
            reward(QuestTranslationKeys.TYPE_LOOT_TABLE, "loot_table", "icon", QuestObjectiveEditFlow.LOOT_TABLE_PICKER, TaskJsonFactory::lootTableReward, "loot_table")
    );

    private QuestObjectiveTypeCatalog() {
    }

    static List<QuestDetailsTypeChoice> taskChoices() {
        return TASK_CHOICES;
    }

    static List<QuestDetailsTypeChoice> rewardChoices() {
        return REWARD_CHOICES;
    }

    static QuestDetailsTypeChoice taskChoice(String typePath) {
        return choice(TASK_CHOICES, typePath);
    }

    static QuestDetailsTypeChoice rewardChoice(String typePath) {
        return choice(REWARD_CHOICES, typePath);
    }

    private static QuestDetailsTypeChoice task(String labelKey, String type, String icon, QuestObjectiveEditFlow editFlow, BiFunction<String, String, JsonObject> defaultJsonFactory, String... requiredJsonFields) {
        return new QuestDetailsTypeChoice(labelKey, type, icon, editFlow, id -> defaultJsonFactory.apply(id, type), List.of(requiredJsonFields));
    }

    private static QuestDetailsTypeChoice reward(String labelKey, String type, String icon, QuestObjectiveEditFlow editFlow, BiFunction<String, String, JsonObject> defaultJsonFactory, String... requiredJsonFields) {
        return new QuestDetailsTypeChoice(labelKey, type, icon, editFlow, id -> defaultJsonFactory.apply(id, type), List.of(requiredJsonFields));
    }

    private static QuestDetailsTypeChoice choice(List<QuestDetailsTypeChoice> choices, String typePath) {
        String normalized = TaskJsonFactory.typePath(typePath);
        for (QuestDetailsTypeChoice choice : choices) {
            if (choice.type().equals(normalized)) {
                return choice;
            }
        }
        return null;
    }
}
