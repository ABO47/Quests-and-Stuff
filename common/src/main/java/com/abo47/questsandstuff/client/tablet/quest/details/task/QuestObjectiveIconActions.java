package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

final class QuestObjectiveIconActions {
    private QuestObjectiveIconActions() {
    }

    static String objectiveIcon(String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = TaskJsonFactory.read(entry.getString("json"));
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        return task ? QuestObjectiveDisplayText.taskIcon(json) : QuestObjectiveDisplayText.rewardIcon(json);
    }

    static boolean isEntityObjectiveIcon(String questId, String id, boolean task) {
        return EntityPreviewRenderer.isEntityAsset(objectiveIcon(questId, id, task));
    }

    static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task) {
        putObjectiveIcon(player, questId, id, icon, task, true);
    }

    static void putObjectiveIcon(Player player, String questId, String id, String icon, boolean task, boolean sync) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = TaskJsonFactory.readForEdit(questId, id, task, entry.getString("json")).value();
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        json.addProperty("icon", icon.startsWith("#") ? icon.substring(1) : icon);
        if (task) {
            ClientQuestStateFacade.putQuestTaskJsonLocal(questId, json.toString());
        } else {
            ClientQuestStateFacade.putQuestRewardJsonLocal(questId, json.toString());
        }
        if (!sync) {
            return;
        }
        if (task) {
            EditorQuestCommandClient.putQuestTaskJson(player, questId, json.toString());
        } else {
            EditorQuestCommandClient.putQuestRewardJson(player, questId, json.toString());
        }
    }

    static String blockIcon(String target) {
        String clean = target == null ? "" : target.trim();
        return clean.isBlank() ? "box" : clean;
    }

    static String statIcon(String target) {
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

    static String recipeIcon(String target) {
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
}
