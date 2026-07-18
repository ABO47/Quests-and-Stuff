package com.abo47.questsandstuff.network.team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.team.NbtKeys;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;

public final class TeamPacketHelper {
    private TeamPacketHelper() {
    }

    public static void send(ServerPlayer player, TeamData team) {
        CompoundTag tag = new CompoundTag();
        if (team == null) {
            tag.putBoolean(NbtKeys.EMPTY, true);
        } else {
            tag.putUUID(NbtKeys.TEAM_ID, team.teamId());
            tag.putUUID(NbtKeys.OWNER, team.owner());
            tag.putString(NbtKeys.INVITE_CODE, team.inviteCode());
            tag.putLong(NbtKeys.INVITE_EXPIRY, team.inviteExpiryMs());
            ListTag membersList = new ListTag();
            for (TeamMember m : team.members()) {
                CompoundTag memberTag = new CompoundTag();
                memberTag.putUUID(NbtKeys.UUID, m.uuid());
                memberTag.putString(NbtKeys.NAME, m.name());
                memberTag.putString(NbtKeys.ROLE, m.role().name());
                membersList.add(memberTag);
            }
            tag.put(NbtKeys.MEMBERS, membersList);
        }
        ModNetwork.sendToPlayer(new S2CTeamSyncPacket(tag), player);
    }

    public static void broadcastToMembers(ServerLevel level, TeamData team) {
        if (team == null) return;
        for (TeamMember m : team.members()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(m.uuid());
            if (member != null) {
                send(member, team);
            }
        }
    }

    public static void clearPlayer(ServerLevel level, UUID playerUuid) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            send(player, null);
        }
    }

    public static TeamData fromPayload(CompoundTag tag) {
        if (tag == null || tag.getBoolean(NbtKeys.EMPTY)) {
            return null;
        }
        UUID teamId = tag.getUUID(NbtKeys.TEAM_ID);
        UUID owner = tag.getUUID(NbtKeys.OWNER);
        String inviteCode = tag.getString(NbtKeys.INVITE_CODE);
        long inviteExpiry = tag.getLong(NbtKeys.INVITE_EXPIRY);
        ListTag membersList = tag.getList(NbtKeys.MEMBERS, Tag.TAG_COMPOUND);
        List<TeamMember> members = new ArrayList<>();
        for (int i = 0; i < membersList.size(); i++) {
            CompoundTag memberTag = membersList.getCompound(i);
            UUID uuid = memberTag.getUUID(NbtKeys.UUID);
            String name = memberTag.getString(NbtKeys.NAME);
            String roleStr = memberTag.getString(NbtKeys.ROLE);
            TeamMember.Role role = "OWNER".equals(roleStr) ? TeamMember.Role.OWNER : TeamMember.Role.MEMBER;
            members.add(new TeamMember(uuid, name, role));
        }
        return new TeamData(teamId, owner, members, inviteCode, inviteExpiry);
    }
}
