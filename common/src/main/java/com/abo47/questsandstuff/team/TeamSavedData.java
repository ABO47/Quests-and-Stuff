package com.abo47.questsandstuff.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;

public class TeamSavedData extends SavedData {
    private static final String DATA_NAME = QuestsAndStuffMod.MODID + "_teams";
    private final Map<UUID, TeamData> teamsByPlayer = new HashMap<>();
    private final Map<String, PlayerInviteCode> inviteCodes = new HashMap<>();

    public record PlayerInviteCode(UUID playerUuid, UUID teamId, long expiryMs) {
    }

    public TeamSavedData() {
    }

    public static TeamSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TeamSavedData::load,
                TeamSavedData::new,
                DATA_NAME
        );
    }

    public static TeamSavedData load(CompoundTag tag) {
        TeamSavedData data = new TeamSavedData();
        ListTag teamsList = tag.getList(NbtKeys.TEAMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < teamsList.size(); i++) {
            TeamData team = TeamData.fromNbt(teamsList.getCompound(i));
            for (TeamMember m : team.members()) {
                data.teamsByPlayer.put(m.uuid(), team);
            }
        }
        ListTag codesList = tag.getList(NbtKeys.INVITE_CODES, Tag.TAG_COMPOUND);
        for (int i = 0; i < codesList.size(); i++) {
            CompoundTag codeTag = codesList.getCompound(i);
            String code = codeTag.getString(NbtKeys.CODE);
            UUID playerUuid = codeTag.getUUID(NbtKeys.INVITE_PLAYER);
            UUID teamId = codeTag.getUUID(NbtKeys.INVITE_TEAM);
            long expiryMs = codeTag.getLong(NbtKeys.INVITE_EXPIRY);
            if (!code.isBlank()) {
                data.inviteCodes.put(code, new PlayerInviteCode(playerUuid, teamId, expiryMs));
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag teamsList = new ListTag();
        Map<UUID, TeamData> uniqueTeams = new HashMap<>();
        for (TeamData team : teamsByPlayer.values()) {
            uniqueTeams.put(team.teamId(), team);
        }
        for (TeamData team : uniqueTeams.values()) {
            teamsList.add(team.toNbt());
        }
        tag.put(NbtKeys.TEAMS, teamsList);
        ListTag codesList = new ListTag();
        for (Map.Entry<String, PlayerInviteCode> entry : inviteCodes.entrySet()) {
            CompoundTag codeTag = new CompoundTag();
            codeTag.putString(NbtKeys.CODE, entry.getKey());
            codeTag.putUUID(NbtKeys.INVITE_PLAYER, entry.getValue().playerUuid());
            codeTag.putUUID(NbtKeys.INVITE_TEAM, entry.getValue().teamId());
            codeTag.putLong(NbtKeys.INVITE_EXPIRY, entry.getValue().expiryMs());
            codesList.add(codeTag);
        }
        tag.put(NbtKeys.INVITE_CODES, codesList);
        return tag;
    }

    public TeamData getTeamByPlayer(UUID playerUuid) {
        return teamsByPlayer.get(playerUuid);
    }

    public TeamData getTeamById(UUID teamId) {
        for (TeamData team : teamsByPlayer.values()) {
            if (team.teamId().equals(teamId)) {
                return team;
            }
        }
        return null;
    }

    public TeamData getTeamByInviteCode(String code) {
        PlayerInviteCode pic = getInviteCode(code);
        if (pic != null) {
            TeamData team = getTeamById(pic.teamId());
            if (team != null) {
                return team;
            }
            inviteCodes.remove(code);
            setDirty();
        }
        return getTeamByInviteCodeOld(code);
    }

    public void putTeam(TeamData team) {
        for (TeamMember m : team.members()) {
            teamsByPlayer.put(m.uuid(), team);
        }
        setDirty();
    }

    public void removeTeam(UUID teamId) {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, TeamData> entry : teamsByPlayer.entrySet()) {
            if (entry.getValue().teamId().equals(teamId)) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID key : toRemove) {
            teamsByPlayer.remove(key);
        }
        setDirty();
    }

    public void updateMember(UUID playerUuid, TeamData team) {
        teamsByPlayer.put(playerUuid, team);
        setDirty();
    }

    public void putInviteCode(String code, PlayerInviteCode data) {
        inviteCodes.put(code, data);
        setDirty();
    }

    public void removeInviteCode(String code) {
        inviteCodes.remove(code);
        setDirty();
    }

    public PlayerInviteCode getInviteCode(String code) {
        PlayerInviteCode pic = inviteCodes.get(code);
        if (pic == null) {
            return null;
        }
        if (System.currentTimeMillis() >= pic.expiryMs()) {
            inviteCodes.remove(code);
            setDirty();
            return null;
        }
        return pic;
    }

    public void removeInviteCodesForTeam(UUID teamId) {
        Iterator<Map.Entry<String, PlayerInviteCode>> it = inviteCodes.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().teamId().equals(teamId)) {
                it.remove();
            }
        }
        setDirty();
    }

    public void removeInviteCodesForPlayer(UUID playerUuid, UUID teamId) {
        Iterator<Map.Entry<String, PlayerInviteCode>> it = inviteCodes.entrySet().iterator();
        while (it.hasNext()) {
            PlayerInviteCode pic = it.next().getValue();
            if (pic.playerUuid().equals(playerUuid) && pic.teamId().equals(teamId)) {
                it.remove();
            }
        }
        setDirty();
    }

    public TeamData getTeamByInviteCodeOld(String code) {
        for (TeamData team : teamsByPlayer.values()) {
            if (team.inviteCode().equals(code) && !team.inviteCode().isBlank()) {
                long now = System.currentTimeMillis();
                if (team.inviteExpiryMs() > now) {
                    return team;
                }
            }
        }
        return null;
    }
}
