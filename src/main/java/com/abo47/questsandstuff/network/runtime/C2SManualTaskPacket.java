package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2SManualTaskPacket(String taskKey) {
    public static C2SManualTaskPacket decode(FriendlyByteBuf buf) {
        return new C2SManualTaskPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(taskKey);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server)
                    .onSignal(QuestSignal.of(QuestSignalType.MANUAL_CHECK, player, taskKey, 1, player.blockPosition())));
        }
        context.setPacketHandled(true);
    }
}
