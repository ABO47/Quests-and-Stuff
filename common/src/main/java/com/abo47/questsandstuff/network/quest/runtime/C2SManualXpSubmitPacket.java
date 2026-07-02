package com.abo47.questsandstuff.network.quest.runtime;

import com.abo47.questsandstuff.network.ModPacketContext;

import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SManualXpSubmitPacket(String questId, String taskId) {
    public static C2SManualXpSubmitPacket decode(FriendlyByteBuf buf) {
        return new C2SManualXpSubmitPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeUtf(taskId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server).submitManualXpTask(player, questId, taskId));
        }
    }
}
