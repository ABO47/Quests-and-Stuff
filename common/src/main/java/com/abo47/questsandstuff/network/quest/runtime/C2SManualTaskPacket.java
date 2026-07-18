package com.abo47.questsandstuff.network.quest.runtime;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SManualTaskPacket(String questId, String taskId) {
    public static C2SManualTaskPacket decode(FriendlyByteBuf buf) {
        return new C2SManualTaskPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(taskId == null ? "" : taskId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server)
                    .submitManualCheckTask(player, questId, taskId));
        }
    }
}
