package com.abo47.questsandstuff.quest.runtime.team;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;

public interface TeamProgressProvider {
    ResourceLocation id();

    Collection<UUID> members(ServerLevel level, UUID playerId);

    default void installChangeHook(BiConsumer<ServerLevel, UUID> callback) {
    }
}
