package com.abo47.questsandstuff.team;

import com.abo47.questsandstuff.team.model.TeamMember;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TeamManager {
    private static final int INVITE_CODE_LENGTH = 8;
    private static final long INVITE_CODE_DURATION_MS = 3600_000L;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ServerLevel level;
    private final RuntimeEngine engine;
    private TeamSavedData savedData;

    public TeamManager(ServerLevel level, RuntimeEngine engine) {
        this.level = level;
        this.engine = engine;
    }

    private TeamSavedData data() {
        if (savedData == null) {
            savedData = TeamSavedData.get(level.getServer().overworld());
        }
        return savedData;
    }

    public TeamData createTeam(ServerPlayer player) {
        TeamData existing = data().getTeamByPlayer(player.getUUID());
        if (existing != null) {
            return existing;
        }
        UUID teamId = UUID.randomUUID();
        TeamMember owner = new TeamMember(player.getUUID(), player.getScoreboardName(), TeamMember.Role.OWNER);
        List<TeamMember> members = List.of(owner);
        TeamData team = new TeamData(teamId, player.getUUID(), members, "", 0L);
        data().putTeam(team);
        engine.triggerTeamMembershipChanged(level, player.getUUID());
        return team;
    }

    public String getJoinError(ServerPlayer player, String inviteCode) {
        TeamData existing = data().getTeamByPlayer(player.getUUID());
        if (existing != null && existing.inviteCode().equals(inviteCode) && !existing.inviteCode().isBlank()) {
            return "already_in_this_team";
        }
        if (existing != null && existing.members().size() > 1) {
            return "leave_first";
        }
        TeamData team = data().getTeamByInviteCode(inviteCode);
        if (team == null) {
            return "invalid_code";
        }
        return null;
    }

    public TeamData joinTeam(ServerPlayer player, String inviteCode) {
        TeamData existing = data().getTeamByPlayer(player.getUUID());
        if (existing != null && existing.members().size() <= 1) {
            data().removeTeam(existing.teamId());
        }
        TeamData team = data().getTeamByInviteCode(inviteCode);
        if (team == null) {
            return null;
        }
        List<TeamMember> newMembers = new ArrayList<>(team.members());
        newMembers.add(new TeamMember(player.getUUID(), player.getScoreboardName(), TeamMember.Role.MEMBER));
        TeamData updated = team.withMembers(newMembers);
        data().putTeam(updated);
        engine.triggerTeamMembershipChanged(level, player.getUUID());
        return updated;
    }

    public boolean leaveTeam(ServerPlayer player) {
        TeamData team = data().getTeamByPlayer(player.getUUID());
        if (team == null) {
            return false;
        }
        if (team.isOwner(player.getUUID()) && team.members().size() > 1) {
            return false;
        }
        data().removeInviteCodesForPlayer(player.getUUID(), team.teamId());
        List<TeamMember> newMembers = new ArrayList<>();
        for (TeamMember m : team.members()) {
            if (!m.uuid().equals(player.getUUID())) {
                newMembers.add(m);
            }
        }
        if (newMembers.isEmpty()) {
            data().removeTeam(team.teamId());
        } else {
            TeamData updated = team.withMembers(newMembers);
            data().removeTeam(team.teamId());
            for (TeamMember m : updated.members()) {
                data().updateMember(m.uuid(), updated);
            }
        }
        engine.triggerTeamMembershipChanged(level, player.getUUID());
        return true;
    }

    public boolean kickMember(ServerPlayer owner, UUID targetUuid) {
        TeamData team = data().getTeamByPlayer(owner.getUUID());
        if (team == null || !team.isOwner(owner.getUUID())) {
            return false;
        }
        if (team.owner().equals(targetUuid)) {
            return false;
        }
        if (!team.isMember(targetUuid)) {
            return false;
        }
        data().removeInviteCodesForPlayer(targetUuid, team.teamId());
        List<TeamMember> newMembers = new ArrayList<>();
        for (TeamMember m : team.members()) {
            if (!m.uuid().equals(targetUuid)) {
                newMembers.add(m);
            }
        }
        if (newMembers.isEmpty()) {
            data().removeTeam(team.teamId());
        } else {
            TeamData updated = team.withMembers(newMembers);
            data().removeTeam(team.teamId());
            for (TeamMember m : updated.members()) {
                data().updateMember(m.uuid(), updated);
            }
        }
        engine.triggerTeamMembershipChanged(level, targetUuid);
        engine.triggerTeamMembershipChanged(level, owner.getUUID());
        return true;
    }

    public boolean transferOwnership(ServerPlayer owner, UUID targetUuid) {
        TeamData team = data().getTeamByPlayer(owner.getUUID());
        if (team == null || !team.isOwner(owner.getUUID())) {
            return false;
        }
        if (!team.isMember(targetUuid)) {
            return false;
        }
        List<TeamMember> newMembers = new ArrayList<>();
        for (TeamMember m : team.members()) {
            if (m.uuid().equals(owner.getUUID())) {
                newMembers.add(new TeamMember(m.uuid(), m.name(), TeamMember.Role.MEMBER));
            } else if (m.uuid().equals(targetUuid)) {
                newMembers.add(new TeamMember(m.uuid(), m.name(), TeamMember.Role.OWNER));
            } else {
                newMembers.add(m);
            }
        }
        TeamData updated = team.withOwner(targetUuid).withMembers(newMembers);
        data().removeTeam(team.teamId());
        for (TeamMember m : updated.members()) {
            data().updateMember(m.uuid(), updated);
        }
        engine.triggerTeamMembershipChanged(level, targetUuid);
        engine.triggerTeamMembershipChanged(level, owner.getUUID());
        return true;
    }

    public String generateInviteCode(ServerPlayer player) {
        TeamData team = data().getTeamByPlayer(player.getUUID());
        if (team == null) {
            return "";
        }
        data().removeInviteCodesForPlayer(player.getUUID(), team.teamId());

        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        String codeStr = code.toString();
        long expiry = System.currentTimeMillis() + INVITE_CODE_DURATION_MS;
        data().putInviteCode(codeStr, new TeamSavedData.PlayerInviteCode(player.getUUID(), team.teamId(), expiry));

        TeamData updated = team.withInviteCode(codeStr, expiry);
        data().removeTeam(team.teamId());
        for (TeamMember m : updated.members()) {
            data().updateMember(m.uuid(), updated);
        }
        return codeStr;
    }

    public TeamData getTeam(ServerPlayer player) {
        return data().getTeamByPlayer(player.getUUID());
    }

    public TeamData getTeamByPlayer(UUID uuid) {
        return data().getTeamByPlayer(uuid);
    }

    public TeamData getTeamById(UUID teamId) {
        return data().getTeamById(teamId);
    }
}
