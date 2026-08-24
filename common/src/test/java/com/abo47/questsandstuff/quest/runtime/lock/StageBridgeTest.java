package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.server.level.ServerPlayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StageBridgeTest {
    @AfterEach
    void tearDown() {
        StageBridge.setHook(null);
    }

    @Test
    void dispatchRequiresNonNullPlayer() {
        AtomicInteger completions = new AtomicInteger();
        StageBridge.setHook(new StageBridge.GrantHook() {
            @Override
            public void onQuestCompleted(ServerPlayer player, String questId) {
                completions.incrementAndGet();
            }

            @Override
            public void onQuestRevoked(ServerPlayer player, String questId) {
            }
        });
        StageBridge.onQuestCompleted(null, "test/quest");
        assertEquals(0, completions.get());
        StageBridge.setHook(null);
        assertFalse(StageBridge.installed());
    }

    @Test
    void nullPlayerIsIgnored() {
        AtomicInteger calls = new AtomicInteger();
        StageBridge.setHook(new StageBridge.GrantHook() {
            @Override
            public void onQuestCompleted(ServerPlayer player, String questId) {
                calls.incrementAndGet();
            }

            @Override
            public void onQuestRevoked(ServerPlayer player, String questId) {
                calls.incrementAndGet();
            }
        });
        StageBridge.onQuestCompleted(null, "a");
        StageBridge.onQuestRevoked(null, "b");
        assertEquals(0, calls.get());
    }

    @Test
    void blankQuestIdIsIgnored() {
        AtomicInteger calls = new AtomicInteger();
        StageBridge.setHook(new StageBridge.GrantHook() {
            @Override
            public void onQuestCompleted(ServerPlayer player, String questId) {
                calls.incrementAndGet();
            }

            @Override
            public void onQuestRevoked(ServerPlayer player, String questId) {
                calls.incrementAndGet();
            }
        });
        StageBridge.onQuestCompleted(null, "  ");
        StageBridge.onQuestRevoked(null, "");
        assertEquals(0, calls.get());
    }

    @Test
    void throwingHookIsContainedAndClearedAfterwards() {
        StageBridge.setHook(new StageBridge.GrantHook() {
            @Override
            public void onQuestCompleted(ServerPlayer player, String questId) {
                throw new IllegalStateException("bridge down");
            }

            @Override
            public void onQuestRevoked(ServerPlayer player, String questId) {
            }
        });
        StageBridge.onQuestCompleted(null, "x");
        StageBridge.setHook(null);
        assertFalse(StageBridge.installed());
        StageBridge.onQuestCompleted(null, "x");
    }
}
