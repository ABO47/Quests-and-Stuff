package com.abo47.questsandstuff.team.runtime;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public interface TeamProgressProvider {
    ResourceLocation id();

    Collection<UUID> members(ServerLevel level, UUID playerId);

    default void installChangeHook(BiConsumer<ServerLevel, UUID> callback) {
    }
}
