package com.abo47.questsandstuff.client.tablet.contextmenu;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CONTEXT_ROW_H;

final class ContextActionLayout {
    static final int PROMOTED_BAR_H = 18;
    static final int PROMOTED_BUTTON = GRID_16;
    static final int OUTER_PAD = 4;

    private ContextActionLayout() {
    }

    static int heightForRows(int visibleRows) {
        return OUTER_PAD * 2 + Math.max(1, visibleRows) * CONTEXT_ROW_H;
    }

    static int heightFor(List<ContextAction> actions, int visibleRows) {
        int promotedCount = promotedActions(actions).size();
        int rowCount = rowActionCount(actions);
        return heightForCounts(promotedCount, rowCount, visibleRows);
    }

    static int heightForCounts(int promotedCount, int rowCount, int visibleRows) {
        int rowH = rowCount == 0 ? 0 : Math.max(1, Math.min(visibleRows, rowCount)) * CONTEXT_ROW_H;
        return OUTER_PAD * 2 + (promotedCount <= 0 ? 0 : PROMOTED_BAR_H) + rowH;
    }

    static int rowActionCount(List<ContextAction> actions) {
        return rowActions(actions).size();
    }

    static List<ContextAction> promotedActions(List<ContextAction> actions) {
        List<ContextAction> promoted = orderedPromotedActions(actions);
        return promoted.size() < 2 ? List.of() : promoted;
    }

    static List<ContextAction> rowActions(List<ContextAction> actions) {
        List<ContextAction> rows = new ArrayList<>();
        boolean showPromotedBar = orderedPromotedActions(actions).size() >= 2;
        if (actions != null) {
            for (ContextAction action : actions) {
                if (action != null && (!showPromotedBar || !action.promoted())) {
                    rows.add(action);
                }
            }
        }
        return rows;
    }

    static int rowTop(List<ContextAction> promotedActions) {
        return OUTER_PAD + (promotedActions == null || promotedActions.isEmpty() ? 0 : PROMOTED_BAR_H);
    }

    static int safeVisibleRows(int rowCount, int visibleRows) {
        if (rowCount <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(visibleRows, rowCount));
    }

    static int visiblePromotedCount(List<ContextAction> promoted, int menuW) {
        if (promoted == null || promoted.isEmpty()) {
            return 0;
        }
        int available = Math.max(PROMOTED_BUTTON, menuW - OUTER_PAD * 2);
        int maxButtons = Math.max(1, available / PROMOTED_BUTTON);
        return Math.min(promoted.size(), maxButtons);
    }

    static List<ContextAction> visiblePromotedActions(List<ContextAction> promoted, int menuW) {
        int visible = visiblePromotedCount(promoted, menuW);
        if (visible <= 0) {
            return List.of();
        }
        if (promoted.size() <= visible) {
            return promoted;
        }

        List<ContextAction> visibleActions = new ArrayList<>(visible);
        List<ContextAction> destructiveActions = new ArrayList<>();
        for (ContextAction action : promoted) {
            if (isDestructiveAction(action)) {
                destructiveActions.add(action);
            }
        }
        if (destructiveActions.isEmpty()) {
            return promoted.subList(0, visible);
        }

        int destructiveCount = Math.min(destructiveActions.size(), visible);
        int regularSlots = visible - destructiveCount;
        for (ContextAction action : promoted) {
            if (visibleActions.size() >= regularSlots) {
                break;
            }
            if (!isDestructiveAction(action)) {
                visibleActions.add(action);
            }
        }
        visibleActions.addAll(destructiveActions.subList(destructiveActions.size() - destructiveCount, destructiveActions.size()));
        return visibleActions;
    }

    static int promotedButtonX(int menuW, int visible, int index) {
        int available = Math.max(PROMOTED_BUTTON, menuW - OUTER_PAD * 2);
        int center = Math.round(((index + 0.5f) * available) / Math.max(1, visible));
        return OUTER_PAD + Math.max(0, center - PROMOTED_BUTTON / 2);
    }

    static int preferredWidth(List<ContextAction> actions, int minWidth, int maxWidth) {
        List<String> labels = new ArrayList<>();
        for (ContextAction action : rowActions(actions)) {
            labels.add(action.label());
        }
        int rowWidth = ContextMenuRenderer.preferredMenuWidth(labels, minWidth, maxWidth);
        int promotedWidth = promotedActions(actions).size() * PROMOTED_BUTTON + OUTER_PAD * 2;
        return Math.max(rowWidth, Math.min(maxWidth, Math.max(minWidth, promotedWidth)));
    }

    private static List<ContextAction> orderedPromotedActions(List<ContextAction> actions) {
        List<ContextAction> promoted = new ArrayList<>();
        List<ContextAction> destructiveActions = new ArrayList<>();
        if (actions != null) {
            for (ContextAction action : actions) {
                if (action != null && action.promoted()) {
                    if (isDestructiveAction(action)) {
                        destructiveActions.add(action);
                    } else {
                        promoted.add(action);
                    }
                }
            }
        }
        promoted.addAll(destructiveActions);
        return promoted;
    }

    private static boolean isDestructiveAction(ContextAction action) {
        return action != null && action.tone().destructive();
    }
}
