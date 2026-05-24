package com.abo47.questsandstuff.network.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

public final class SyncPacketPayloadLimits {
    public static final long MAX_SYNC_NBT_BYTES = 8L * 1024L * 1024L;

    private SyncPacketPayloadLimits() {
    }

    public static CompoundTag readNbt(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt(new NbtAccounter(MAX_SYNC_NBT_BYTES));
        return tag == null ? new CompoundTag() : tag;
    }
}
