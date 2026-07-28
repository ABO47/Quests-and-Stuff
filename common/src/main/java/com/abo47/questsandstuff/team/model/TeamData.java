package com.abo47.questsandstuff.team.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.team.NbtKeys;

public record TeamData(UUID teamId, UUID owner, List<TeamMember> members, String inviteCode, long inviteExpiryMs) {
    public TeamData {
        members = List.copyOf(members);
    }

    public List<TeamMember> members() {
        return Collections.unmodifiableList(members);
    }

    public TeamMember findMember(UUID uuid) {
        for (TeamMember m : members) {
            if (m.uuid().equals(uuid)) {
                return m;
            }
        }
        return null;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return findMember(uuid) != null;
    }

    public TeamData withMembers(List<TeamMember> newMembers) {
        return new TeamData(teamId, owner, newMembers, inviteCode, inviteExpiryMs);
    }

    public TeamData withOwner(UUID newOwner) {
        return new TeamData(teamId, newOwner, members, inviteCode, inviteExpiryMs);
    }

    public TeamData withInviteCode(String code, long expiryMs) {
        return new TeamData(teamId, owner, members, code, expiryMs);
    }

    public TeamData clearInviteCode() {
        return new TeamData(teamId, owner, members, "", 0L);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(NbtKeys.TEAM_ID, teamId);
        tag.putUUID(NbtKeys.OWNER, owner);
        tag.putString(NbtKeys.INVITE_CODE, inviteCode);
        tag.putLong(NbtKeys.INVITE_EXPIRY, inviteExpiryMs);
        ListTag membersList = new ListTag();
        for (TeamMember m : members) {
            membersList.add(m.toNbt());
        }
        tag.put(NbtKeys.MEMBERS, membersList);
        return tag;
    }

    public static TeamData fromNbt(CompoundTag tag) {
        UUID teamId = tag.getUUID(NbtKeys.TEAM_ID);
        UUID owner = tag.getUUID(NbtKeys.OWNER);
        String inviteCode = tag.getString(NbtKeys.INVITE_CODE);
        long inviteExpiry = tag.getLong(NbtKeys.INVITE_EXPIRY);
        ListTag membersList = tag.getList(NbtKeys.MEMBERS, Tag.TAG_COMPOUND);
        List<TeamMember> members = new ArrayList<>();
        for (int i = 0; i < membersList.size(); i++) {
            members.add(TeamMember.fromNbt(membersList.getCompound(i)));
        }
        return new TeamData(teamId, owner, members, inviteCode, inviteExpiry);
    }
}
