package com.abo47.questsandstuff.client.tablet.teams;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public enum ClientTeamCache {
    INSTANCE;

    private volatile ClientTeamInfo team;
    private volatile JoinResult pendingJoinResult;

    public void setTeam(ClientTeamInfo team) {
        this.team = team;
    }

    public ClientTeamInfo getTeam() {
        return team;
    }

    public void clear() {
        team = null;
    }

    public void setPendingJoinResult(String message, boolean success) {
        pendingJoinResult = new JoinResult(message, success);
    }

    public JoinResult takePendingJoinResult() {
        JoinResult r = pendingJoinResult;
        pendingJoinResult = null;
        return r;
    }

    public record JoinResult(String message, boolean success) {
    }

    public record ClientTeamInfo(UUID teamId, UUID owner, List<ClientMember> members, String inviteCode, long inviteExpiryMs) {
        public ClientTeamInfo {
            members = List.copyOf(members);
        }

        public List<ClientMember> members() {
            return Collections.unmodifiableList(members);
        }

        public ClientMember findMember(UUID uuid) {
            for (ClientMember m : members) {
                if (m.uuid().equals(uuid)) {
                    return m;
                }
            }
            return null;
        }

        public boolean isOwner(UUID uuid) {
            return owner.equals(uuid);
        }
    }

    public record ClientMember(UUID uuid, String name, Role role) {
        public enum Role {
            OWNER,
            MEMBER
        }
    }
}
