package com.abo47.questsandstuff.quest.model.reward;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestRewards {
    private static final Map<ResourceLocation, QuestRewardType<? extends QuestRewardDefinition>> TYPES = new LinkedHashMap<>();

    public static final Codec<QuestRewardDefinition> CODEC = ResourceLocation.CODEC.dispatch(
            "type",
            QuestRewardDefinition::type,
            type -> TYPES.getOrDefault(type, unsupported(type)).codec()
    );

    public static final Codec<Map<String, QuestRewardDefinition>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

    private QuestRewards() {
    }

    static {
        bootstrapDefaults();
    }

    public static void bootstrapDefaults() {
        if (!TYPES.isEmpty()) {
            return;
        }
        register(new QuestRewardType<>(id("item"), ItemQuestRewardDefinition.codec(id("item"))));
        register(new QuestRewardType<>(id("xp"), XpQuestRewardDefinition.codec(id("xp"))));
        register(new QuestRewardType<>(id("loot_table"), LootTableQuestRewardDefinition.codec(id("loot_table"))));
        register(new QuestRewardType<>(id("loot"), LootTableQuestRewardDefinition.codec(id("loot"))));
        register(new QuestRewardType<>(id("command"), CommandQuestRewardDefinition.codec(id("command"))));
        register(new QuestRewardType<>(id("selectable"), SelectableQuestRewardDefinition.codec(id("selectable"))));
    }

    public static void register(QuestRewardType<? extends QuestRewardDefinition> type) {
        TYPES.put(type.id(), type);
    }

    public static QuestRewardType<? extends QuestRewardDefinition> get(ResourceLocation id) {
        return TYPES.get(id);
    }

    public static Map<ResourceLocation, QuestRewardType<? extends QuestRewardDefinition>> allTypes() {
        bootstrapDefaults();
        return Map.copyOf(TYPES);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, path);
    }

    private static QuestRewardType<UnsupportedQuestRewardDefinition> unsupported(ResourceLocation id) {
        return new QuestRewardType<>(id, UnsupportedQuestRewardDefinition.codec(id));
    }
}
