package com.abo47.questsandstuff.team.model;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.team.NbtKeys;

public record TeamMember(UUID uuid, String name, Role role) {
    public enum Role {
        OWNER,
        MEMBER
    }

    public TeamMember withRole(Role role) {
        return new TeamMember(uuid, name, role);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(NbtKeys.UUID, uuid);
        tag.putString(NbtKeys.NAME, name);
        tag.putString(NbtKeys.ROLE, role.name());
        return tag;
    }

    public static TeamMember fromNbt(CompoundTag tag) {
        UUID uuid = tag.getUUID(NbtKeys.UUID);
        String name = tag.getString(NbtKeys.NAME);
        String roleStr = tag.getString(NbtKeys.ROLE);
        Role role = "OWNER".equals(roleStr) ? Role.OWNER : Role.MEMBER;
        return new TeamMember(uuid, name, role);
    }
}
