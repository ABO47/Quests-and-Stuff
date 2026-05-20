package com.abo47.questsandstuff.client.tablet.tools;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.AnchoredMenuRevealWidget;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.layout.TabletResizeCursor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.cyclePercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.toolPercentStep;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.tools.TabletToolButtons.addToggle;

final class QuestDetailsToolsMenu {
    private QuestDetailsToolsMenu() {
    }

    static void rebuild(WidgetGroup toolsMenu, TabletUiState state, Player player, Runnable refresh, String questId, int buttonX, int buttonY, int headerH, int toolSlot) {
        if (!ToolMenuAnimation.questDetailsVisible(state)) {
            return;
        }
        final int menuPad = 1;
        final int toolGap = 2;
        final boolean editTools = state.canEdit && state.questDetailsEditMode;
        final int toolCount = editTools ? 11 : 3;
        final int toolButtonBorder = withAlpha(ModColors.TEXT_MUTED, 210);
        int menuW = menuPad * 2 + toolSlot;
        int menuH = menuPad * 2 + toolCount * toolSlot + (toolCount - 1) * toolGap;
        int menuX = buttonX - 1;
        int menuY = buttonY + headerH + 6;
        int slotX = menuPad;
        int y = menuPad;
        WidgetGroup menu = new WidgetGroup(menuX, menuY, menuW, menuH);
        menu.setActive(ToolMenuAnimation.questDetailsInteractive(state));

        menu.addWidget(panel(0, 0, menuW, menuH, withAlpha(ModColors.SURFACE_BASE, 244), ModColors.BORDER_ACCENT));

        if (!editTools) {
            addReadOnlyTools(menu, state, player, questId, refresh, slotX, y, toolSlot, toolGap, toolButtonBorder);
            addAnimatedMenu(toolsMenu, state, menu);
            return;
        }

        ToolMenuRows rows = ToolMenuRows.at(menu, slotX, y, toolSlot, toolGap, toolButtonBorder);
        addEditRows(rows, state, player, refresh, questId);

        addQuestAutoClaimToggle(menu, state, player, refresh, questId, slotX, rows.y(), toolSlot, toolButtonBorder);
        rows.advancePastCustomRow();

        ToolMenuThemeButton.add(menu, state, refresh, slotX, rows.y(), toolSlot, toolButtonBorder);
        addAnimatedMenu(toolsMenu, state, menu);
    }

    private static void addEditRows(ToolMenuRows rows, TabletUiState state, Player player, Runnable refresh, String questId) {
        CanvasToolRows.grid(rows, state.questDetailsGridEnabled, () -> {
                    state.questDetailsGridEnabled = !state.questDetailsGridEnabled;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool grid enabled={}", state.questDetailsGridEnabled);
                    refresh.run();
                });

        CanvasToolRows.snap(rows, state.questDetailsGridSnapLocked, () -> {
                    state.questDetailsGridSnapLocked = !state.questDetailsGridSnapLocked;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool snap-to-grid enabled={}", state.questDetailsGridSnapLocked);
                    refresh.run();
                });

        CanvasToolRows.centerX(rows, state.questDetailsCenterSnapXEnabled, () -> {
                    state.questDetailsCenterSnapXEnabled = !state.questDetailsCenterSnapXEnabled;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool vertical-guide enabled={}", state.questDetailsCenterSnapXEnabled);
                    refresh.run();
                });

        CanvasToolRows.centerY(rows, state.questDetailsCenterSnapYEnabled, () -> {
                    state.questDetailsCenterSnapYEnabled = !state.questDetailsCenterSnapYEnabled;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool horizontal-guide enabled={}", state.questDetailsCenterSnapYEnabled);
                    refresh.run();
                });

        CanvasToolRows.objectSnap(rows, state.questDetailsObjectSnapEnabled, () -> {
                    state.questDetailsObjectSnapEnabled = !state.questDetailsObjectSnapEnabled;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool object-guide enabled={}", state.questDetailsObjectSnapEnabled);
                    refresh.run();
                });

        CanvasToolRows.gridOpacity(rows, state.questDetailsGridOpacityPercent, rightClick -> {
                    state.questDetailsGridOpacityPercent = cyclePercent(state.questDetailsGridOpacityPercent, toolPercentStep(), rightClick);
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool grid-opacity percent={}", state.questDetailsGridOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.backgroundOpacity(rows, state.questDetailsCanvasBgOpacityPercent, rightClick -> {
                    state.questDetailsCanvasBgOpacityPercent = cyclePercent(state.questDetailsCanvasBgOpacityPercent, toolPercentStep(), rightClick);
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool bg-opacity percent={}", state.questDetailsCanvasBgOpacityPercent);
                    refresh.run();
                });

        CanvasToolRows.canvasLock(rows, state.questDetailsCanvasLocked, () -> {
                    state.questDetailsCanvasLocked = !state.questDetailsCanvasLocked;
                    QuestDetailsDescriptionModel.saveTools(player, questId, state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool lock-canvas enabled={}", state.questDetailsCanvasLocked);
                    refresh.run();
                });

        CanvasToolRows.splitterLock(rows, state.questDetailsSplitterLocked, () -> {
                    state.questDetailsSplitterLocked = !state.questDetailsSplitterLocked;
                    if (state.questDetailsSplitterLocked) {
                        state.questDetailsDraggingSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details tool splitter-lock enabled={} width={}", state.questDetailsSplitterLocked, state.questDetailsLeftPanelWidth);
                    refresh.run();
                });
    }

    private static void addQuestAutoClaimToggle(WidgetGroup menu, TabletUiState state, Player player, Runnable refresh, String questId, int x, int y, int toolSlot, int border) {
        boolean autoClaim = ClientQuestCache.quest(questId).getBoolean("auto_claim_rewards");
        addToggle(menu, x, y, toolSlot, border, "claim_all",
                autoClaim ? ModColors.SUCCESS : ModColors.ERROR,
                autoClaim,
                new Component[]{
                        QuestVocabulary.component(QuestVocabulary.AUTO_CLAIM_REWARDS),
                        QuestVocabulary.component(autoClaim ? QuestVocabulary.COMMON_ENABLED : QuestVocabulary.COMMON_DISABLED)
                },
                () -> {
                    EditorCommandClient.setQuestAutoClaim(player, questId, !autoClaim);
                    QuestsAndStuffMod.debugLog("[QnS:UI] quest details auto-claim toggle quest={} enabled={}", questId, !autoClaim);
                    refresh.run();
                });
    }

    private static void addReadOnlyTools(WidgetGroup menu, TabletUiState state, Player player, String questId, Runnable refresh, int x, int y, int toolSlot, int toolGap, int border) {
        addToggle(menu, x, y, toolSlot, border, state.questDetailsSplitterLocked ? "lock" : "unlock",
                state.questDetailsSplitterLocked ? ModColors.ERROR : ModColors.SUCCESS,
                !state.questDetailsSplitterLocked,
                new Component[]{
                        Component.translatable("ui.questsandstuff.tools.lock_separator"),
                        Component.translatable(state.questDetailsSplitterLocked ? "ui.questsandstuff.tools.separator_state_locked" : "ui.questsandstuff.tools.separator_state_unlocked")
                },
                () -> {
                    state.questDetailsSplitterLocked = !state.questDetailsSplitterLocked;
                    if (state.questDetailsSplitterLocked) {
                        state.questDetailsDraggingSplitter = false;
                        TabletResizeCursor.update(false);
                    }
                    persistUiState(state);
                    refresh.run();
                });
        y += toolSlot + toolGap;
        addQuestAutoClaimToggle(menu, state, player, refresh, questId, x, y, toolSlot, border);
        y += toolSlot + toolGap;
        ToolMenuThemeButton.add(menu, state, refresh, x, y, toolSlot, border);
    }

    private static void addAnimatedMenu(WidgetGroup toolsMenu, TabletUiState state, WidgetGroup menu) {
        if (!QuestsAndStuffConfig.toolsMenuAnimationsEnabled()) {
            toolsMenu.addWidget(menu);
            return;
        }
        toolsMenu.addWidget(AnchoredMenuRevealWidget.tools(menu, () -> state.toolsMenuAnimationStartMs, () -> ToolMenuAnimation.questDetailsOpening(state)));
    }
}
