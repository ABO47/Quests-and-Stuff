package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.concurrent.atomic.AtomicReference;

public final class LockViewerBridges {
    private static final AtomicReference<LockViewerBridge> JEI = new AtomicReference<>();

    private LockViewerBridges() {
    }

    public static void setJei(LockViewerBridge bridge) {
        JEI.set(bridge);
    }

    public static void clearJei() {
        JEI.set(null);
    }

    public static LockViewerBridge jei() {
        return JEI.get();
    }
}
