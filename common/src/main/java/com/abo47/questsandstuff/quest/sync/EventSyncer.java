package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CQuestEventPacket;
import net.minecraft.server.level.ServerPlayer;

final class EventSyncer {
    void send(ServerPlayer player, long sequence, String eventType, String questId, String rewardId) {
        ModNetwork.sendToPlayer(new S2CQuestEventPacket(sequence, eventType, questId, rewardId), player);
    }
}
