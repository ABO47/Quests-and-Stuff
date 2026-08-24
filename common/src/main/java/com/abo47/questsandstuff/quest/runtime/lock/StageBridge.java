package com.abo47.questsandstuff.quest.runtime.lock;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class StageBridge {
    public interface GrantHook {
        void onQuestCompleted(ServerPlayer player, String questId);

        void onQuestRevoked(ServerPlayer player, String questId);
    }

    private static volatile GrantHook hook;
    private static final java.util.Set<String> WARNED =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private StageBridge() {
    }

    public static void setHook(GrantHook grantHook) {
        hook = grantHook;
        if (grantHook != null) {
            QuestsAndStuffMod.LOGGER.info("[QnS:Lock] stage bridge installed ({})",
                    grantHook.getClass().getSimpleName());
        }
    }

    public static boolean installed() {
        return hook != null;
    }

    public static void onQuestCompleted(ServerPlayer player, String questId) {
        dispatch("completed", player, questId, true);
    }

    public static void onQuestRevoked(ServerPlayer player, String questId) {
        dispatch("revoked", player, questId, false);
    }

    private static void dispatch(String action, ServerPlayer player, String questId, boolean completion) {
        GrantHook current = hook;
        if (current == null || player == null || questId == null || questId.isBlank()) {
            return;
        }
        try {
            if (completion) {
                current.onQuestCompleted(player, questId);
            } else {
                current.onQuestRevoked(player, questId);
            }
            QuestsAndStuffMod.debugLog("[QnS:Lock] stage bridge {} quest {}", action, questId);
        } catch (Exception error) {
            if (WARNED.add(action + ":" + questId)) {
                QuestsAndStuffMod.LOGGER.warn(
                        "[QnS:Lock] stage bridge failed for {} of quest {}", action, questId, error);
            }
        }
    }
}
