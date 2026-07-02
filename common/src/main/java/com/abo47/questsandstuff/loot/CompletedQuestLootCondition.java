package com.abo47.questsandstuff.loot;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record CompletedQuestLootCondition(String questId) implements LootItemCondition {
    public static final Serializer<CompletedQuestLootCondition> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(JsonObject json, CompletedQuestLootCondition value, JsonSerializationContext context) {
            json.addProperty("quest", value.questId);
        }

        @Override
        public CompletedQuestLootCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new CompletedQuestLootCondition(GsonHelper.getAsString(json, "quest"));
        }
    };

    @Override
    public LootItemConditionType getType() {
        return QuestsAndStuffMod.COMPLETED_QUEST_LOOT_CONDITION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (!(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)) {
            return false;
        }
        try {
            return QuestServiceRegistry.engine(player.server).isQuestCompleted(player.getUUID(), questId);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static LootItemCondition.Builder builder(String questId) {
        return () -> new CompletedQuestLootCondition(questId);
    }

    public static ResourceLocation id() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "completed_quest");
    }
}
