package com.abo47.questsandstuff.quest.model.team;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
}
