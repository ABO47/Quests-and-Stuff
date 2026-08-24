package com.abo47.questsandstuff.client.quest.lock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class ClientLockEvents {
    public interface Listener {
        void onLockStatesChanged();
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private ClientLockEvents() {
    }

    public static void register(Listener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
            QuestsAndStuffMod.debugLog("[QnS:Lock] lock-state listener registered ({})", listener.getClass().getSimpleName());
        }
    }

    public static void unregister(Listener listener) {
        LISTENERS.remove(listener);
    }

    public static void fire() {
        QuestsAndStuffMod.debugLog("[QnS:Lock] lock states changed, notifying {} listener(s)", LISTENERS.size());
        for (Listener listener : LISTENERS) {
            try {
                listener.onLockStatesChanged();
            } catch (Exception error) {
                QuestsAndStuffMod.LOGGER.warn("[QnS:Lock] lock-state listener failed", error);
            }
        }
    }
}
