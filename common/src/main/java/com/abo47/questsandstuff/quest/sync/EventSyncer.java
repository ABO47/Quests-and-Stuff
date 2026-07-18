package com.abo47.questsandstuff.quest.sync;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CQuestEventPacket;

final class EventSyncer {
    void send(ServerPlayer player, long sequence, String eventType, String questId, String rewardId) {
        ModNetwork.sendToPlayer(new S2CQuestEventPacket(sequence, eventType, questId, rewardId), player);
    }
}
