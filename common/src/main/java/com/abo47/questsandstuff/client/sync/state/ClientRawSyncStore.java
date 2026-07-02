package com.abo47.questsandstuff.client.sync.state;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;

public final class ClientRawSyncStore {
    private static final CompoundTag RAW = new CompoundTag();

    private ClientRawSyncStore() {
    }

    public static void reset() {
        new ArrayList<>(RAW.getAllKeys()).forEach(RAW::remove);
    }

    public static void replace(CompoundTag payload) {
        reset();
        merge(payload);
    }

    public static void merge(CompoundTag payload) {
        RAW.merge(payload == null ? new CompoundTag() : payload.copy());
    }
}
