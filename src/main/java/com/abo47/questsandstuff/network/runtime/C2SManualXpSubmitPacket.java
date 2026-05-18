package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SManualXpSubmitPacket(String questId, String taskId) {
    public static C2SManualXpSubmitPacket decode(FriendlyByteBuf buf) {
        return new C2SManualXpSubmitPacket(buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeUtf(taskId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server).submitManualXpTask(player, questId, taskId));
        }
        context.setPacketHandled(true);
    }
}
