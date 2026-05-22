package com.abo47.questsandstuff.network;

import net.minecraft.server.level.ServerPlayer;

public interface QuestPacketContext {
    ServerPlayer sender();

    void enqueueWork(Runnable work);
}
