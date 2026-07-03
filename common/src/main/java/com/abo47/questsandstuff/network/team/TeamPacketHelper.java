package com.abo47.questsandstuff.network.team;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.team.model.TeamMember;
import com.abo47.questsandstuff.team.model.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TeamPacketHelper {
    private static final String TAG_TEAM_ID = "team_id";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_MEMBERS = "members";
    private static final String TAG_UUID = "uuid";
    private static final String TAG_NAME = "name";
    private static final String TAG_ROLE = "role";
    private static final String TAG_INVITE_CODE = "invite_code";
    private static final String TAG_INVITE_EXPIRY = "invite_expiry";

    private TeamPacketHelper() {
    }

    public static void send(ServerPlayer player, TeamData team) {
        CompoundTag tag = new CompoundTag();
        if (team == null) {
            tag.putBoolean("empty", true);
        } else {
            tag.putUUID(TAG_TEAM_ID, team.teamId());
            tag.putUUID(TAG_OWNER, team.owner());
            tag.putString(TAG_INVITE_CODE, team.inviteCode());
            tag.putLong(TAG_INVITE_EXPIRY, team.inviteExpiryMs());
            ListTag membersList = new ListTag();
            for (TeamMember m : team.members()) {
                CompoundTag memberTag = new CompoundTag();
                memberTag.putUUID(TAG_UUID, m.uuid());
                memberTag.putString(TAG_NAME, m.name());
                memberTag.putString(TAG_ROLE, m.role().name());
                membersList.add(memberTag);
            }
            tag.put(TAG_MEMBERS, membersList);
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
        if (tag == null || tag.getBoolean("empty")) {
            return null;
        }
        UUID teamId = tag.getUUID(TAG_TEAM_ID);
        UUID owner = tag.getUUID(TAG_OWNER);
        String inviteCode = tag.getString(TAG_INVITE_CODE);
        long inviteExpiry = tag.getLong(TAG_INVITE_EXPIRY);
        ListTag membersList = tag.getList(TAG_MEMBERS, Tag.TAG_COMPOUND);
        List<TeamMember> members = new ArrayList<>();
        for (int i = 0; i < membersList.size(); i++) {
            CompoundTag memberTag = membersList.getCompound(i);
            UUID uuid = memberTag.getUUID(TAG_UUID);
            String name = memberTag.getString(TAG_NAME);
            String roleStr = memberTag.getString(TAG_ROLE);
            TeamMember.Role role = "OWNER".equals(roleStr) ? TeamMember.Role.OWNER : TeamMember.Role.MEMBER;
            members.add(new TeamMember(uuid, name, role));
        }
        return new TeamData(teamId, owner, members, inviteCode, inviteExpiry);
    }
}
