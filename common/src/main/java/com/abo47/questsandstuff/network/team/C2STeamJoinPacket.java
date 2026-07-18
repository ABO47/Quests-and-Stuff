package com.abo47.questsandstuff.network.team;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.TeamManager;
import com.abo47.questsandstuff.team.model.TeamData;

public record C2STeamJoinPacket(String inviteCode) {
    public static C2STeamJoinPacket decode(FriendlyByteBuf buf) {
        return new C2STeamJoinPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(inviteCode == null ? "" : inviteCode);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null || inviteCode == null || inviteCode.isBlank()) {
            return;
        }
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            TeamManager manager = new TeamManager(level, QuestServiceRegistry.engine(player.server));
            String code = inviteCode.trim().toUpperCase();
            String error = manager.getJoinError(player, code);
            if (error != null) {
                TeamData existing = manager.getTeamByPlayer(player.getUUID());
                if (existing != null) {
                    TeamPacketHelper.send(player, existing);
                }
                S2CTeamJoinResultPacket.send(player, "ui.questsandstuff.teams.join_error." + error, false);
                return;
            }
            TeamData team = manager.joinTeam(player, code);
            if (team == null) {
                S2CTeamJoinResultPacket.send(player, "ui.questsandstuff.teams.invite_invalid", false);
                return;
            }
            TeamPacketHelper.broadcastToMembers(level, team);
            S2CTeamJoinResultPacket.send(player, "", true);
        });
    }
}
