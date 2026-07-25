package com.abo47.questsandstuff.network.quest.sync;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import net.minecraft.nbt.CompoundTag;

final class ClientboundSyncPacketDispatch {
    private static final String HANDLER_CLASS = "com.abo47.questsandstuff.client.sync.packet.ClientSyncPacketHandler";

    private static final Class<?> HANDLER;
    private static final MethodHandle HANDLE_FULL;
    private static final MethodHandle HANDLE_DELTA;
    private static final MethodHandle HANDLE_DESCRIPTION;
    private static final MethodHandle HANDLE_DISPLAY_CACHE;
    private static final MethodHandle HANDLE_PINNED;
    private static final MethodHandle HANDLE_QUEST_EVENT;
    private static final MethodHandle HANDLE_EDITOR_MUTATION;

    static {
        try {
            HANDLER = Class.forName(HANDLER_CLASS);
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType mt = MethodType.methodType(void.class, long.class, int.class, int.class, CompoundTag.class);
            HANDLE_FULL = lookup.findStatic(HANDLER, "handleFull", mt);
            HANDLE_DELTA = lookup.findStatic(HANDLER, "handleDelta", mt);
            HANDLE_DESCRIPTION = lookup.findStatic(HANDLER, "handleDescription", mt);
            HANDLE_DISPLAY_CACHE = lookup.findStatic(HANDLER, "handleDisplayCache",
                    MethodType.methodType(void.class, long.class, CompoundTag.class));
            HANDLE_PINNED = lookup.findStatic(HANDLER, "handlePinned",
                    MethodType.methodType(void.class, long.class, List.class));
            HANDLE_QUEST_EVENT = lookup.findStatic(HANDLER, "handleQuestEvent",
                    MethodType.methodType(void.class, long.class, String.class, String.class, String.class));
            HANDLE_EDITOR_MUTATION = lookup.findStatic(HANDLER, "handleEditorMutation",
                    MethodType.methodType(void.class, long.class, String.class, String.class, CompoundTag.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ClientboundSyncPacketDispatch() {
    }

    static void handleFull(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke(HANDLE_FULL, "handleFull", sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDelta(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke(HANDLE_DELTA, "handleDelta", sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDescription(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        invoke(HANDLE_DESCRIPTION, "handleDescription", sequence, chunkIndex, chunkCount, payload);
    }

    static void handleDisplayCache(long sequence, CompoundTag payload) {
        invoke(HANDLE_DISPLAY_CACHE, "handleDisplayCache", sequence, payload);
    }

    static void handlePinned(long sequence, List<String> pinned) {
        invoke(HANDLE_PINNED, "handlePinned", sequence, pinned);
    }

    static void handleQuestEvent(long sequence, String eventType, String questId, String rewardId) {
        invoke(HANDLE_QUEST_EVENT, "handleQuestEvent", sequence, eventType, questId, rewardId);
    }

    static void handleEditorMutation(long sequence, String action, String questId, CompoundTag questTag) {
        invoke(HANDLE_EDITOR_MUTATION, "handleEditorMutation", sequence, action, questId, questTag);
    }

    private static void invoke(MethodHandle handle, String name, Object... args) {
        try {
            handle.invokeWithArguments(args);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Client sync packet handler argument type mismatch: " + name, e);
        } catch (Throwable e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (e instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Client sync packet handler failed: " + name, e);
        }
    }
}
