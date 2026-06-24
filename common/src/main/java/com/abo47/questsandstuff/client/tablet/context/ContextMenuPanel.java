package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import static com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem.CONTEXT_MENU_WIDTH;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTEXT_ROW_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addWindowsContextRow;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class ContextMenuPanel {
    public static final int PROMOTED_BAR_H = ContextActionLayout.PROMOTED_BAR_H;
    public static final int PROMOTED_BUTTON = ContextActionLayout.PROMOTED_BUTTON;
    private static final int OUTER_PAD = ContextActionLayout.OUTER_PAD;
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
        int maxW = Math.max(x + w + 4, TabletStateQueries.rootWidth(state));
        int maxH = Math.max(y + heightFor(actions, visibleRows) + 4, TabletStateQueries.rootHeight(state));
        return build(x, y, w, actions, start, visibleRows, borderColor, state, afterAction, animationKey, maxW, maxH);
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
            int maxW,
            int maxH
    ) {
        return build(x, y, w, actions, start, visibleRows, borderColor, state, afterAction, ContextMenuAnimation.DEFAULT_KEY, maxW, maxH);
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
            String animationKey,
            int maxW,
            int maxH
    ) {
        return buildInternal(x, y, w, actions, start, visibleRows, borderColor, state, afterAction, animationKey, true, maxW, maxH, x, y, null, null);
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
            int maxW,
            int maxH,
            ScrollState scrollState,
            Runnable refresh
    ) {
        return buildInternal(x, y, w, actions, start, visibleRows, borderColor, state, afterAction, ContextMenuAnimation.DEFAULT_KEY, true, maxW, maxH, x, y, scrollState, refresh);
    }

    private static WidgetGroup buildInternal(
            int x,
            int y,
            int w,
            List<ContextAction> actions,
            int start,
            int visibleRows,
            int borderColor,
            TabletUiState state,
            Consumer<ContextAction> afterAction,
            String animationKey,
            boolean animate,
            int maxW,
            int maxH,
            int absoluteX,
            int absoluteY,
            ScrollState scrollState,
            Runnable refresh
    ) {
        List<ContextAction> promoted = promotedActions(actions);
        List<ContextAction> rows = rowActions(actions);
        int safeVisibleRows = safeVisibleRows(rows.size(), visibleRows);
        int menuH = heightFor(actions, safeVisibleRows);
        int rowTop = rowTop(promoted);
        int scrollMax = Math.max(0, rows.size() - safeVisibleRows);
        int requestedStart = scrollState == null ? start : scrollState.value();
        int safeStart = Math.max(0, Math.min(requestedStart, scrollMax));
        if (scrollState != null && safeStart != requestedStart) {
            scrollState.setValue(safeStart);
        }
        int end = Math.min(rows.size(), safeStart + safeVisibleRows);
        boolean needsScroll = rows.size() > safeVisibleRows;
        int rowWidth = needsScroll ? w - SCROLLBAR_EXTRA_W : w - ROW_EXTRA_W;
        WidgetGroup menu = menuPanel(x, y, w, menuH, borderColor, needsScroll, scrollState, refresh, scrollMax);
        addPromotedBar(menu, promoted, w, state, afterAction, animationKey);
        for (int i = safeStart; i < end; i++) {
            ContextAction action = rows.get(i);
            int rowY = rowTop + (i - safeStart) * CONTEXT_ROW_H;
            addWindowsContextRow(menu, rowY, rowWidth, action.label(), action.icon(), action.accentColor(), action.hasSubmenu(), action.hasSubmenu()
                    ? click -> {
                    }
                    : click -> runAction(state, afterAction, action, animationKey));
            if (action.hasSubmenu()) {
                addSubmenu(menu, action, rowY, rowWidth, w, absoluteX, absoluteY, maxW, maxH, borderColor, state, afterAction, animationKey);
            }
        }
        if (needsScroll) {
            addScrollbar(menu, rows.size(), safeVisibleRows, safeStart, w, rowTop, scrollState, refresh);
        }
        return animate ? ContextMenuAnimation.wrap(menu, state, animationKey) : menu;
    }

    public static int heightForRows(int visibleRows) {
        return ContextActionLayout.heightForRows(visibleRows);
    }

    public static int heightFor(List<ContextAction> actions, int visibleRows) {
        return ContextActionLayout.heightFor(actions, visibleRows);
    }

    public static int heightForCounts(int promotedCount, int rowCount, int visibleRows) {
        return ContextActionLayout.heightForCounts(promotedCount, rowCount, visibleRows);
    }

    public static int rowActionCount(List<ContextAction> actions) {
        return ContextActionLayout.rowActionCount(actions);
    }

    public static List<ContextAction> promotedActions(List<ContextAction> actions) {
        return ContextActionLayout.promotedActions(actions);
    }

    public static List<ContextAction> rowActions(List<ContextAction> actions) {
        return ContextActionLayout.rowActions(actions);
    }

    public static int rowTop(List<ContextAction> promotedActions) {
        return ContextActionLayout.rowTop(promotedActions);
    }

    public static int safeVisibleRows(int rowCount, int visibleRows) {
        return ContextActionLayout.safeVisibleRows(rowCount, visibleRows);
    }

    public static int visiblePromotedCount(List<ContextAction> promoted, int menuW) {
        return ContextActionLayout.visiblePromotedCount(promoted, menuW);
    }

    public static List<ContextAction> visiblePromotedActions(List<ContextAction> promoted, int menuW) {
        return ContextActionLayout.visiblePromotedActions(promoted, menuW);
    }

    public static int promotedButtonX(int menuW, int visible, int index) {
        return ContextActionLayout.promotedButtonX(menuW, visible, index);
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
                ContextAction action = visiblePromoted.get(i);
                if (!action.hasSubmenu()) {
                    runAction(state, afterAction, action, animationKey);
                }
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
            ContextAction action = rows.get(actionIndex);
            if (!action.hasSubmenu()) {
                runAction(state, afterAction, action, animationKey);
            }
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
        if (action == null || action.hasSubmenu()) {
            return;
        }
        ContextMenuAnimation.finish(state, animationKey);
        action.action().run();
        if (afterAction != null) {
            afterAction.accept(action);
        }
    }

    private static void addSubmenu(
            WidgetGroup menu,
            ContextAction action,
            int rowY,
            int parentRowWidth,
            int parentMenuW,
            int parentAbsoluteX,
            int parentAbsoluteY,
            int maxW,
            int maxH,
            int borderColor,
            TabletUiState state,
            Consumer<ContextAction> afterAction,
            String animationKey
    ) {
        List<ContextAction> children = action.children();
        int childRows = rowActionCount(children);
        if (childRows <= 0) {
            return;
        }
        int childVisibleRows = safeVisibleRows(childRows, childRows);
        int childW = Math.min(CONTEXT_MENU_WIDTH, Math.max(48, maxW - 8));
        int childH = heightFor(children, childVisibleRows);
        int childX = submenuX(parentAbsoluteX, parentMenuW, childW, maxW);
        int desiredY = rowY - OUTER_PAD;
        int minChildY = 4 - parentAbsoluteY;
        int maxChildY = maxH - parentAbsoluteY - childH - 4;
        int childY = maxChildY < minChildY ? desiredY : Math.max(minChildY, Math.min(desiredY, maxChildY));
        WidgetGroup childMenu = buildInternal(
                childX,
                childY,
                childW,
                children,
                0,
                childVisibleRows,
                borderColor,
                state,
                afterAction,
                animationKey,
                false,
                maxW,
                maxH,
                parentAbsoluteX + childX,
                parentAbsoluteY + childY,
                null,
                null
        );
        childMenu.setVisible(false);
        childMenu.setActive(false);
        int rowLeft = 4;
        int rowRight = rowLeft + Math.max(1, parentRowWidth);
        int childLeft = childX;
        int childRight = childX + childW;
        int bridgeX = Math.min(rowLeft, childLeft);
        int bridgeRight = Math.max(rowRight, childRight);
        WidgetGroup hoverBridge = new WidgetGroup(bridgeX, rowY, Math.max(1, bridgeRight - bridgeX), CONTEXT_ROW_H);
        WidgetGroup hoverController = new WidgetGroup(rowLeft, rowY, Math.max(1, parentRowWidth), CONTEXT_ROW_H) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                boolean parentHovered = isMouseOverElement(mouseX, mouseY);
                boolean keepOpen = childMenu.isVisible()
                        && (hoverBridge.isMouseOverElement(mouseX, mouseY)
                        || childMenu.isMouseOverElement(mouseX, mouseY));
                boolean open = parentHovered || keepOpen;
                childMenu.setVisible(open);
                childMenu.setActive(open);
            }
        };
        menu.addWidget(hoverBridge);
        menu.addWidget(hoverController);
        menu.addWidget(childMenu);
    }

    private static int submenuX(int parentAbsoluteX, int parentMenuW, int childW, int maxW) {
        int rightX = parentMenuW - 1;
        int leftX = -childW + 1;
        int rightOverflow = horizontalOverflow(parentAbsoluteX + rightX, childW, maxW);
        int leftOverflow = horizontalOverflow(parentAbsoluteX + leftX, childW, maxW);
        if (rightOverflow == 0) {
            return rightX;
        }
        if (leftOverflow == 0) {
            return leftX;
        }
        int preferredX = leftOverflow < rightOverflow ? leftX : rightX;
        int minAbsX = 4;
        int maxAbsX = Math.max(minAbsX, maxW - childW - 4);
        int clampedAbsX = Math.max(minAbsX, Math.min(parentAbsoluteX + preferredX, maxAbsX));
        return clampedAbsX - parentAbsoluteX;
    }

    private static int horizontalOverflow(int absoluteX, int width, int maxW) {
        int overflow = 0;
        if (absoluteX < 4) {
            overflow += 4 - absoluteX;
        }
        int right = absoluteX + width;
        int maxRight = maxW - 4;
        if (right > maxRight) {
            overflow += right - maxRight;
        }
        return overflow;
    }

    private static int preferredWidth(List<ContextAction> actions, int minWidth, int maxWidth) {
        return ContextActionLayout.preferredWidth(actions, minWidth, maxWidth);
    }

    private static WidgetGroup menuPanel(int x, int y, int w, int h, int borderColor, boolean needsScroll, ScrollState scrollState, Runnable refresh, int scrollMax) {
        WidgetGroup menu = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
                if (!needsScroll || scrollState == null || !isMouseOverElement(mouseX, mouseY)) {
                    return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
                }
                int current = ScrollController.clamp(scrollState.value(), scrollMax);
                int next = ScrollController.wheel(current, scrollMax, 1, wheelDelta);
                if (next != current) {
                    scrollState.setValue(next);
                    if (refresh != null) {
                        refresh.run();
                    }
                }
                return true;
            }
        };
        menu.setBackground(Surfaces.bordered(withAlpha(ModColors.SURFACE_BASE, 246), borderColor));
        return menu;
    }

    private static void addScrollbar(WidgetGroup menu, int actionCount, int visibleRows, int start, int menuW, int rowTop, ScrollState scrollState, Runnable refresh) {
        int trackX = menuW - DragScrollBarWidget.RESERVED_WIDTH;
        int trackY = rowTop;
        int trackH = visibleRows * CONTEXT_ROW_H;
        int knobH = Math.max(14, (trackH * visibleRows) / Math.max(1, actionCount));
        int scrollMax = Math.max(1, actionCount - visibleRows);
        if (scrollState != null) {
            Runnable safeRefresh = refresh == null ? () -> {
            } : refresh;
            menu.addWidget(new DragScrollBarWidget(
                    trackX,
                    trackY,
                    DragScrollBarWidget.RESERVED_WIDTH,
                    trackH,
                    () -> ScrollController.clamp(scrollState.value(), scrollMax),
                    () -> scrollMax,
                    () -> knobH,
                    value -> scrollState.setValue(ScrollController.clamp(value, scrollMax)),
                    scrollState::dragging,
                    scrollState::setDragging,
                    safeRefresh,
                    ModColors.scrollTrack(scrollState.dragging()),
                    ModColors.scrollThumb(false),
                    ModColors.scrollThumb(true),
                    DragScrollBarWidget.WIDTH
            ));
            return;
        }
        int knobOffset = Math.round(((float) start / (float) scrollMax) * Math.max(0, trackH - knobH));
        menu.addWidget(new WidgetGroup(trackX, trackY, DragScrollBarWidget.RESERVED_WIDTH, trackH) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int railW = 2;
                int railX = getPositionX() + Math.max(0, (getSizeWidth() - railW) / 2);
                int thumbX = getPositionX() + Math.max(0, (getSizeWidth() - DragScrollBarWidget.WIDTH) / 2);
                DragScrollBarWidget.drawVerticalTrack(graphics, mouseX, mouseY, railX, getPositionY(), railW, getSizeHeight(), ModColors.scrollTrack(false));
                DragScrollBarWidget.drawVerticalThumb(graphics, mouseX, mouseY, thumbX, getPositionY() + knobOffset, DragScrollBarWidget.WIDTH, knobH, ModColors.scrollThumb(false));
            }
        });
    }

}
