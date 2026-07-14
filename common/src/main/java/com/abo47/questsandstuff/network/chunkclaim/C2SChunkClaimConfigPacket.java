package com.abo47.questsandstuff.network.chunkclaim;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.QuestsAndStuffConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record C2SChunkClaimConfigPacket(
        boolean protectBreakPlace,
        boolean protectInteraction,
        boolean protectExplosions,
        boolean protectMobGriefing,
        boolean protectPvp,
        int maxClaimedChunks,
        int maxForceLoadedChunks
) {
    public static C2SChunkClaimConfigPacket decode(FriendlyByteBuf buf) {
        return new C2SChunkClaimConfigPacket(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(protectBreakPlace);
        buf.writeBoolean(protectInteraction);
        buf.writeBoolean(protectExplosions);
        buf.writeBoolean(protectMobGriefing);
        buf.writeBoolean(protectPvp);
        buf.writeInt(maxClaimedChunks);
        buf.writeInt(maxForceLoadedChunks);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null || !player.hasPermissions(2)) {
            return;
        }
        QuestsAndStuffConfig.updateChunkClaims(
                protectBreakPlace,
                protectInteraction,
                protectExplosions,
                protectMobGriefing,
                protectPvp,
                maxClaimedChunks,
                maxForceLoadedChunks
        );
    }
}
