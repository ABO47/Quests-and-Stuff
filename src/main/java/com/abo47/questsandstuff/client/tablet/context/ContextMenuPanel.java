package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTEXT_ROW_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addWindowsContextRow;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class ContextMenuPanel {
    public static final int PROMOTED_BAR_H = 18;
    public static final int PROMOTED_BUTTON = 16;
    private static final int OUTER_PAD = 4;
    private static final int SCROLLBAR_EXTRA_W = 14;
    private static final int ROW_EXTRA_W = 8;

    private ContextMenuPanel() {
    }

    public static WidgetGroup build(
            int x,
            int y,
            int w,
            List<ContextAction> actions,
            int start,
            int visibleRows,
            int borderColor,
            TabletUiState state,
            Consumer<ContextAction> afterAction
    ) {
        return build(x, y, w, actions, start, visibleRows, borderColor, state, afterAction, ContextMenuAnimation.DEFAULT_KEY);
    }

    public static WidgetGroup build(
            int x,
            int y,
            int w,
            List<ContextAction> actions,
            int start,
            int visibleRows,
            int borderColor,
            TabletUiState state,
            Consumer<ContextAction> afterAction,
            String animationKey
    ) {
        List<ContextAction> promoted = promotedActions(actions);
        List<ContextAction> rows = rowActions(actions);
        int safeVisibleRows = safeVisibleRows(rows.size(), visibleRows);
        int menuH = heightFor(actions, safeVisibleRows);
        int rowTop = rowTop(promoted);
        int safeStart = Math.max(0, Math.min(start, Math.max(0, rows.size() - safeVisibleRows)));
        int end = Math.min(rows.size(), safeStart + safeVisibleRows);
        boolean needsScroll = rows.size() > safeVisibleRows;
        int rowWidth = needsScroll ? w - SCROLLBAR_EXTRA_W : w - ROW_EXTRA_W;
        WidgetGroup menu = panel(x, y, w, menuH, withAlpha(ModColors.SURFACE_BASE, 246), borderColor);
        addPromotedBar(menu, promoted, w, state, afterAction, animationKey);
        for (int i = safeStart; i < end; i++) {
            ContextAction action = rows.get(i);
            int rowY = rowTop + (i - safeStart) * CONTEXT_ROW_H;
            addWindowsContextRow(menu, rowY, rowWidth, action.label(), action.icon(), click -> runAction(state, afterAction, action, animationKey));
        }
        if (needsScroll) {
            addScrollbar(menu, rows.size(), safeVisibleRows, safeStart, w, rowTop);
        }
        return ContextMenuAnimation.wrap(menu, state, animationKey);
    }

    public static int heightForRows(int visibleRows) {
        return OUTER_PAD * 2 + Math.max(1, visibleRows) * CONTEXT_ROW_H;
    }

    public static int heightFor(List<ContextAction> actions, int visibleRows) {
        List<ContextAction> promoted = promotedActions(actions);
        int rowCount = rowActionCount(actions);
        return heightForCounts(promoted.size(), rowCount, visibleRows);
    }

    public static int heightForCounts(int promotedCount, int rowCount, int visibleRows) {
        int rowH = rowCount == 0 ? 0 : Math.max(1, Math.min(visibleRows, rowCount)) * CONTEXT_ROW_H;
        return OUTER_PAD * 2 + (promotedCount <= 0 ? 0 : PROMOTED_BAR_H) + rowH;
    }

    public static int rowActionCount(List<ContextAction> actions) {
        return rowActions(actions).size();
    }

    public static List<ContextAction> promotedActions(List<ContextAction> actions) {
        List<ContextAction> promoted = new ArrayList<>();
        List<ContextAction> deleteActions = new ArrayList<>();
        if (actions != null) {
            for (ContextAction action : actions) {
                if (action != null && action.promoted()) {
                    if (isDeleteAction(action)) {
                        deleteActions.add(action);
                    } else {
                        promoted.add(action);
                    }
                }
            }
        }
        promoted.addAll(deleteActions);
        return promoted;
    }

    public static List<ContextAction> rowActions(List<ContextAction> actions) {
        List<ContextAction> rows = new ArrayList<>();
        if (actions != null) {
            for (ContextAction action : actions) {
                if (action != null && !action.promoted()) {
                    rows.add(action);
                }
            }
        }
        return rows;
    }

    public static int rowTop(List<ContextAction> promotedActions) {
        return OUTER_PAD + (promotedActions == null || promotedActions.isEmpty() ? 0 : PROMOTED_BAR_H);
    }

    public static int safeVisibleRows(int rowCount, int visibleRows) {
        if (rowCount <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(visibleRows, rowCount));
    }

    public static int visiblePromotedCount(List<ContextAction> promoted, int menuW) {
        if (promoted == null || promoted.isEmpty()) {
            return 0;
        }
        int available = Math.max(PROMOTED_BUTTON, menuW - OUTER_PAD * 2);
        int maxButtons = Math.max(1, available / PROMOTED_BUTTON);
        return Math.min(promoted.size(), maxButtons);
    }

    public static List<ContextAction> visiblePromotedActions(List<ContextAction> promoted, int menuW) {
        int visible = visiblePromotedCount(promoted, menuW);
        if (visible <= 0) {
            return List.of();
        }
        if (promoted.size() <= visible) {
            return promoted;
        }

        List<ContextAction> visibleActions = new ArrayList<>(visible);
        List<ContextAction> deleteActions = new ArrayList<>();
        for (ContextAction action : promoted) {
            if (isDeleteAction(action)) {
                deleteActions.add(action);
            }
        }
        if (deleteActions.isEmpty()) {
            return promoted.subList(0, visible);
        }

        int deleteCount = Math.min(deleteActions.size(), visible);
        int regularSlots = visible - deleteCount;
        for (ContextAction action : promoted) {
            if (visibleActions.size() >= regularSlots) {
                break;
            }
            if (!isDeleteAction(action)) {
                visibleActions.add(action);
            }
        }
        visibleActions.addAll(deleteActions.subList(deleteActions.size() - deleteCount, deleteActions.size()));
        return visibleActions;
    }

    public static int promotedButtonX(int menuW, int visible, int index) {
        int available = Math.max(PROMOTED_BUTTON, menuW - OUTER_PAD * 2);
        int center = Math.round(((index + 0.5f) * available) / Math.max(1, visible));
        return OUTER_PAD + Math.max(0, center - PROMOTED_BUTTON / 2);
    }

    public static boolean click(
            List<ContextAction> actions,
            int start,
            int visibleRows,
            int menuX,
            int menuY,
            int menuW,
            int mouseX,
            int mouseY,
            TabletUiState state,
            Consumer<ContextAction> afterAction,
            String animationKey
    ) {
        int menuH = heightFor(actions, visibleRows);
        if (mouseX < menuX || mouseX > menuX + menuW || mouseY < menuY || mouseY > menuY + menuH) {
            return false;
        }
        List<ContextAction> promoted = promotedActions(actions);
        List<ContextAction> rows = rowActions(actions);
        int safeVisibleRows = safeVisibleRows(rows.size(), visibleRows);
        int safeStart = Math.max(0, Math.min(start, Math.max(0, rows.size() - safeVisibleRows)));
        int relX = mouseX - menuX;
        int relY = mouseY - menuY;
        if (!promoted.isEmpty() && relY >= OUTER_PAD && relY < OUTER_PAD + PROMOTED_BAR_H) {
            List<ContextAction> visiblePromoted = visiblePromotedActions(promoted, menuW);
            int visible = visiblePromoted.size();
            int y = OUTER_PAD + Math.max(0, (PROMOTED_BAR_H - PROMOTED_BUTTON) / 2);
            for (int i = 0; i < visible; i++) {
                int buttonX = promotedButtonX(menuW, visible, i);
                if (relX < buttonX || relX >= buttonX + PROMOTED_BUTTON || relY < y || relY >= y + PROMOTED_BUTTON) {
                    continue;
                }
                runAction(state, afterAction, visiblePromoted.get(i), animationKey);
                return true;
            }
            return true;
        }
        int rowTop = rowTop(promoted);
        if (relY < rowTop || relY >= rowTop + safeVisibleRows * CONTEXT_ROW_H) {
            return true;
        }
        int row = (relY - rowTop) / CONTEXT_ROW_H;
        int actionIndex = safeStart + row;
        if (actionIndex >= 0 && actionIndex < rows.size()) {
            runAction(state, afterAction, rows.get(actionIndex), animationKey);
            return true;
        }
        return true;
    }

    private static void addPromotedBar(WidgetGroup menu, List<ContextAction> promoted, int menuW, TabletUiState state, Consumer<ContextAction> afterAction, String animationKey) {
        if (promoted.isEmpty()) {
            return;
        }
        List<ContextAction> visiblePromoted = visiblePromotedActions(promoted, menuW);
        int visible = visiblePromoted.size();
        int y = OUTER_PAD + Math.max(0, (PROMOTED_BAR_H - PROMOTED_BUTTON) / 2);
        for (int i = 0; i < visible; i++) {
            ContextAction action = visiblePromoted.get(i);
            int buttonX = promotedButtonX(menuW, visible, i);
            menu.addWidget(IconOnlyButton.create(buttonX, y, PROMOTED_BUTTON, ContextMenuSystem.contextIconFileKey(action.icon()), action.accentColor(), click -> runAction(state, afterAction, action, animationKey))
                    .tooltips(new Component[]{Component.literal(action.label())}));
        }
        WidgetGroup sep = new WidgetGroup(OUTER_PAD, OUTER_PAD + PROMOTED_BAR_H - 1, menuW - OUTER_PAD * 2, 1);
        sep.setBackground(Surfaces.fill(withAlpha(ModColors.BORDER_BASE, 130)));
        menu.addWidget(sep);
    }

    private static void runAction(TabletUiState state, Consumer<ContextAction> afterAction, ContextAction action, String animationKey) {
        ContextMenuAnimation.finish(state, animationKey);
        action.action().run();
        if (afterAction != null) {
            afterAction.accept(action);
        }
    }

    private static void addScrollbar(WidgetGroup menu, int actionCount, int visibleRows, int start, int menuW, int rowTop) {
        int trackX = menuW - DragScrollBarWidget.RESERVED_WIDTH;
        int trackY = rowTop;
        int trackH = visibleRows * CONTEXT_ROW_H;
        int knobX = trackX + Math.max(0, (DragScrollBarWidget.RESERVED_WIDTH - DragScrollBarWidget.WIDTH) / 2);
        WidgetGroup track = new WidgetGroup(knobX + 1, trackY, 2, trackH);
        track.setBackground(Surfaces.fill(ModColors.scrollTrack(false)));
        menu.addWidget(track);

        int knobH = Math.max(8, (trackH * visibleRows) / Math.max(1, actionCount));
        int scrollMax = Math.max(1, actionCount - visibleRows);
        int knobOffset = Math.round(((float) start / (float) scrollMax) * Math.max(0, trackH - knobH));
        WidgetGroup knob = new WidgetGroup(knobX, trackY + knobOffset, DragScrollBarWidget.WIDTH, knobH);
        knob.setBackground(Surfaces.fill(ModColors.scrollThumb(false)));
        menu.addWidget(knob);
    }

    private static boolean isDeleteAction(ContextAction action) {
        return "delete".equals(action.icon());
    }
}
