package com.abo47.questsandstuff.network.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record S2CPinnedSyncPacket(long sequence, List<String> pinned) {
    public static S2CPinnedSyncPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        int size = buf.readVarInt();
        List<String> pinned = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            pinned.add(buf.readUtf());
        }
        return new S2CPinnedSyncPacket(sequence, pinned);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeVarInt(pinned.size());
        for (String id : pinned) {
            buf.writeUtf(id);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handlePinned(sequence, pinned));
        context.setPacketHandled(true);
    }
}
