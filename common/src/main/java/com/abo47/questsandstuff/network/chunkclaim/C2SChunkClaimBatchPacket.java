package com.abo47.questsandstuff.network.chunkclaim;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.team.TeamPacketHelper;

public record C2SChunkClaimBatchPacket(ResourceLocation dimension, List<Entry> entries) {
    public static final int MAX_ENTRIES = 1024;

    public record Entry(C2SChunkClaimActionPacket.Action action, int x, int z) {
    }

    public static C2SChunkClaimBatchPacket decode(FriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES) {
            throw new IllegalArgumentException("Invalid chunk claim batch size " + count);
        }
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readEnum(C2SChunkClaimActionPacket.Action.class), buf.readInt(), buf.readInt()));
        }
        return new C2SChunkClaimBatchPacket(dimension, entries);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            buf.writeEnum(entry.action());
            buf.writeInt(entry.x());
            buf.writeInt(entry.z());
        }
    }

    public void handle(ModPacketContext context) {
        TeamPacketHelper.onServer(context, (player, manager) ->
                ChunkClaimPacketHelper.applyBatch(player, manager, dimension, entries));
    }
}
