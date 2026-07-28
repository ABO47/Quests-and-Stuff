package com.abo47.questsandstuff.network.team;

import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.network.PacketBufHelper;

public record C2STeamJoinPacket(String inviteCode) {
    public static C2STeamJoinPacket decode(FriendlyByteBuf buf) {
        return new C2STeamJoinPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        PacketBufHelper.writeUtfSafe(buf, inviteCode);
    }

    public void handle(ModPacketContext context) {
        if (inviteCode == null || inviteCode.isBlank()) {
            return;
        }
        TeamPacketHelper.onServer(context, (player, manager) -> {
            String code = inviteCode.trim().toUpperCase();
            var level = player.serverLevel();
            String error = manager.getJoinError(player, code);
            if (error != null) {
                var existing = manager.getTeamByPlayer(player.getUUID());
                if (existing != null) {
                    TeamPacketHelper.send(player, existing);
                }
                S2CTeamJoinResultPacket.send(player, "ui.questsandstuff.teams.join_error." + error, false);
                return;
            }
            var team = manager.joinTeam(player, code);
            if (team == null) {
                S2CTeamJoinResultPacket.send(player, "ui.questsandstuff.teams.invite_invalid", false);
                return;
            }
            TeamPacketHelper.broadcastToMembers(level, team);
            S2CTeamJoinResultPacket.send(player, "", true);
        });
    }
}
