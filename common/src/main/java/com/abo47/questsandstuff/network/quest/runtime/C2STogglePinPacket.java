package com.abo47.questsandstuff.network.quest.runtime;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record C2STogglePinPacket(String questId) {
    public static C2STogglePinPacket decode(FriendlyByteBuf buf) {
        return new C2STogglePinPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId == null ? "" : questId);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        String normalizedQuestId = questId == null ? "" : questId.trim();
        if (player == null || normalizedQuestId.isBlank()) {
            return;
        }
        context.enqueueWork(() -> QuestServices.engine(player.server).togglePin(player, normalizedQuestId));
    }
}
