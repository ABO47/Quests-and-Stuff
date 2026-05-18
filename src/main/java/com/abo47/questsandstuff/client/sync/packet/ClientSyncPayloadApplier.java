package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;

import com.abo47.questsandstuff.client.sync.cache.ClientCanvasLayerState;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public final class ClientSyncPayloadApplier {
    private ClientSyncPayloadApplier() {
    }

    public static void applyFullSync(CompoundTag payload) {
        ClientQuestState.QUESTS.clear();
        ClientRawSyncPayload.replace(payload);
        ClientChapterState.loadFromFullPayload(payload);
        ClientCanvasLayerState.loadFromFullPayload(payload);

        CompoundTag questsTag = payload.getCompound("quests");
        for (String questId : questsTag.getAllKeys()) {
            ClientQuestState.QUESTS.put(questId, questsTag.getCompound(questId).copy());
        }
        TabletUiFactory.syncActiveCanvasStateFromCache();
    }

    public static void applyDeltaSync(CompoundTag payload) {
        CompoundTag changed = payload.getCompound("changed");
        for (String questId : changed.getAllKeys()) {
            ClientQuestState.QUESTS.put(questId, changed.getCompound(questId).copy());
        }
        CompoundTag removed = payload.getCompound("removed");
        for (String questId : removed.getAllKeys()) {
            ClientQuestState.QUESTS.remove(questId);
        }
        ClientRawSyncPayload.merge(payload);
    }

    public static void applyDescriptionSync(CompoundTag payload) {
        CompoundTag descriptions = payload.getCompound("descriptions");
        for (String questId : descriptions.getAllKeys()) {
            CompoundTag quest = ClientQuestState.QUESTS.computeIfAbsent(questId, ignored -> new CompoundTag());
            ListTag lines = descriptions.getList(questId, Tag.TAG_STRING);
            quest.put("description", lines.copy());
        }
    }

    public static void applyPinnedSync(List<String> pinnedList) {
        ClientQuestState.PINNED.clear();
        for (String questId : pinnedList) {
            ClientQuestState.PINNED.add(questId);
        }
    }
}
