package com.abo47.questsandstuff.quest.model.task;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.compat.oresandstuff.OresAndStuffCompat;
import com.abo47.questsandstuff.quest.model.task.fallback.UnsupportedQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.CheckQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.CompositeQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.generic.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.item.GatherItemQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.LocationQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.StatQuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.player.XpQuestTaskDefinition;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

public final class QuestTasks {
    private static final Map<ResourceLocation, QuestTaskType<? extends QuestTaskDefinition>> TYPES = new LinkedHashMap<>();

    public static final Codec<QuestTaskDefinition> CODEC = ResourceLocation.CODEC.dispatch(
            "type",
            QuestTaskDefinition::type,
            type -> TYPES.getOrDefault(type, unsupported(type)).codec()
    );

    private QuestTasks() {
    }

    static {
        bootstrapDefaults();
    }

    public static void bootstrapDefaults() {
        if (!TYPES.isEmpty()) {
            return;
        }
        register(new QuestTaskType<>(id("kill_entity"), SimpleQuestTaskDefinition.codec(id("kill_entity"), QuestSignalType.ENTITY_KILLED), "kill_entity_widget"));
        if (OresAndStuffCompat.isAvailable()) {
            register(new QuestTaskType<>(id("scan_entity"), SimpleQuestTaskDefinition.codec(id("scan_entity"), QuestSignalType.BIO_SCANNED), "scan_entity_widget"));
        }
        register(new QuestTaskType<>(id("item"), GatherItemQuestTaskDefinition.codec(id("item")), "item_widget"));
        register(new QuestTaskType<>(id("advancement"), SimpleQuestTaskDefinition.codec(id("advancement"), QuestSignalType.ADVANCEMENT), "advancement_widget"));
        register(new QuestTaskType<>(id("recipe"), SimpleQuestTaskDefinition.codec(id("recipe"), QuestSignalType.ITEM_CRAFTED), "recipe_widget"));
        register(new QuestTaskType<>(id("structure"), SimpleQuestTaskDefinition.codec(id("structure"), QuestSignalType.STRUCTURE_ENTER), "structure_widget"));
        register(new QuestTaskType<>(id("biome"), SimpleQuestTaskDefinition.codec(id("biome"), QuestSignalType.BIOME_ENTER), "biome_widget"));
        register(new QuestTaskType<>(id("block_interact"), SimpleQuestTaskDefinition.codec(id("block_interact"), QuestSignalType.BLOCK_INTERACT), "block_interact_widget"));
        register(new QuestTaskType<>(id("block_interaction"), SimpleQuestTaskDefinition.codec(id("block_interaction"), QuestSignalType.BLOCK_INTERACT), "block_interact_widget"));
        register(new QuestTaskType<>(id("entity_interact"), SimpleQuestTaskDefinition.codec(id("entity_interact"), QuestSignalType.ENTITY_INTERACT), "entity_interact_widget"));
        register(new QuestTaskType<>(id("entity_interaction"), SimpleQuestTaskDefinition.codec(id("entity_interaction"), QuestSignalType.ENTITY_INTERACT), "entity_interact_widget"));
        register(new QuestTaskType<>(id("item_interact"), SimpleQuestTaskDefinition.codec(id("item_interact"), QuestSignalType.ITEM_INTERACT), "item_interact_widget"));
        register(new QuestTaskType<>(id("item_interaction"), SimpleQuestTaskDefinition.codec(id("item_interaction"), QuestSignalType.ITEM_INTERACT), "item_interact_widget"));
        register(new QuestTaskType<>(id("item_use"), SimpleQuestTaskDefinition.codec(id("item_use"), QuestSignalType.ITEM_USED), "item_use_widget"));
        register(new QuestTaskType<>(id("changed_dimension"), SimpleQuestTaskDefinition.codec(id("changed_dimension"), QuestSignalType.DIMENSION_CHANGED), "changed_dimension_widget"));
        register(new QuestTaskType<>(id("check"), CheckQuestTaskDefinition.codec(id("check")), "check_widget"));
        register(new QuestTaskType<>(id("dummy"), CheckQuestTaskDefinition.codec(id("dummy")), "dummy_widget"));
        register(new QuestTaskType<>(id("xp"), XpQuestTaskDefinition.codec(id("xp")), "xp_widget"));
        register(new QuestTaskType<>(id("stat"), StatQuestTaskDefinition.codec(id("stat")), "stat_widget"));
        register(new QuestTaskType<>(id("location"), LocationQuestTaskDefinition.codec(id("location")), "location_widget"));
        register(new QuestTaskType<>(id("composite"), CompositeQuestTaskDefinition.codec(id("composite")), "composite_widget"));
    }

    public static void register(QuestTaskType<? extends QuestTaskDefinition> type) {
        TYPES.put(type.id(), type);
    }

    public static QuestTaskType<? extends QuestTaskDefinition> get(ResourceLocation id) {
        return TYPES.get(id);
    }

    public static Map<ResourceLocation, QuestTaskType<? extends QuestTaskDefinition>> allTypes() {
        bootstrapDefaults();
        return Map.copyOf(TYPES);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, path);
    }

    private static QuestTaskType<UnsupportedQuestTaskDefinition> unsupported(ResourceLocation id) {
        return new QuestTaskType<>(id, UnsupportedQuestTaskDefinition.codec(id), "unsupported_task_widget");
    }
}
