package com.abo47.questsandstuff.network;

import net.minecraft.network.FriendlyByteBuf;

public final class PacketBufHelper {
    private PacketBufHelper() {
    }

    public static void writeUtfSafe(FriendlyByteBuf buf, String value) {
        buf.writeUtf(value == null ? "" : value);
    }
}
