package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public record C2SManualTaskPacket(String questId, String taskId) {
    public static C2SManualTaskPacket decode(FriendlyByteBuf buf) {
        return new C2SManualTaskPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(taskId == null ? "" : taskId);
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server)
                    .submitManualCheckTask(player, questId, taskId));
        }
    }
}
