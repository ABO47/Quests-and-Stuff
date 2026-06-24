package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.FluidIconCodec;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class QuestObjectivePickerApplyActions {
    private QuestObjectivePickerApplyActions() {
    }

    static void applyIconPick(Player player, TabletUiState state, String entry) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || entry == null || entry.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_icon", parsed, 4)) {
            return;
        }
        String questId = parsed.questId();
        String id = parsed.entryId();
        String type = parsed.type();
        if (parsed.isTaskItem()) {
            if (FluidIconCodec.isFluidIcon(entry)) {
                JsonObject json = fluidItemJson(id, type, entry);
                json.addProperty("collection", "automatic");
                EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details task fluid picked quest={} task={} fluid={}", questId, id, FluidIconCodec.fluidId(entry));
                state.questDetails.questDetailsPickTarget = "";
                return;
            }
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
            QuestObjectiveIconActions.putObjectiveIcon(player, questId, id, entry, true);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details task icon changed quest={} task={} icon={}", questId, id, entry);
        } else if (parsed.isRewardItem() && !entry.startsWith("#")) {
            if (FluidIconCodec.isFluidIcon(entry)) {
                JsonObject json = fluidItemJson(id, type, entry);
                QuestObjectiveRewardEditActions.preserveRewardSelectableFlag(questId, id, json);
                EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward fluid picked quest={} reward={} fluid={}", questId, id, FluidIconCodec.fluidId(entry));
                state.questDetails.questDetailsPickTarget = "";
                return;
            }
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("type", type);
            json.addProperty("item", entry);
            json.addProperty("amount", 1);
            json.addProperty("nbt", "");
            QuestObjectiveRewardEditActions.preserveRewardSelectableFlag(questId, id, json);
            EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward item picked quest={} reward={} item={}", questId, id, entry);
        } else if (parsed.isRewardCommandEditorIcon()) {
            state.questDetails.questDetailsCommandRewardIcon = entry.startsWith("#") ? entry.substring(1) : entry;
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details command reward icon picked quest={} reward={} icon={}", questId, id, entry);
        } else if (parsed.isRewardIcon()) {
            QuestObjectiveIconActions.putObjectiveIcon(player, questId, id, entry, false);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward icon changed quest={} reward={} icon={}", questId, id, entry);
        }
        state.questDetails.questDetailsPickTarget = "";
    }

    static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || stack == null || stack.isEmpty()) {
            return;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String icon = ItemStackIconCodec.iconFromStack(stack);
        if (icon.isBlank()) {
            return;
        }
        if (parsed.isQuestIcon() && ModalTargetState.requireParts("inventory_quest_icon", parsed, 2)) {
            EditorCommandClient.runQuestIconAction(player, parsed.questId(), icon);
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest inventory icon picked quest={} item={} hasNbt={}", parsed.questId(), itemId, stack.hasTag());
            return;
        }
        if (parsed.isChapterIcon() && ModalTargetState.requireParts("inventory_chapter_icon", parsed, 2)) {
            EditorCommandClient.runGroupAction(player, state, "set_icon", parsed.questId(), icon, 0);
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter inventory icon picked chapter={} item={} hasNbt={}", parsed.questId(), itemId, stack.hasTag());
            return;
        }
        if (parsed.isTaskIcon() && ModalTargetState.requireParts("inventory_task_icon", parsed, 3)) {
            QuestObjectiveIconActions.putObjectiveIcon(player, parsed.questId(), parsed.entryId(), icon, true);
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details task inventory icon picked quest={} task={} item={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.hasTag());
            return;
        }
        if (parsed.isRewardIcon() && ModalTargetState.requireParts("inventory_reward_icon", parsed, 3)) {
            QuestObjectiveIconActions.putObjectiveIcon(player, parsed.questId(), parsed.entryId(), icon, false);
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details reward inventory icon picked quest={} reward={} item={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.hasTag());
            return;
        }
        if (parsed.isRewardCommandEditorIcon() && ModalTargetState.requireParts("inventory_command_reward_icon", parsed, 3)) {
            state.questDetails.questDetailsCommandRewardIcon = icon;
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details command reward inventory icon picked quest={} reward={} item={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.hasTag());
            return;
        }
        if (!ModalTargetState.requireParts("inventory_objective_item", parsed, 4)) {
            return;
        }
        if (parsed.isTaskInventoryItem()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", parsed.entryId());
            json.addProperty("type", parsed.type());
            json.addProperty("item", itemId);
            json.addProperty("amount", 1);
            json.addProperty("nbt", stack.hasTag() ? stack.getTag().toString() : "");
            json.addProperty("collection", "automatic");
            EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
            state.questDetails.questDetailsPickTarget = "";
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
            QuestObjectiveRewardEditActions.preserveRewardSelectableFlag(parsed.questId(), parsed.entryId(), json);
            EditorCommandClient.putQuestRewardJson(player, parsed.questId(), json.toString());
            state.questDetails.questDetailsPickTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details inventory reward item picked quest={} reward={} item={} amount={} hasNbt={}", parsed.questId(), parsed.entryId(), itemId, stack.getCount(), stack.hasTag());
        }
    }

    static void applyBiomePick(Player player, TabletUiState state, String biome) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || biome == null || biome.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_biome", parsed, 4) || !parsed.isTaskBiome()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), biome, "biome");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details biome picked quest={} task={} biome={} defaultIcon=biome", parsed.questId(), parsed.entryId(), biome);
    }

    static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || advancement == null || advancement.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_advancement", parsed, 4) || !parsed.isTaskAdvancement()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), advancement.trim(), "trophy");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details advancement picked quest={} task={} advancement={}", parsed.questId(), parsed.entryId(), advancement.trim());
    }

    static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || recipe == null || recipe.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_recipe", parsed, 4) || !parsed.isTaskRecipe()) {
            return;
        }
        String recipeTarget = recipe.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), recipeTarget, QuestObjectiveIconActions.recipeIcon(recipeTarget));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details recipe picked quest={} task={} recipe={}", parsed.questId(), parsed.entryId(), recipeTarget);
    }

    static void applyStructurePick(Player player, TabletUiState state, String structure) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || structure == null || structure.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_structure", parsed, 4) || !parsed.isTaskStructure()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), structure.trim(), "pyramid");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details structure picked quest={} task={} structure={}", parsed.questId(), parsed.entryId(), structure.trim());
    }

    static void applyBlockPick(Player player, TabletUiState state, String block) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || block == null || block.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_block", parsed, 4) || !parsed.isTaskBlock()) {
            return;
        }
        String blockId = block.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), blockId, QuestObjectiveIconActions.blockIcon(blockId));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details block picked quest={} task={} block={}", parsed.questId(), parsed.entryId(), blockId);
    }

    static void applyStatPick(Player player, TabletUiState state, String stat) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || stat == null || stat.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_stat", parsed, 4) || !parsed.isTaskStat()) {
            return;
        }
        String statTarget = stat.trim();
        JsonObject json = QuestObjectiveJsons.simpleTask(parsed.entryId(), parsed.type(), statTarget, QuestObjectiveIconActions.statIcon(statTarget));
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details stat picked quest={} task={} stat={}", parsed.questId(), parsed.entryId(), statTarget);
    }

    static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || dimension == null || dimension.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_dimension", parsed, 4) || !parsed.isTaskDimension()) {
            return;
        }
        JsonObject json = QuestObjectiveJsons.defaultTask(parsed.entryId(), "location");
        json.addProperty("type", parsed.type());
        json.addProperty("dimension", dimension.trim());
        json.addProperty("icon", "minecraft:compass");
        EditorCommandClient.putQuestTaskJson(player, parsed.questId(), json.toString());
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details dimension picked quest={} task={} dimension={}", parsed.questId(), parsed.entryId(), dimension.trim());
    }

    static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        ModalTargetParser.Target parsed = pickTarget(state);
        if (parsed.kind().isBlank() || lootTable == null || lootTable.isBlank()) {
            return;
        }
        if (!ModalTargetState.requireParts("objective_loot_table", parsed, 3) || !parsed.isRewardLootTable()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(parsed.questId());
        CompoundTag reward = quest.getCompound("rewards").getCompound(parsed.entryId());
        JsonObject existing = QuestObjectiveJsons.readRewardForEdit(parsed.questId(), parsed.entryId(), reward.getString("json"));
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
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details loot table picked quest={} reward={} lootTable={}", parsed.questId(), parsed.entryId(), lootTable.trim());
    }

    private static JsonObject fluidItemJson(String id, String type, String fluidIcon) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type);
        json.addProperty("item", "minecraft:air");
        json.addProperty("amount", 1);
        json.addProperty("nbt", "");
        json.addProperty("icon", fluidIcon);
        json.addProperty("title", FluidIconCodec.displayName(fluidIcon));
        return json;
    }

    private static ModalTargetParser.Target pickTarget(TabletUiState state) {
        return ModalTargetState.parsedTarget(state, ModalSession.TargetSlot.QUEST_DETAILS_PICK, state.questDetails.questDetailsPickTarget);
    }
}
