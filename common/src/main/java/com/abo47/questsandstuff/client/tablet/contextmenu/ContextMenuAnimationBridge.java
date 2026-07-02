package com.abo47.questsandstuff.client.tablet.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.ContextMenuPopWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class ContextMenuAnimationBridge {
    public static final String DEFAULT_KEY = "context";
    public static final String CHAPTER_KEY = "chapter";

    private ContextMenuAnimationBridge() {
    }

    public static WidgetGroup wrap(WidgetGroup content) {
        return wrap(content, null, "");
    }

    public static WidgetGroup wrap(WidgetGroup content, TabletUiState state, String key) {
        if (content == null) {
            return new WidgetGroup(0, 0, 1, 1);
        }
        if (!QuestsAndStuffConfig.contextMenuAnimationsEnabled()) {
            return content;
        }
        long fallbackStartMs = System.currentTimeMillis();
        String safeKey = key == null ? "" : key;
        return ContextMenuPopWidget.menu(content, () -> effectiveStartMs(state, safeKey, fallbackStartMs));
    }

    public static void start(TabletUiState state, String key) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuAnimationStartMs = System.currentTimeMillis();
        state.contextMenu.contextMenuAnimationKey = key == null ? "" : key;
    }

    public static void finish(TabletUiState state, String key) {
        if (state == null) {
            return;
        }
        state.contextMenu.contextMenuAnimationStartMs = System.currentTimeMillis() - ContextMenuPopWidget.durationMs();
        state.contextMenu.contextMenuAnimationKey = key == null ? "" : key;
    }

    private static long effectiveStartMs(TabletUiState state, String key, long fallbackStartMs) {
        if (state == null || state.contextMenu.contextMenuAnimationStartMs <= 0L) {
            return fallbackStartMs;
        }
        String stateKey = state.contextMenu.contextMenuAnimationKey == null ? "" : state.contextMenu.contextMenuAnimationKey;
        return key.equals(stateKey) ? state.contextMenu.contextMenuAnimationStartMs : fallbackStartMs;
    }
}
