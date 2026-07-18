package com.abo47.questsandstuff.team.runtime;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class SoloTeamProgressProvider implements TeamProgressProvider {
    public static final SoloTeamProgressProvider INSTANCE = new SoloTeamProgressProvider();

    private SoloTeamProgressProvider() {
    }

    @Override
    public ResourceLocation id() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "solo");
    }

    @Override
    public Collection<UUID> members(ServerLevel level, UUID playerId) {
        return List.of(playerId);
    }
}
