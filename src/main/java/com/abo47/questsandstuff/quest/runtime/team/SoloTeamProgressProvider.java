package com.abo47.questsandstuff.quest.runtime.team;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
