package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

import net.minecraft.network.FriendlyByteBuf;


public record S2CQuestEventPacket(long sequence, String eventType, String questId, String rewardId) {
    public static S2CQuestEventPacket decode(FriendlyByteBuf buf) {
        return new S2CQuestEventPacket(buf.readLong(), buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeUtf(eventType == null ? "" : eventType);
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(rewardId == null ? "" : rewardId);
    }

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleQuestEvent(sequence, eventType, questId, rewardId));
    }
}
