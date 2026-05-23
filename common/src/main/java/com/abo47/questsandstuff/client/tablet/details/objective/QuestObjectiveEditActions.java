package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class QuestObjectiveEditActions {
    private QuestObjectiveEditActions() {
    }

    static void applyIconPick(Player player, TabletUiState state, String entry) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || entry == null || entry.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4)) {
            return;
        }
        String questId = parsed.questId();
        String id = parsed.entryId();
        String type = parsed.type();
        if (parsed.isTaskItem()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("type", type);
            if (entry.startsWith("#")) {
                json.addProperty("item", "minecraft:air");
                json.addProperty("tag", entry.substring(1));
            } else {
                json.addProperty("item", entry);
            }
            json.addProperty("amount", 1);
            json.addProperty("nbt", "");
            json.addProperty("collection", "automatic");
            EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details task item picked quest={} task={} entry={}", questId, id, entry);
        } else if (parsed.isTaskSimpleIcon()) {
            JsonObject json = QuestObjectiveJsons.simpleTask(id, type, entry, entry);
            EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details task icon picked quest={} task={} entry={}", questId, id, entry);
        } else if (parsed.isTaskEntity()) {
            String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(entry);
            if (!entityId.isBlank()) {
                JsonObject json = QuestObjectiveJsons.simpleTask(id, type, entityId, "");
                EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details task entity picked quest={} task={} entity={} egg={}", questId, id, entityId, entry);
            } else {
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details task entity pick ignored quest={} task={} item={}", questId, id, entry);
            }
        } else if (parsed.isTaskIcon()) {
            putObjectiveIcon(player, questId, id, entry, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details task icon changed quest={} task={} icon={}", questId, id, entry);
        } else if (parsed.isRewardItem() && !entry.startsWith("#")) {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("type", type);
            json.addProperty("item", entry);
            json.addProperty("amount", 1);
            json.addProperty("nbt", "");
            EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward item picked quest={} reward={} item={}", questId, id, entry);
        } else if (parsed.isRewardCommandEditorIcon()) {
            state.questDetailsCommandRewardIcon = entry.startsWith("#") ? entry.substring(1) : entry;
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details command reward icon picked quest={} reward={} icon={}", questId, id, entry);
        } else if (parsed.isRewardIcon()) {
            putObjectiveIcon(player, questId, id, entry, false);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward icon changed quest={} reward={} icon={}", questId, id, entry);
        }
        state.questDetailsPickTarget = "";
    }

    static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || stack == null || stack.isEmpty()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4)) {
            return;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (parsed.isTaskInventoryItem()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", parsed.entryId());
            json.addProperty("type", parsed.type());
            json.addProperty("item", itemId);
            json.addProperty("amount", 1);
            json.addProperty("nbt", stack.hasTag() ? stack.getTag().toString() : "");
            json.addProperty("collection", "automatic");
            EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
            state.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details inventory item picked quest={} task={} item={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.hasTag());
            return;
        }
        if (parsed.isRewardInventoryItem()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", parsed.entryId());
            json.addProperty("type", parsed.type());
            json.addProperty("item", itemId);
            json.addProperty("amount", Math.max(1, stack.getCount()));
            json.addProperty("nbt", stack.hasTag() ? stack.getTag().toString() : "");
            EditorCommandClient.putQuestRewardJson(player, parsed.questId(), json.toString());
            state.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details inventory reward item picked quest={} reward={} item={} amount={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.getCount(), stack.hasTag());
        }
    }

    static void applyBiomePick(Player player, TabletUiState state, String biome) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || biome == null || biome.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskBiome()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), biome, "biome");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details biome picked quest={} task={} biome={} defaultIcon=biome", parsed.questId(), parsed.entryId(), biome);
    }

    static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || advancement == null || advancement.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskAdvancement()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), advancement.trim(), "trophy");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details advancement picked quest={} task={} advancement={}", parsed.questId(), parsed.entryId(), advancement.trim());
    }

    static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || recipe == null || recipe.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskRecipe()) {
            return;
        }
        String recipeTarget = recipe.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), recipeTarget, recipeIcon(recipeTarget));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details recipe picked quest={} task={} recipe={}", parsed.questId(), parsed.entryId(), recipeTarget);
    }

    static void applyStructurePick(Player player, TabletUiState state, String structure) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || structure == null || structure.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskStructure()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), structure.trim(), "pyramid");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details structure picked quest={} task={} structure={}", parsed.questId(), parsed.entryId(), structure.trim());
    }

    static void applyBlockPick(Player player, TabletUiState state, String block) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || block == null || block.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskBlock()) {
            return;
        }
        String blockId = block.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), blockId, blockIcon(blockId));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details block picked quest={} task={} block={}", parsed.questId(), parsed.entryId(), blockId);
    }

    static void applyStatPick(Player player, TabletUiState state, String stat) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || stat == null || stat.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskStat()) {
            return;
        }
        String statTarget = stat.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), statTarget, statIcon(statTarget));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details stat picked quest={} task={} stat={}", parsed.questId(), parsed.entryId(), statTarget);
    }

    static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || dimension == null || dimension.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4) || !parsed.isTaskDimension()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.defaultTask(parsed.entryId(), "location");
        json.addProperty("type", parsed.type());
        json.addProperty("dimension", dimension.trim());
        json.addProperty("icon", "minecraft:compass");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details dimension picked quest={} task={} dimension={}", parsed.questId(), parsed.entryId(), dimension.trim());
    }

    static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        if (target.isBlank() || lootTable == null || lootTable.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(3) || !parsed.isRewardLootTable()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(parsed.questId());
        CompoundTag reward = quest.getCompound("rewards").getCompound(parsed.entryId());
        JsonObject existing = QuestObjectiveJsons.read(reward.getString("json"));
        String type = !parsed.type().isBlank() ? parsed.type() : QuestObjectiveJsons.MOD + "loot_table";
        JsonObject json = QuestObjectiveLootTableRewardEditor.isLootTable(existing)
                ? existing.deepCopy()
                : QuestObjectiveJsons.defaultReward(parsed.entryId(), QuestObjectiveJsons.typePath(type));
        json.addProperty("id", parsed.entryId());
        json.addProperty("type", type);
        json.remove("fallback_item");
        json.remove("amount");
        json.addProperty("title", QuestObjectiveLootTableRewardEditor.displayName(lootTable));
        if (!json.has("icon")) {
            json.addProperty("icon", "minecraft:chest");
        }
        json.addProperty("loot_table", lootTable.trim());
        ClientQuestCache.putQuestRewardJsonLocal(parsed.questId(), json.toString());
        EditorCommandClient.putQuestRewardJson(player, parsed.questId(), json.toString());
        state.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details loot table picked quest={} reward={} lootTable={}", parsed.questId(), parsed.entryId(), lootTable.trim());
    }

    static void beginTaskAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        String type = QuestObjectiveJsons.MOD + typePath;
        String id = QuestObjectiveJsons.nextId(quest.getCompound("tasks"), "task_" + typePath);
        beginTask(player, state, questId, id, type, typePath, true);
    }

    static void beginTaskChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        String type = QuestObjectiveJsons.MOD + typePath;
        beginTask(player, state, questId, id, type, typePath, false);
    }

    static void beginRewardAdd(Player player, TabletUiState state, String questId, CompoundTag quest, String typePath) {
        String type = QuestObjectiveJsons.MOD + typePath;
        String id = QuestObjectiveJsons.nextId(quest.getCompound("rewards"), "reward_" + typePath);
        beginReward(player, state, questId, id, type, typePath, true);
    }

    static void beginRewardChange(Player player, TabletUiState state, String questId, String id, String typePath) {
        String type = QuestObjectiveJsons.MOD + typePath;
        beginReward(player, state, questId, id, type, typePath, false);
    }

    static void openCommandRewardEditor(TabletUiState state, String questId, String id, String command, String title, String icon) {
        QuestDetailsTransientState.openCommandRewardEditor(state, questId, id, command, title, icon);
    }

    static void openExistingCommandRewardEditor(TabletUiState state, String questId, String id) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag reward = quest.getCompound("rewards").getCompound(id);
        JsonObject json = QuestObjectiveJsons.read(reward.getString("json"));
        openCommandRewardEditor(
                state,
                questId,
                id,
                QuestObjectiveJsons.asString(json, "command", ""),
                QuestObjectiveJsons.asString(json, "title", "Command"),
                QuestObjectiveJsons.asString(json, "icon", "minecraft:command_block")
        );
    }

    static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        JsonObject json = QuestObjectiveJsons.read(entries.getCompound(id).getString("json"));
        QuestDetailsTransientState.openObjectiveRename(
                state,
                questId,
                id,
                task,
                QuestObjectiveDisplayText.displayName(json, QuestObjectiveJsons.asString(json, "type", ""))
        );
    }

    static void putObjectiveTitle(Player player, String questId, String id, String title, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = QuestObjectiveJsons.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isBlank()) {
            json.remove("title");
        } else {
            json.addProperty("title", normalizedTitle);
        }
        if (task) {
            EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
        } else {
            EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details objective renamed quest={} id={} task={} title={}", questId, id, task, normalizedTitle);
    }

    private static void beginTask(Player player, TabletUiState state, String questId, String id, String type, String typePath, boolean add) {
        if ("item".equals(typePath)) {
            QuestDetailsTransientState.openItemSourcePicker(state, ModalTargets.taskItem(questId, id, type));
            return;
        }
        if ("biome".equals(typePath)) {
            QuestDetailsWindow.openBiomePicker(state, ModalTargets.taskBiome(questId, id, type));
            return;
        }
        if ("advancement".equals(typePath)) {
            QuestDetailsWindow.openAdvancementPicker(state, ModalTargets.taskAdvancement(questId, id, type));
            return;
        }
        if ("recipe".equals(typePath)) {
            QuestDetailsWindow.openRecipePicker(state, ModalTargets.taskRecipe(questId, id, type));
            return;
        }
        if ("structure".equals(typePath)) {
            QuestDetailsWindow.openStructurePicker(state, ModalTargets.taskStructure(questId, id, type));
            return;
        }
        if ("block_interact".equals(typePath) || "block_interaction".equals(typePath)) {
            QuestDetailsWindow.openBlockPicker(state, ModalTargets.taskBlock(questId, id, type));
            return;
        }
        if ("stat".equals(typePath)) {
            QuestDetailsWindow.openStatPicker(state, ModalTargets.taskStat(questId, id, type));
            return;
        }
        if ("location".equals(typePath)) {
            QuestDetailsWindow.openDimensionPicker(state, ModalTargets.taskDimension(questId, id, type));
            return;
        }
        if ("kill_entity".equals(typePath) || "entity_interact".equals(typePath) || "entity_interaction".equals(typePath)) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskEntity(questId, id, type));
            return;
        }
        if ("xp".equals(typePath)) {
            QuestDetailsTransientState.openXpPicker(state, questId, id, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp requirement picker open quest={} task={} add={}", questId, id, add);
            return;
        }
        if ("item_use".equals(typePath) || "item_interact".equals(typePath) || "item_interaction".equals(typePath)) {
            QuestDetailsWindow.openIconPicker(state, ModalTargets.taskSimpleIcon(questId, id, type));
            return;
        }
        EditorCommandClient.putQuestTaskJson(player, questId, QuestObjectiveJsons.defaultTask(id, typePath).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} task quest={} task={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    private static void beginReward(Player player, TabletUiState state, String questId, String id, String type, String typePath, boolean add) {
        if ("item".equals(typePath)) {
            QuestDetailsTransientState.openItemSourcePicker(state, ModalTargets.rewardItem(questId, id, type));
            return;
        }
        if ("command".equals(typePath)) {
            openCommandRewardEditor(state, questId, id, "", "Command", "minecraft:command_block");
            return;
        }
        if ("loot_table".equals(typePath) || "loot".equals(typePath)) {
            QuestDetailsWindow.openLootTablePicker(state, ModalTargets.rewardLootTable(questId, id, type));
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details loot table picker open quest={} reward={} type={} add={}", questId, id, typePath, add);
            return;
        }
        if ("xp".equals(typePath)) {
            QuestDetailsTransientState.openXpPicker(state, questId, id, false);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp reward picker open quest={} reward={} add={}", questId, id, add);
            return;
        }
        EditorCommandClient.putQuestRewardJson(player, questId, QuestObjectiveJsons.defaultReward(id, typePath).toString());
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details {} reward quest={} reward={} type={}", add ? "add" : "change", questId, id, typePath);
    }

    public static String objectiveIcon(String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = QuestObjectiveJsons.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        return task ? QuestObjectiveDisplayText.taskIcon(json) : QuestObjectiveDisplayText.rewardIcon(json);
    }

    private static String blockIcon(String target) {
        String clean = target == null ? "" : target.trim();
        return clean.isBlank() ? "box" : clean;
    }

    private static String statIcon(String target) {
        String clean = target == null ? "" : target.trim();
        int split = clean.indexOf(':');
        if (split <= 0) {
            return "stat";
        }
        String category = clean.substring(0, split);
        String value = clean.substring(split + 1);
        if ("mined".equals(category)) {
            return blockItemIcon(value);
        }
        if ("crafted".equals(category) || "used".equals(category) || "broken".equals(category)
                || "picked_up".equals(category) || "dropped".equals(category)) {
            return itemIcon(value);
        }
        if ("killed".equals(category) || "killed_by".equals(category)) {
            return EntityPreviewRenderer.entityAsset(value);
        }
        return "stat";
    }

    private static String recipeIcon(String target) {
        if (target != null && target.trim().startsWith("#")) {
            return target.trim();
        }
        String icon = itemIcon(target);
        return "minecraft:paper".equals(icon) ? "recipe" : icon;
    }

    private static String blockItemIcon(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "minecraft:paper";
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        if (!id.equals(BuiltInRegistries.BLOCK.getKey(block))) {
            return "minecraft:paper";
        }
        Item item = block.asItem();
        if (item == Items.AIR) {
            return "minecraft:paper";
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return itemId == null ? "minecraft:paper" : itemId.toString();
    }

    private static String itemIcon(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return "minecraft:paper";
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR && !"minecraft:air".equals(value) ? "minecraft:paper" : id.toString();
    }

    public static boolean isEntityObjectiveIcon(String questId, String id, boolean task) {
        return EntityPreviewRenderer.isEntityAsset(objectiveIcon(questId, id, task));
    }

    public static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task) {
        putObjectiveIcon(player, questId, id, icon, task, true);
    }

    public static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task, boolean sync) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = QuestObjectiveJsons.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        json.addProperty("icon", icon.startsWith("#") ? icon.substring(1) : icon);
        if (task) {
            ClientQuestCache.putQuestTaskJsonLocal(questId, json.toString());
        } else {
            ClientQuestCache.putQuestRewardJsonLocal(questId, json.toString());
        }
        if (!sync) {
            return;
        }
        if (task) {
            EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
        } else {
            EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
        }
    }
}
