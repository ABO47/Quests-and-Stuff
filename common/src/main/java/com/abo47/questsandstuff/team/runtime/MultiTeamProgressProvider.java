package com.abo47.questsandstuff.team.runtime;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.model.TeamMember;
import com.abo47.questsandstuff.team.TeamSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
