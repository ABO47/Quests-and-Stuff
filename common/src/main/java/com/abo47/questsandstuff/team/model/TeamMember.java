package com.abo47.questsandstuff.team.model;

import java.util.UUID;

public record TeamMember(UUID uuid, String name, Role role) {
    public enum Role {
        OWNER,
        MEMBER
    }

    public TeamMember withRole(Role role) {
        return new TeamMember(uuid, name, role);
    }
}
