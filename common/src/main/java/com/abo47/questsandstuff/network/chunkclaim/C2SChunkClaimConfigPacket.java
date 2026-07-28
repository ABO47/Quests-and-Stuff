package com.abo47.questsandstuff.network.chunkclaim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.network.ModPacketContext;

public record C2SChunkClaimConfigPacket(
        boolean protectBreakPlace,
        boolean protectInteraction,
        boolean protectExplosions,
        boolean protectMobGriefing,
        boolean protectPvp,
        boolean protectFire,
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
        buf.writeBoolean(protectFire);
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
                protectFire,
                maxClaimedChunks,
                maxForceLoadedChunks
        );
    }
}
