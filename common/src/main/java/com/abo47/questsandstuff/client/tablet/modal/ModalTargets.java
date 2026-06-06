package com.abo47.questsandstuff.client.tablet.modal;

public final class ModalTargets {
    public static final String TASK_ITEM = "task_item";
    public static final String TASK_INVENTORY_ITEM = "task_inventory_item";
    public static final String TASK_BIOME = "task_biome";
    public static final String TASK_ADVANCEMENT = "task_advancement";
    public static final String TASK_RECIPE = "task_recipe";
    public static final String TASK_STRUCTURE = "task_structure";
    public static final String TASK_BLOCK = "task_block";
    public static final String TASK_STAT = "task_stat";
    public static final String TASK_DIMENSION = "task_dimension";
    public static final String TASK_ENTITY = "task_entity";
    public static final String TASK_SIMPLE_ICON = "task_simple_icon";
    public static final String TASK_ICON = "task_icon";
    public static final String REWARD_ITEM = "reward_item";
    public static final String REWARD_INVENTORY_ITEM = "reward_inventory_item";
    public static final String REWARD_LOOT_TABLE = "reward_loot_table";
    public static final String REWARD_ICON = "reward_icon";
    public static final String REWARD_COMMAND_EDITOR_ICON = "reward_command_editor_icon";
    public static final String OBJECTIVE_TASK = "objective_task";
    public static final String OBJECTIVE_REWARD = "objective_reward";
    public static final String QUEST_ICON = "quest_icon";
    public static final String CHAPTER_ICON = "chapter_icon";
    public static final String QUEST_DETAILS = "quest_details";
    public static final String DESC_BACKGROUND = "desc_background";
    public static final String DESC_IMAGE = "desc_image";
    public static final String DESC_IMAGE_NEW = "desc_image_new";
    public static final String DESC_ENTITY = "desc_entity";
    public static final String DESC_ENTITY_NEW = "desc_entity_new";
    public static final String DESC_ITEM = "desc_item";
    public static final String DESC_ITEM_NEW = "desc_item_new";
    public static final String DESC_BLOCK = "desc_block";
    public static final String DESC_BLOCK_NEW = "desc_block_new";
    public static final String DESC_RECIPE = "desc_recipe";
    public static final String DESC_RECIPE_NEW = "desc_recipe_new";
    public static final String QUEST_DESC_TEXT = "quest_desc_text";
    public static final String CANVAS_ENTITY_NEW = "new";
    public static final String CANVAS_ENTITY_CHANGE = "change";
    public static final String CANVAS_ITEM_NEW = "item_new";
    public static final String CANVAS_ITEM_CHANGE = "item_change";
    public static final String CANVAS_BLOCK_NEW = "block_new";
    public static final String CANVAS_BLOCK_CHANGE = "block_change";
    public static final String CANVAS_RECIPE_NEW = "recipe_new";
    public static final String CANVAS_RECIPE_CHANGE = "recipe_change";
    public static final String CANVAS_IMAGE = "canvas";
    public static final String CANVAS_TEXT = "canvas_text";
    public static final String CONNECTION = "connection";
    public static final String CONNECTION_SELECTION = "connection_selection";
    public static final String GRID_COLOR = "grid_color";

    private ModalTargets() {
    }

    public static String of(String kind, Object... parts) {
        StringBuilder builder = new StringBuilder(clean(kind));
        if (parts != null) {
            for (Object part : parts) {
                builder.append('|').append(clean(part));
            }
        }
        return builder.toString();
    }

    public static String taskItem(String questId, String taskId, String type) {
        return of(TASK_ITEM, questId, taskId, type);
    }

    public static String taskInventoryItem(String questId, String taskId, String type) {
        return of(TASK_INVENTORY_ITEM, questId, taskId, type);
    }

    public static String taskBiome(String questId, String taskId, String type) {
        return of(TASK_BIOME, questId, taskId, type);
    }

    public static String taskAdvancement(String questId, String taskId, String type) {
        return of(TASK_ADVANCEMENT, questId, taskId, type);
    }

    public static String taskRecipe(String questId, String taskId, String type) {
        return of(TASK_RECIPE, questId, taskId, type);
    }

    public static String taskStructure(String questId, String taskId, String type) {
        return of(TASK_STRUCTURE, questId, taskId, type);
    }

    public static String taskBlock(String questId, String taskId, String type) {
        return of(TASK_BLOCK, questId, taskId, type);
    }

    public static String taskStat(String questId, String taskId, String type) {
        return of(TASK_STAT, questId, taskId, type);
    }

    public static String taskDimension(String questId, String taskId, String type) {
        return of(TASK_DIMENSION, questId, taskId, type);
    }

    public static String taskEntity(String questId, String taskId, String type) {
        return of(TASK_ENTITY, questId, taskId, type);
    }

    public static String taskSimpleIcon(String questId, String taskId, String type) {
        return of(TASK_SIMPLE_ICON, questId, taskId, type);
    }

    public static String taskIcon(String questId, String taskId) {
        return of(TASK_ICON, questId, taskId, "icon");
    }

    public static String rewardItem(String questId, String rewardId, String type) {
        return of(REWARD_ITEM, questId, rewardId, type);
    }

    public static String rewardInventoryItem(String questId, String rewardId, String type) {
        return of(REWARD_INVENTORY_ITEM, questId, rewardId, type);
    }

    public static String rewardLootTable(String questId, String rewardId, String type) {
        return of(REWARD_LOOT_TABLE, questId, rewardId, type);
    }

    public static String rewardIcon(String questId, String rewardId) {
        return of(REWARD_ICON, questId, rewardId, "icon");
    }

    public static String rewardCommandEditorIcon(String questId, String rewardId) {
        return of(REWARD_COMMAND_EDITOR_ICON, questId, rewardId, "icon");
    }

    public static String objectiveTask(String questId, String taskId) {
        return of(OBJECTIVE_TASK, questId, taskId);
    }

    public static String objectiveReward(String questId, String rewardId) {
        return of(OBJECTIVE_REWARD, questId, rewardId);
    }

    public static String questIcon(String questId) {
        return of(QUEST_ICON, questId);
    }

    public static String chapterIcon(String chapter) {
        return of(CHAPTER_ICON, chapter);
    }

    public static String questDetailsImage(String questId, String imageId) {
        return of(QUEST_DETAILS, questId, imageId);
    }

    public static String descBackground(String questId) {
        return of(DESC_BACKGROUND, questId);
    }

    public static String descImage(String questId, String imageId) {
        return of(DESC_IMAGE, questId, imageId);
    }

    public static String descImageNew(String questId, String imageId, int x, int y) {
        return of(DESC_IMAGE_NEW, questId, imageId, x, y);
    }

    public static String descEntity(String questId, String imageId) {
        return of(DESC_ENTITY, questId, imageId);
    }

    public static String descEntityNew(String questId, String imageId, int x, int y) {
        return of(DESC_ENTITY_NEW, questId, imageId, x, y);
    }

    public static String descItem(String questId, String imageId) {
        return of(DESC_ITEM, questId, imageId);
    }

    public static String descItemNew(String questId, String imageId, int x, int y) {
        return of(DESC_ITEM_NEW, questId, imageId, x, y);
    }

    public static String descBlock(String questId, String imageId) {
        return of(DESC_BLOCK, questId, imageId);
    }

    public static String descBlockNew(String questId, String imageId, int x, int y) {
        return of(DESC_BLOCK_NEW, questId, imageId, x, y);
    }

    public static String descRecipe(String questId, String imageId) {
        return of(DESC_RECIPE, questId, imageId);
    }

    public static String descRecipeNew(String questId, String imageId, int x, int y) {
        return of(DESC_RECIPE_NEW, questId, imageId, x, y);
    }

    public static String questDescText(String questId, String textId) {
        return of(QUEST_DESC_TEXT, questId, textId);
    }

    public static String canvasEntityNew(String group) {
        return of(CANVAS_ENTITY_NEW, group);
    }

    public static String canvasEntityChange(String group, String imageId) {
        return of(CANVAS_ENTITY_CHANGE, group, imageId);
    }

    public static String canvasItemNew(String group) {
        return of(CANVAS_ITEM_NEW, group);
    }

    public static String canvasItemChange(String group, String imageId) {
        return of(CANVAS_ITEM_CHANGE, group, imageId);
    }

    public static String canvasBlockNew(String group) {
        return of(CANVAS_BLOCK_NEW, group);
    }

    public static String canvasBlockChange(String group, String imageId) {
        return of(CANVAS_BLOCK_CHANGE, group, imageId);
    }

    public static String canvasRecipeNew(String group) {
        return of(CANVAS_RECIPE_NEW, group);
    }

    public static String canvasRecipeChange(String group, String imageId) {
        return of(CANVAS_RECIPE_CHANGE, group, imageId);
    }

    public static String canvasImage(String group, String imageId) {
        return of(CANVAS_IMAGE, group, imageId);
    }

    public static String canvasText(String group, String textId) {
        return of(CANVAS_TEXT, group, textId);
    }

    public static String connection(String group, String sourceQuestId, String targetQuestId) {
        return of(CONNECTION, group, sourceQuestId, targetQuestId);
    }

    public static String connectionSelection(String group) {
        return of(CONNECTION_SELECTION, group);
    }

    public static String gridColor() {
        return of(GRID_COLOR);
    }

    public static String doubleClickKey(String picker, Object... parts) {
        return of(picker, parts);
    }

    private static String clean(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
