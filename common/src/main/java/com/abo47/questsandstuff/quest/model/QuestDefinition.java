package com.abo47.questsandstuff.quest.model;

import com.abo47.questsandstuff.quest.model.reward.QuestRewardDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record QuestDefinition(
        int schema,
        String id,
        QuestDisplay display,
        QuestSettings settings,
        Set<String> prerequisites,
        Map<String, Integer> connectionColors,
        Map<String, String> connectionModes,
        Set<String> hiddenConnections,
        Map<String, String> connectionTextures,
        Map<String, Integer> connectionTextureSpacings,
        List<String> tasksOrder,
        List<String> rewardsOrder,
        Map<String, QuestTaskDefinition> tasks,
        Map<String, QuestRewardDefinition> rewards
) {
    public static final int CURRENT_SCHEMA = 1;
    public static final String PREREQUISITES_FIELD = "prerequisites";

    public QuestDefinition {
        tasksOrder = normalizedOrder(tasksOrder, tasks == null ? Set.of() : tasks.keySet());
        rewardsOrder = normalizedOrder(rewardsOrder, rewards == null ? Set.of() : rewards.keySet());
        tasks = orderedCopy(tasks, tasksOrder);
        rewards = orderedCopy(rewards, rewardsOrder);
    }

    public QuestDefinition(
            int schema,
            String id,
            QuestDisplay display,
            QuestSettings settings,
            Set<String> prerequisites,
            Map<String, QuestTaskDefinition> tasks,
            Map<String, QuestRewardDefinition> rewards
    ) {
        this(schema, id, display, settings, prerequisites, Map.of(), Map.of(), Set.of(), Map.of(), Map.of(), List.of(), List.of(), tasks, rewards);
    }

    public QuestDefinition(
            int schema,
            String id,
            QuestDisplay display,
            QuestSettings settings,
            Set<String> prerequisites,
            Map<String, Integer> connectionColors,
            Map<String, QuestTaskDefinition> tasks,
            Map<String, QuestRewardDefinition> rewards
    ) {
        this(schema, id, display, settings, prerequisites, connectionColors, Map.of(), Set.of(), Map.of(), Map.of(), List.of(), List.of(), tasks, rewards);
    }

    public QuestDefinition(
            int schema,
            String id,
            QuestDisplay display,
            QuestSettings settings,
            Set<String> prerequisites,
            Map<String, Integer> connectionColors,
            Map<String, String> connectionModes,
            Set<String> hiddenConnections,
            Map<String, QuestTaskDefinition> tasks,
            Map<String, QuestRewardDefinition> rewards
    ) {
        this(schema, id, display, settings, prerequisites, connectionColors, connectionModes, hiddenConnections, Map.of(), Map.of(), List.of(), List.of(), tasks, rewards);
    }

    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema").orElse(CURRENT_SCHEMA).forGetter(QuestDefinition::schema),
            Codec.STRING.fieldOf("id").forGetter(QuestDefinition::id),
            QuestDisplay.CODEC.fieldOf("display").orElse(QuestDisplay.DEFAULT).forGetter(QuestDefinition::display),
            QuestSettings.CODEC.fieldOf("settings").orElse(QuestSettings.DEFAULT).forGetter(QuestDefinition::settings),
            Codec.STRING.listOf().xmap(Set::copyOf, set -> new ArrayList<>(set)).fieldOf(PREREQUISITES_FIELD).orElse(Set.of()).forGetter(QuestDefinition::prerequisites),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("connection_colors").orElse(Map.of()).forGetter(QuestDefinition::connectionColors),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("connection_modes").orElse(Map.of()).forGetter(QuestDefinition::connectionModes),
            Codec.STRING.listOf().xmap(Set::copyOf, set -> new ArrayList<>(set)).fieldOf("hidden_connections").orElse(Set.of()).forGetter(QuestDefinition::hiddenConnections),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("connection_textures").orElse(Map.of()).forGetter(QuestDefinition::connectionTextures),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("connection_texture_spacings").orElse(Map.of()).forGetter(QuestDefinition::connectionTextureSpacings),
            Codec.STRING.listOf().fieldOf("tasks_order").orElse(List.of()).forGetter(QuestDefinition::tasksOrder),
            Codec.STRING.listOf().fieldOf("rewards_order").orElse(List.of()).forGetter(QuestDefinition::rewardsOrder),
            Codec.unboundedMap(Codec.STRING, QuestTaskDefinition.CODEC).fieldOf("tasks").orElse(Map.of()).forGetter(QuestDefinition::tasks),
            Codec.unboundedMap(Codec.STRING, QuestRewardDefinition.CODEC).fieldOf("rewards").orElse(Map.of()).forGetter(QuestDefinition::rewards)
    ).apply(instance, QuestDefinition::new));

    private static List<String> normalizedOrder(List<String> source, Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> order = new ArrayList<>();
        Set<String> added = new HashSet<>();
        if (source != null) {
            for (String id : source) {
                if (id != null && keys.contains(id) && added.add(id)) {
                    order.add(id);
                }
            }
        }
        for (String key : keys) {
            if (key != null && added.add(key)) {
                order.add(key);
            }
        }
        return List.copyOf(order);
    }

    private static <T> Map<String, T> orderedCopy(Map<String, T> source, List<String> order) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, T> out = new LinkedHashMap<>();
        for (String key : order) {
            if (source.containsKey(key)) {
                out.put(key, source.get(key));
            }
        }
        for (Map.Entry<String, T> entry : source.entrySet()) {
            out.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return out;
    }
}
