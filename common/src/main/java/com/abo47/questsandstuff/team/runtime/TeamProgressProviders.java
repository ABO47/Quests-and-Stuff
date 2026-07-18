package com.abo47.questsandstuff.team.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public final class TeamProgressProviders {
    private static final Map<ResourceLocation, TeamProgressProvider> PROVIDERS = new LinkedHashMap<>();

    private TeamProgressProviders() {
    }

    public static void bootstrapDefaults() {
        if (!PROVIDERS.isEmpty()) {
            return;
        }
        register(SoloTeamProgressProvider.INSTANCE);
        register(MultiTeamProgressProvider.INSTANCE);
    }

    public static void register(TeamProgressProvider provider) {
        PROVIDERS.put(provider.id(), provider);
    }

    public static List<UUID> members(ServerLevel level, UUID playerId) {
        List<UUID> members = new ArrayList<>();
        for (TeamProgressProvider provider : PROVIDERS.values()) {
            members.addAll(provider.members(level, playerId));
        }
        if (members.isEmpty()) {
            members.add(playerId);
        }
        return members.stream().distinct().toList();
    }

    public static void installHooks(BiConsumer<ServerLevel, UUID> callback) {
        for (TeamProgressProvider provider : PROVIDERS.values()) {
            provider.installChangeHook(callback);
        }
    }

    public static Collection<TeamProgressProvider> all() {
        return PROVIDERS.values();
    }
}
