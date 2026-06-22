package com.abo47.questsandstuff.client.tablet.quest.tools;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.animation.AnchoredMenuRevealWidget;
import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
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
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class MainCanvasToolsMenu {
    private MainCanvasToolsMenu() {
    }

    static void rebuild(WidgetGroup toolsMenu, TabletUiState state, Player player, Runnable refresh, int canvasX, int toolsX, int topY, int headerH, int toolsW) {
        toolsMenu.clearAllWidgets();
        boolean visible = ToolMenuAnimation.mainVisible(state);
        toolsMenu.setVisible(visible);
        toolsMenu.setActive(ToolMenuAnimation.mainInteractive(state));
        if (!visible) {
            ToolMenuAnimation.finishMain(state);
            return;
        }

        final int toolSlot = toolsW;
        final int menuPad = 1;
        final int toolGap = 2;
        final boolean editTools = state.root.canEdit;
        final int toolCount = editTools ? 10 : 2;
        final int toolButtonBorder = withAlpha(ModColors.TEXT_MUTED, 210);
        int menuW = menuPad * 2 + toolSlot;
        int menuH = menuPad * 2 + toolCount * toolSlot + (toolCount - 1) * toolGap;
        int menuX = canvasX + toolsX - 1;
        int menuY = CANVAS_Y + topY + headerH + 6;
        WidgetGroup menu = new WidgetGroup(menuX, menuY, menuW, menuH);
        menu.setActive(ToolMenuAnimation.mainInteractive(state));

        menu.addWidget(panel(0, 0, menuW, menuH, withAlpha(ModColors.SURFACE_BASE, 244), ModColors.BORDER_ACCENT));

        int slotX = menuPad;
        int y = menuPad;
        if (!editTools) {
            ToolMenuRows rows = ToolMenuRows.at(menu, slotX, y, toolSlot, toolGap, toolButtonBorder);
            addReadOnlyRows(rows, state, refresh);
            addRewardRows(rows, refresh);
            addAnimatedMenu(toolsMenu, state, menu);
            rememberBounds(state, menuX, menuY, menuW, menuH);
            return;
        }

        ToolMenuRows rows = ToolMenuRows.at(menu, slotX, y, toolSlot, toolGap, toolButtonBorder);
        addEditRows(rows, state, player, refresh);
        addRewardRows(rows, refresh);

        state.canvas.toolsGridSizeMenuOpen = false;
        state.canvas.toolsGridOpacityMenuOpen = false;
        addAnimatedMenu(toolsMenu, state, menu);
        rememberBounds(state, menuX, menuY, menuW, menuH);
    }

    private static void addEditRows(ToolMenuRows rows, TabletUiState state, Player player, Runnable refresh) {
        CanvasToolRows.grid(rows, state.canvas.gridEnabled, () -> {
                    state.canvas.gridEnabled = !state.canvas.gridEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool grid toggle enabled={}", state.canvas.gridEnabled);
                    refresh.run();
                });

        CanvasToolRows.gridOpacity(rows, state.canvas.gridOpacityPercent, rightClick -> {
                    int next = cyclePercent(state.canvas.gridOpacityPercent, toolPercentStep(), rightClick);
                    applyGridOpacityPercent(state, next);
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool grid-opacity percent={}", state.canvas.gridOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.snap(rows, state.canvas.gridSnapLocked, () -> {
                    state.canvas.gridSnapLocked = !state.canvas.gridSnapLocked;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool snap-to-grid enabled={}", state.canvas.gridSnapLocked);
                    refresh.run();
                });

        CanvasToolRows.objectSnap(rows, state.canvas.objectSnapEnabled, () -> {
                    state.canvas.objectSnapEnabled = !state.canvas.objectSnapEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool element-guides enabled={}", state.canvas.objectSnapEnabled);
                    refresh.run();
                });

        CanvasToolRows.centerX(rows, state.canvas.centerSnapXEnabled, () -> {
                    int selectionCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
                    if (selectionCount > 0) {
                        CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, true);
                        QuestsAndStuffMod.debugLog("[QnS:UI] tool align-selected-vertical-center count={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
                        refresh.run();
                        return;
                    }
                    state.canvas.centerSnapXEnabled = !state.canvas.centerSnapXEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-vertical-center-guide enabled={}", state.canvas.centerSnapXEnabled);
                    refresh.run();
                });

        CanvasToolRows.centerY(rows, state.canvas.centerSnapYEnabled, () -> {
                    int selectionCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
                    if (selectionCount > 0) {
                        CanvasSelectionActions.alignSelectedToCanvasCenter(player, state, false);
                        QuestsAndStuffMod.debugLog("[QnS:UI] tool align-selected-horizontal-center count={}", CanvasSelectionActions.totalCanvasSelectionCount(state));
                        refresh.run();
                        return;
                    }
                    state.canvas.centerSnapYEnabled = !state.canvas.centerSnapYEnabled;
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-horizontal-center-guide enabled={}", state.canvas.centerSnapYEnabled);
                    refresh.run();
                });

        CanvasToolRows.backgroundOpacity(rows, state.canvas.canvasBgOpacityPercent, rightClick -> {
                    int next = cyclePercent(state.canvas.canvasBgOpacityPercent, toolPercentStep(), rightClick);
                    applyCanvasBgOpacityPercent(state, next);
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool canvas-bg-opacity percent={}", state.canvas.canvasBgOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.canvasLock(rows, state.canvas.gridCanvasLocked, () -> {
                    state.canvas.gridCanvasLocked = !state.canvas.gridCanvasLocked;
                    if (state.canvas.gridCanvasLocked) {
                        CanvasCameraController.setOffset(state, 0, 0, false);
                    }
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool lock-canvas enabled={}", state.canvas.gridCanvasLocked);
                    refresh.run();
                });

        CanvasToolRows.splitterLock(rows, state.chapterPanel.chapterSplitterLocked, () -> {
                    state.chapterPanel.chapterSplitterLocked = !state.chapterPanel.chapterSplitterLocked;
                    if (state.chapterPanel.chapterSplitterLocked) {
                        state.canvas.draggingChapterSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] tool splitter-lock enabled={} width={}", state.chapterPanel.chapterSplitterLocked, chapterPanelWidth(state));
                    refresh.run();
                });
    }

    private static void addRewardRows(ToolMenuRows rows, Runnable refresh) {
        boolean autoClaim = QuestsAndStuffConfig.autoClaimRewardsEnabled();
        rows.toggle("auto_claim",
                autoClaim ? ModColors.SUCCESS : ModColors.ERROR,
                autoClaim,
                new Component[]{
                        TabletVocabulary.component(QuestVocabulary.AUTO_CLAIM_REWARDS),
                        TabletVocabulary.component(autoClaim ? TabletVocabulary.COMMON_ENABLED : TabletVocabulary.COMMON_DISABLED)
                },
                () -> {
                    boolean next = !QuestsAndStuffConfig.autoClaimRewardsEnabled();
                    QuestsAndStuffConfig.setAutoClaimRewardsEnabled(next);
                    QuestsAndStuffMod.debugLog("[QnS:UI] global auto-claim rewards enabled={}", next);
                    refresh.run();
                });
    }

    private static void addReadOnlyRows(ToolMenuRows rows, TabletUiState state, Runnable refresh) {
        rows.toggle(state.chapterPanel.chapterSplitterLocked ? "lock_separator" : "unlock_separator",
                state.chapterPanel.chapterSplitterLocked ? ModColors.ERROR : ModColors.SUCCESS,
                !state.chapterPanel.chapterSplitterLocked,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.lock_separator"),
                        Component.translatable(state.chapterPanel.chapterSplitterLocked ? "ui.questsandstuff.tools.separator_state_locked" : "ui.questsandstuff.tools.separator_state_unlocked")
                },
                () -> {
                    state.chapterPanel.chapterSplitterLocked = !state.chapterPanel.chapterSplitterLocked;
                    if (state.chapterPanel.chapterSplitterLocked) {
                        state.canvas.draggingChapterSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    refresh.run();
                });
    }

    private static void rememberBounds(TabletUiState state, int menuX, int menuY, int menuW, int menuH) {
        state.canvas.toolsMenuX = menuX;
        state.canvas.toolsMenuY = menuY;
        state.canvas.toolsMenuW = menuW;
        state.canvas.toolsMenuH = menuH;
    }

    private static void addAnimatedMenu(WidgetGroup toolsMenu, TabletUiState state, WidgetGroup menu) {
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            toolsMenu.addWidget(menu);
            return;
        }
        toolsMenu.addWidget(AnchoredMenuRevealWidget.tools(menu, () -> state.canvas.toolsMenuAnimationStartMs, () -> ToolMenuAnimation.mainOpening(state)));
    }
}
