package com.abo47.questsandstuff.client.sync.packet;

import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ClientSyncUiBridge {
    private static Runnable activeTabletRefresh = () -> {
    };
    private static Runnable activeCanvasRefresh = () -> {
    };
    private static Supplier<String> activeSelectedChapter = () -> "";
    private static Consumer<CompoundTag> pasteSelection = payload -> {
    };

    private ClientSyncUiBridge() {
    }

    public static void registerTabletCallbacks(
            Runnable tabletRefresh,
            Runnable canvasRefresh,
            Supplier<String> selectedChapter,
            Consumer<CompoundTag> pasteSelectionHandler
    ) {
        activeTabletRefresh = tabletRefresh == null ? () -> {
        } : tabletRefresh;
        activeCanvasRefresh = canvasRefresh == null ? () -> {
        } : canvasRefresh;
        activeSelectedChapter = selectedChapter == null ? () -> "" : selectedChapter;
        pasteSelection = pasteSelectionHandler == null ? payload -> {
        } : pasteSelectionHandler;
    }

    public static void requestActiveTabletRefresh() {
        activeTabletRefresh.run();
    }

    public static void requestActiveCanvasRefresh() {
        activeCanvasRefresh.run();
    }

    public static String activeSelectedChapter() {
        String selected = activeSelectedChapter.get();
        return selected == null ? "" : selected;
    }

    public static void selectPastedQuests(CompoundTag payload) {
        pasteSelection.accept(payload == null ? new CompoundTag() : payload);
    }

    public static void resetForTests() {
        registerTabletCallbacks(null, null, null, null);
    }
}
