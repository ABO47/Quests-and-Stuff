package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record S2CPinnedSyncPacket(long sequence, List<String> pinned) {
    public static S2CPinnedSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int size = SyncPacketPayloadLimits.readPinnedQuestCount(buf);
        List<String> pinned = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pinned.add(buf.readUtf());
        }
        return new S2CPinnedSyncPacket(sequence, pinned);
    }

    public void encode(FriendlyByteBuf buf) {
        SyncPacketPayloadLimits.requirePinnedQuestCount(pinned.size());
        buf.writeLong(sequence);
        buf.writeVarInt(pinned.size());
        for (String id : pinned) {
            buf.writeUtf(id);
        }
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handlePinned(sequence, pinned));
    }
}
