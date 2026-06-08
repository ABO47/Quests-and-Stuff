package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.client.sync.cache.ClientCanvasLayerState;
import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public final class ClientSyncPayloadApplier {
    private ClientSyncPayloadApplier() {
    }

    public static void applyFullSync(CompoundTag payload) {
        ClientQuestState.clearQuests();
        ClientRawSyncPayload.replace(payload);
        ClientChapterState.loadFromFullPayload(payload);
        ClientCanvasLayerState.loadFromFullPayload(payload);

        CompoundTag questsTag = payload.getCompound(QuestSyncKeys.QUESTS);
        for (String questId : questsTag.getAllKeys()) {
            ClientQuestState.putQuest(questId, questsTag.getCompound(questId));
        }
        TabletUiFactory.syncActiveCanvasStateFromCache();
    }

    public static void applyDeltaSync(CompoundTag payload) {
        boolean chapterPayload = payload.contains(QuestSyncKeys.GROUPS, Tag.TAG_LIST) || payload.contains(QuestSyncKeys.GROUP_PROPS, Tag.TAG_COMPOUND);
        if (chapterPayload) {
            ClientChapterState.mergeFromDeltaPayload(payload);
            ClientCanvasLayerState.mergeFromDeltaPayload(payload);
        }
        CompoundTag changed = payload.getCompound(QuestSyncKeys.CHANGED);
        for (String questId : changed.getAllKeys()) {
            ClientQuestState.putQuest(questId, changed.getCompound(questId));
        }
        CompoundTag removed = payload.getCompound(QuestSyncKeys.REMOVED);
        for (String questId : removed.getAllKeys()) {
            ClientQuestState.removeQuest(questId);
        }
        ClientRawSyncPayload.merge(payload);
        if (chapterPayload) {
            TabletUiFactory.syncActiveCanvasStateFromCache();
        }
    }

    public static void applyDescriptionSync(CompoundTag payload) {
        CompoundTag descriptions = payload.getCompound(QuestSyncKeys.DESCRIPTIONS);
        for (String questId : descriptions.getAllKeys()) {
            CompoundTag quest = ClientQuestState.mutableQuestOrCreate(questId);
            ListTag lines = descriptions.getList(questId, Tag.TAG_STRING);
            quest.put(QuestSyncKeys.Quest.DESCRIPTION, lines.copy());
        }
    }

    public static void applyPinnedSync(List<String> pinnedList) {
        ClientQuestState.setPinned(pinnedList);
    }
}
