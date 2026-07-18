package com.abo47.questsandstuff.team.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.team.TeamSavedData;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;

public final class MultiTeamProgressProvider implements TeamProgressProvider {
    public static final MultiTeamProgressProvider INSTANCE = new MultiTeamProgressProvider();

    private MultiTeamProgressProvider() {
    }

    @Override
    public ResourceLocation id() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "multi_team");
    }

    @Override
    public Collection<UUID> members(ServerLevel level, UUID playerId) {
        TeamSavedData data = TeamSavedData.get(level);
        TeamData team = data.getTeamByPlayer(playerId);
        if (team == null) {
            return List.of(playerId);
        }
        List<UUID> memberIds = new ArrayList<>();
        for (TeamMember m : team.members()) {
            memberIds.add(m.uuid());
        }
        return memberIds;
    }
}
