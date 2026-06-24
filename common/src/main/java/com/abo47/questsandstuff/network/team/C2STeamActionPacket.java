package com.abo47.questsandstuff.network.team;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.model.team.TeamData;
import com.abo47.questsandstuff.quest.model.team.TeamMember;
import com.abo47.questsandstuff.quest.team.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record C2STeamActionPacket(Action action, UUID targetUuid) {
    public enum Action {
        LEAVE,
        KICK,
        TRANSFER
    }

    public static C2STeamActionPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        UUID targetUuid = buf.readUUID();
        return new C2STeamActionPacket(action, targetUuid);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUUID(targetUuid);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null) {
            return;
        }
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            TeamManager manager = new TeamManager(level, QuestServices.engine(player.server));
            TeamData before = manager.getTeamByPlayer(player.getUUID());
            boolean success = false;
            switch (action) {
                case LEAVE -> success = manager.leaveTeam(player);
                case KICK -> success = manager.kickMember(player, targetUuid);
                case TRANSFER -> success = manager.transferOwnership(player, targetUuid);
            }
            if (success) {
                TeamData after = manager.getTeamByPlayer(player.getUUID());
                if (after != null) {
                    S2CTeamSyncPacket.broadcastToMembers(level, after);
                }
                if (before != null) {
                    for (TeamMember m : before.members()) {
                        if (after == null || !after.isMember(m.uuid())) {
                            S2CTeamSyncPacket.clearPlayer(level, m.uuid());
                        }
                    }
                }
                if (after == null && before != null) {
                    TeamData remaining = manager.getTeamById(before.teamId());
                    if (remaining != null) {
                        S2CTeamSyncPacket.broadcastToMembers(level, remaining);
                    }
                }
            }
        });
    }
}
