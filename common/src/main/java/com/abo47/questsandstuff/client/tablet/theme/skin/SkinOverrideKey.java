package com.abo47.questsandstuff.client.tablet.theme.skin;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.util.Map;
import java.util.Set;

public final class SkinOverrideKey {
    private static final Set<String> SHARED_KEYS = Set.of(
            "quest_details_root", "quest_details_tasks",
            "quest_details_task_section", "quest_details_reward_section",
            "quests_task_cards", "quests_reward_cards"
    );
    private static final Set<String> CARD_KEYS = Set.of(
            "quests_task_cards", "quests_reward_cards"
    );
    private static final Set<String> ROOT_KEYS = Set.of(
            "quest_details_root", "quest_details_tasks", "quest_details_task_section", "quest_details_reward_section"
    );
    private static final Set<String> CANVAS_PANEL_KEYS = Set.of(
            "quests_canvas", "quest_details_canvas_panel"
    );
    private static final Map<String, String> VIEWPORT_BG_KEYS = Map.of(
            "quests_canvas", "quests_canvas_background",
            "quest_details_canvas_panel", "quest_details_canvas_background"
    );

    private SkinOverrideKey() {
    }

    public static boolean isSharedKey(String targetKey) {
        return targetKey != null && SHARED_KEYS.contains(targetKey);
    }

    public static boolean isCardKey(String targetKey) {
        return targetKey != null && CARD_KEYS.contains(targetKey);
    }

    public static boolean isRootKey(String targetKey) {
        return targetKey != null && ROOT_KEYS.contains(targetKey);
    }

    public static String overrideKey(TabletUiState state, String targetKey) {
        if (targetKey == null || targetKey.isBlank()) return "";
        String resolved = resolveTargetKey(state, targetKey);
        if (isSharedKey(resolved)) return resolved;
        String app = state.root.currentApp;
        return app.isBlank() ? resolved : app + ":" + resolved;
    }

    public static String resolveOverride(TabletUiState state, String targetKey) {
        if (targetKey == null || targetKey.isBlank()) return null;
        String qualified = overrideKey(state, targetKey);
        String raw = state.root.skinFillOverrides.get(qualified);
        if (raw == null) {
            String bare = resolveTargetKey(state, targetKey);
            if (!bare.equals(qualified)) {
                raw = state.root.skinFillOverrides.get(bare);
            }
        }
        return raw;
    }

    public static String resolveTargetKey(TabletUiState state, String targetKey) {
        if (targetKey == null) return null;
        int colon = targetKey.indexOf(':');
        if (colon > 0 && targetKey.length() > colon + 1) {
            return targetKey.substring(colon + 1);
        }
        return targetKey;
    }

    public static boolean hasCanvasBackground(String targetKey) {
        return CANVAS_PANEL_KEYS.contains(targetKey);
    }

    public static String viewportBackgroundKey(String canvasKey) {
        return VIEWPORT_BG_KEYS.get(canvasKey);
    }
}
