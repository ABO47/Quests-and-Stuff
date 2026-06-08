package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CDisplayCacheSyncPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.LinkedHashMap;
import java.util.Map;

final class QuestDisplayCacheSyncer {
    void sync(ServerPlayer player, long sequence) {
        ModNetwork.sendToPlayer(new S2CDisplayCacheSyncPacket(sequence, payload(player)), player);
    }

    CompoundTag payload(ServerPlayer player) {
        Map<String, String> advancements = new LinkedHashMap<>();
        for (var advancement : player.server.getAdvancements().getAllAdvancements()) {
            String id = advancement.getId().toString();
            String title = advancement.getDisplay() == null
                    ? id
                    : advancement.getDisplay().getTitle().getString();
            advancements.put(id, title);
        }

        Map<String, String> lootTables = new LinkedHashMap<>();
        for (var key : player.server.getLootData().getKeys(LootDataType.TABLE)) {
            lootTables.put(key.toString(), key.getPath());
        }

        Map<String, String> biomes = new LinkedHashMap<>();
        var biomeRegistry = player.server.registryAccess().registryOrThrow(Registries.BIOME);
        for (var biomeKey : biomeRegistry.registryKeySet()) {
            biomes.put(biomeKey.location().toString(), biomeKey.location().getPath());
        }

        return payload(advancements, lootTables, biomes);
    }

    static CompoundTag payload(Map<String, String> advancements, Map<String, String> lootTables, Map<String, String> biomes) {
        CompoundTag payload = new CompoundTag();
        payload.put(QuestSyncKeys.DisplayCache.ADVANCEMENTS, stringMapTag(advancements));
        payload.put(QuestSyncKeys.DisplayCache.LOOT_TABLES, stringMapTag(lootTables));
        payload.put(QuestSyncKeys.DisplayCache.BIOMES, stringMapTag(biomes));
        return payload;
    }

    private static CompoundTag stringMapTag(Map<String, String> values) {
        CompoundTag tag = new CompoundTag();
        if (values == null || values.isEmpty()) {
            return tag;
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                tag.putString(entry.getKey(), entry.getValue());
            }
        }
        return tag;
    }
}
