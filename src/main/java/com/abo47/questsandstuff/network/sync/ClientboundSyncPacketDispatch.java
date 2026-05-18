package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.client.sync.packet.ClientSyncPacketHandler;

import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

final class ClientboundSyncPacketDispatch {
    private static final String HANDLER_CLASS = "com.abo47.questsandstuff.client.sync.packet.ClientSyncPacketHandler";

    private ClientboundSyncPacketDispatch() {
    }

    static void handleFull(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke("handleFull", new Class<?>[]{long.class, int.class, int.class, CompoundTag.class}, sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDelta(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke("handleDelta", new Class<?>[]{long.class, int.class, int.class, CompoundTag.class}, sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDescription(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke("handleDescription", new Class<?>[]{long.class, int.class, int.class, CompoundTag.class}, sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDisplayCache(long sequence, CompoundTag payload) {
        invoke("handleDisplayCache", new Class<?>[]{long.class, CompoundTag.class}, sequence, payload);
    }

    static void handlePinned(long sequence, List<String> pinned) {
        invoke("handlePinned", new Class<?>[]{long.class, List.class}, sequence, pinned);
    }

    static void handleQuestEvent(long sequence, String eventType, String questId, String rewardId) {
        invoke("handleQuestEvent", new Class<?>[]{long.class, String.class, String.class, String.class}, sequence, eventType, questId, rewardId);
    }

    static void handleEditorMutation(long sequence, String action, String questId, CompoundTag questTag) {
        invoke("handleEditorMutation", new Class<?>[]{long.class, String.class, String.class, CompoundTag.class}, sequence, action, questId, questTag);
    }

    private static void invoke(String method, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> handler = Class.forName(HANDLER_CLASS);
            handler.getMethod(method, parameterTypes).invoke(null, args);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to dispatch client sync packet to " + method, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Client sync packet handler failed: " + method, cause);
        }
    }
}
