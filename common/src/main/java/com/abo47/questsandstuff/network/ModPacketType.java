package com.abo47.questsandstuff.network;

import net.minecraft.network.FriendlyByteBuf;

public record ModPacketType<T>(
        int id,
        Class<T> type,
        Direction direction,
        PacketEncoder<T> encoder,
        PacketDecoder<T> decoder,
        PacketHandler<T> handler
) {
    public enum Direction {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER
    }

    @FunctionalInterface
    public interface PacketEncoder<T> {
        void encode(T packet, FriendlyByteBuf buffer);
    }

    @FunctionalInterface
    public interface PacketDecoder<T> {
        T decode(FriendlyByteBuf buffer);
    }

    @FunctionalInterface
    public interface PacketHandler<T> {
        void handle(T packet, ModPacketContext context);
    }
}
