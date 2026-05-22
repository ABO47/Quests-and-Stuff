package com.abo47.questsandstuff.quest.runtime.signal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public record QuestSignal(
        QuestSignalType type,
        ServerPlayer player,
        String key,
        int amount,
        BlockPos pos,
        ResourceKey<Level> dimension
) {
    public static QuestSignal of(QuestSignalType type, ServerPlayer player, String key, int amount, BlockPos pos) {
        return new QuestSignal(type, player, key == null ? "" : key, amount, pos, player.serverLevel().dimension());
    }
}
