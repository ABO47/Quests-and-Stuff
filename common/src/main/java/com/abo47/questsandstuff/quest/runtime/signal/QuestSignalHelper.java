package com.abo47.questsandstuff.quest.runtime.signal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public final class QuestSignalHelper {
    private QuestSignalHelper() {
    }

    public static void send(ServerPlayer player, QuestSignalType type, String key, int amount) {
        QuestServiceRegistry.engine(player.server).onSignal(QuestSignal.of(type, player, key, amount, player.blockPosition()));
    }

    public static void tick(ServerPlayer player, Map<UUID, Map<String, Integer>> inventorySnapshots, Map<UUID, Map<String, Integer>> statSnapshots) {
        send(player, QuestSignalType.LOCATION_TICK, "", 1);
        send(player, QuestSignalType.XP_SNAPSHOT, "points", Math.max(0, player.totalExperience));
        send(player, QuestSignalType.XP_SNAPSHOT, "level", Math.max(0, player.experienceLevel));

        ResourceLocation biomeId = player.serverLevel().getBiome(player.blockPosition())
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(ResourceLocation.tryBuild("minecraft", "plains"));
        send(player, QuestSignalType.BIOME_ENTER, biomeId.toString(), 1);

        Map<Structure, LongSet> structures = player.serverLevel().structureManager().getAllStructuresAt(player.blockPosition());
        if (!structures.isEmpty()) {
            for (Structure structure : structures.keySet()) {
                ResourceLocation structureId = player.server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(structure);
                if (structureId != null) {
                    send(player, QuestSignalType.STRUCTURE_ENTER, structureId.toString(), 1);
                }
            }
        }

        pushInventorySnapshotDelta(player, inventorySnapshots);
        pushStatSnapshotDelta(player, statSnapshots);
    }

    public static void pushInventorySnapshotDelta(ServerPlayer player, Map<UUID, Map<String, Integer>> snapshots) {
        Map<String, Integer> current = new HashMap<>();
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            current.merge(id.toString(), stack.getCount(), Integer::sum);
        }

        Map<String, Integer> previous = snapshots.getOrDefault(player.getUUID(), Map.of());
        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            int prev = previous.getOrDefault(entry.getKey(), 0);
            int delta = entry.getValue() - prev;
            if (delta > 0) {
                send(player, QuestSignalType.INVENTORY_CHANGED, entry.getKey(), delta);
            }
        }

        snapshots.put(player.getUUID(), current);
    }

    public static void pushStatSnapshotDelta(ServerPlayer player, Map<UUID, Map<String, Integer>> snapshots) {
        var engine = QuestServiceRegistry.engine(player.server);
        Map<String, Integer> current = new HashMap<>();
        for (String statKey : engine.trackedStatTaskTargets()) {
            current.put(statKey, QuestStatHelper.readStat(player, statKey));
        }

        Map<String, Integer> previous = snapshots.getOrDefault(player.getUUID(), Map.of());
        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            int prev = previous.getOrDefault(entry.getKey(), Integer.MIN_VALUE);
            if (entry.getValue() != prev) {
                send(player, QuestSignalType.STAT_CHANGE, entry.getKey(), Math.max(0, entry.getValue()));
            }
        }

        snapshots.put(player.getUUID(), current);
    }
}
