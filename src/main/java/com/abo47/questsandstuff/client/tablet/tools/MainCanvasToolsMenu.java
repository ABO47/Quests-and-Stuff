package com.abo47.questsandstuff.client.tablet.tools;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.applyCanvasBgOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.applyGridOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.cyclePercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.toolPercentStep;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.tools.TabletToolButtons.addToggle;

final class MainCanvasToolsMenu {
    private MainCanvasToolsMenu() {
    }

    static void rebuild(WidgetGroup toolsMenu, TabletUiState state, Player player, Runnable refresh, int canvasX, int toolsX, int topY, int headerH, int toolsW) {
        toolsMenu.clearAllWidgets();
        boolean visible = state.toolsMenuOpen;
        toolsMenu.setVisible(visible);
        toolsMenu.setActive(visible);
        if (!visible) {
            closeTrackedMenu(state);
            return;
        }

        final int toolSlot = toolsW;
        final int menuPad = 1;
        final int toolGap = 2;
        final boolean editTools = state.canEdit;
        final int toolCount = editTools ? 10 : 2;
        final int toolButtonBorder = withAlpha(ModColors.TEXT_MUTED, 210);
        int menuW = menuPad * 2 + toolSlot;
        int menuH = menuPad * 2 + toolCount * toolSlot + (toolCount - 1) * toolGap;
        int menuX = canvasX + toolsX - 1;
        int menuY = CANVAS_Y + topY + headerH + 6;

        toolsMenu.addWidget(panel(menuX, menuY, menuW, menuH, withAlpha(ModColors.SURFACE_BASE, 244), ModColors.BORDER_ACCENT));

        int slotX = menuX + menuPad;
        int y = menuY + menuPad;
        if (!editTools) {
            addReadOnlyTools(toolsMenu, state, refresh, slotX, y, toolSlot, toolGap, toolButtonBorder);
            rememberBounds(state, menuX, menuY, menuW, menuH);
            return;
        }

        ToolMenuRows rows = ToolMenuRows.at(toolsMenu, slotX, y, toolSlot, toolGap, toolButtonBorder);
        addEditRows(rows, state, player, refresh);
        ToolMenuThemeButton.add(toolsMenu, state, refresh, slotX, rows.y(), toolSlot, toolButtonBorder);

        state.toolsGridSizeMenuOpen = false;
        state.toolsGridOpacityMenuOpen = false;
        rememberBounds(state, menuX, menuY, menuW, menuH);
    }

    private static void addEditRows(ToolMenuRows rows, TabletUiState state, Player player, Runnable refresh) {
        CanvasToolRows.grid(rows, state.gridEnabled, () -> {
                    state.gridEnabled = !state.gridEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool grid toggle enabled={}", state.gridEnabled);
                    refresh.run();
                });

        CanvasToolRows.snap(rows, state.gridSnapLocked, () -> {
                    state.gridSnapLocked = !state.gridSnapLocked;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool snap-to-grid enabled={}", state.gridSnapLocked);
                    refresh.run();
                });

        CanvasToolRows.centerX(rows, state.centerSnapXEnabled, () -> {
                    int selectionCount = CanvasRenderer.totalCanvasSelectionCount(state);
                    if (selectionCount > 0) {
                        CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
                        QuestsAndStuffMod.debugLog("[QnS:UI] tool align-selected-vertical-center count={}", CanvasRenderer.totalCanvasSelectionCount(state));
                        refresh.run();
                        return;
                    }
                    state.centerSnapXEnabled = !state.centerSnapXEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-vertical-center-guide enabled={}", state.centerSnapXEnabled);
                    refresh.run();
                });

        CanvasToolRows.centerY(rows, state.centerSnapYEnabled, () -> {
                    int selectionCount = CanvasRenderer.totalCanvasSelectionCount(state);
                    if (selectionCount > 0) {
                        CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
                        QuestsAndStuffMod.debugLog("[QnS:UI] tool align-selected-horizontal-center count={}", CanvasRenderer.totalCanvasSelectionCount(state));
                        refresh.run();
                        return;
                    }
                    state.centerSnapYEnabled = !state.centerSnapYEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-horizontal-center-guide enabled={}", state.centerSnapYEnabled);
                    refresh.run();
                });

        CanvasToolRows.objectSnap(rows, state.objectSnapEnabled, () -> {
                    state.objectSnapEnabled = !state.objectSnapEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool element-guides enabled={}", state.objectSnapEnabled);
                    refresh.run();
                });

        CanvasToolRows.gridOpacity(rows, state.gridOpacityPercent, rightClick -> {
                    int next = cyclePercent(state.gridOpacityPercent, toolPercentStep(), rightClick);
                    applyGridOpacityPercent(state, next);
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool grid-opacity percent={}", state.gridOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.backgroundOpacity(rows, state.canvasBgOpacityPercent, rightClick -> {
                    int next = cyclePercent(state.canvasBgOpacityPercent, toolPercentStep(), rightClick);
                    applyCanvasBgOpacityPercent(state, next);
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-bg-opacity percent={}", state.canvasBgOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.canvasLock(rows, state.gridCanvasLocked, () -> {
                    state.gridCanvasLocked = !state.gridCanvasLocked;
                    if (state.gridCanvasLocked) {
                        state.canvasOffsetX = 0;
                        state.canvasOffsetY = 0;
                    }
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool lock-grid enabled={}", state.gridCanvasLocked);
                    refresh.run();
                });

        CanvasToolRows.splitterLock(rows, state.chapterSplitterLocked, () -> {
                    state.chapterSplitterLocked = !state.chapterSplitterLocked;
                    if (state.chapterSplitterLocked) {
                        state.draggingChapterSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool splitter-lock enabled={} width={}", state.chapterSplitterLocked, chapterPanelWidth(state));
                    refresh.run();
                });
    }

    private static void addReadOnlyTools(WidgetGroup menu, TabletUiState state, Runnable refresh, int x, int y, int toolSlot, int toolGap, int border) {
        addToggle(menu, x, y, toolSlot, border, state.chapterSplitterLocked ? "lock" : "unlock",
                state.chapterSplitterLocked ? ModColors.ERROR : ModColors.SUCCESS,
                !state.chapterSplitterLocked,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.lock_separator"),
                        Component.translatable(state.chapterSplitterLocked ? "ui.questsandstuff.tools.separator_state_locked" : "ui.questsandstuff.tools.separator_state_unlocked")
                },
                () -> {
                    state.chapterSplitterLocked = !state.chapterSplitterLocked;
                    if (state.chapterSplitterLocked) {
                        state.draggingChapterSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    refresh.run();
                });
        ToolMenuThemeButton.add(menu, state, refresh, x, y + toolSlot + toolGap, toolSlot, border);
    }

    private static void rememberBounds(TabletUiState state, int menuX, int menuY, int menuW, int menuH) {
        state.toolsMenuX = menuX;
        state.toolsMenuY = menuY;
        state.toolsMenuW = menuW;
        state.toolsMenuH = menuH;
    }

    private static void closeTrackedMenu(TabletUiState state) {
        state.toolsMenuW = 0;
        state.toolsMenuH = 0;
        state.toolsGridSizeMenuOpen = false;
        state.toolsGridOpacityMenuOpen = false;
    }
}
