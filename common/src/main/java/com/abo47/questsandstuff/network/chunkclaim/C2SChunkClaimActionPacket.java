package com.abo47.questsandstuff.network.chunkclaim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.team.TeamPacketHelper;

public record C2SChunkClaimActionPacket(Action action, ResourceLocation dimension, int x, int z) {
    public enum Action {
        CLAIM,
        UNCLAIM,
        TOGGLE_FORCE,
        REQUEST
    }

    public static C2SChunkClaimActionPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        ResourceLocation dimension = buf.readResourceLocation();
        int x = buf.readInt();
        int z = buf.readInt();
        return new C2SChunkClaimActionPacket(action, dimension, x, z);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeResourceLocation(dimension);
        buf.writeInt(x);
        buf.writeInt(z);
    }

    public void handle(ModPacketContext context) {
        TeamPacketHelper.onServer(context, (player, manager) ->
                ChunkClaimPacketHelper.applyAction(player, manager, action, dimension, x, z));
    }
}
