package com.abo47.questsandstuff.network.team;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.team.model.TeamMember;

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
        TeamPacketHelper.onServer(context, (player, manager) -> {
            var level = player.serverLevel();
            var before = manager.getTeamByPlayer(player.getUUID());
            boolean success = false;
            switch (action) {
                case LEAVE -> success = manager.leaveTeam(player);
                case KICK -> success = manager.kickMember(player, targetUuid);
                case TRANSFER -> success = manager.transferOwnership(player, targetUuid);
            }
            if (success) {
                var after = manager.getTeamByPlayer(player.getUUID());
                if (after != null) {
                    TeamPacketHelper.broadcastToMembers(level, after);
                }
                if (before != null) {
                    for (TeamMember m : before.members()) {
                        if (after == null || !after.isMember(m.uuid())) {
                            TeamPacketHelper.clearPlayer(level, m.uuid());
                        }
                    }
                }
                if (after == null && before != null) {
                    var remaining = manager.getTeamById(before.teamId());
                    if (remaining != null) {
                        TeamPacketHelper.broadcastToMembers(level, remaining);
                    }
                }
            }
        });
    }
}
