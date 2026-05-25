package com.abo47.questsandstuff.client.canvas.contextmenu;


import com.abo47.questsandstuff.client.canvas.CanvasViewport;

import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTEXT_ROW_H;

public final class CanvasContextMenuSupport {
    private CanvasContextMenuSupport() {
    }

    public static int contextMenuWidth(List<ContextAction> actions, int maxAvailableWidth) {
        List<String> labels = new ArrayList<>();
        for (ContextAction action : ContextMenuPanel.rowActions(actions)) {
            labels.add(action.label());
        }
        return ContextMenuSystem.preferredMenuWidth(labels, 82, Math.max(82, Math.min(156, maxAvailableWidth - 8)));
    }

    public static int contextMenuWidth(TabletUiState state) {
        return state.contextMenuWidthPx > 0 ? state.contextMenuWidthPx : 118;
    }

    public static int contextMenuHeight(int visibleRows) {
        return ContextMenuPanel.heightForRows(visibleRows);
    }

    public static int maxContextVisibleRows(CanvasViewport canvasViewport) {
        int usable = Math.max(CONTEXT_ROW_H, canvasViewport.getSize().height - 16);
        return Math.max(3, Math.min(10, usable / CONTEXT_ROW_H));
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        int w = contextMenuWidth(state);
        int h = state.contextMenuHeightPx > 0 ? state.contextMenuHeightPx : contextMenuHeight(Math.max(1, state.contextMenuRows));
        return x >= state.contextMenuX && y >= state.contextMenuY
                && x <= state.contextMenuX + w
                && y <= state.contextMenuY + h;
    }

    public static boolean clickContextMenu(CanvasViewport canvasViewport, TabletUiState state, int x, int y) {
        if (!state.contextMenuOpen || !isContextMenuHit(state, x, y)) {
            return false;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            state.contextMenuOpen = false;
            state.contextQuestCompletionSoundMenuOpen = false;
            return true;
        }

        int maxVisibleRows = maxContextVisibleRows(canvasViewport);
        List<ContextAction> promoted = ContextMenuPanel.promotedActions(actions);
        List<ContextAction> rows = ContextMenuPanel.rowActions(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rows.size(), maxVisibleRows);
        int scrollMax = Math.max(0, rows.size() - visibleRows);
        int scroll = ScrollController.clamp(state.contextMenuScroll, scrollMax);
        boolean needsScroll = scrollMax > 0;
        int menuW = contextMenuWidth(actions, canvasViewport.getSize().width);
        int rowWidth = needsScroll ? menuW - 14 : menuW - 8;

        int relX = x - state.contextMenuX;
        int relY = y - state.contextMenuY;
        if (handlePromotedClick(promoted, relX, relY, menuW, state)) {
            return true;
        }

        int rowTop = ContextMenuPanel.rowTop(promoted);
        if (relY < rowTop || relY >= rowTop + visibleRows * CONTEXT_ROW_H) {
            return true;
        }
        if (relX < 4 || relX > 4 + rowWidth) {
            return true;
        }

        int row = (relY - rowTop) / CONTEXT_ROW_H;
        int actionIndex = scroll + row;
        if (actionIndex >= 0 && actionIndex < rows.size()) {
            ContextAction action = rows.get(actionIndex);
            ContextMenuAnimation.finish(state, ContextMenuAnimation.DEFAULT_KEY);
            action.action().run();
            if (!action.closeAfterClick()) {
                return true;
            }
        }
        state.contextMenuOpen = false;
        state.contextMenuRows = 0;
        state.contextMenuScroll = 0;
        state.contextMenuScrollMax = 0;
        state.contextDeleteConfirmKey = "";
        state.contextQuestCompletionSoundMenuOpen = false;
        return true;
    }

    private static boolean handlePromotedClick(List<ContextAction> promoted, int relX, int relY, int menuW, TabletUiState state) {
        if (promoted.isEmpty() || relY < 4 || relY >= 4 + ContextMenuPanel.PROMOTED_BAR_H) {
            return false;
        }
        int visible = ContextMenuPanel.visiblePromotedCount(promoted, menuW);
        int y = 4 + Math.max(0, (ContextMenuPanel.PROMOTED_BAR_H - ContextMenuPanel.PROMOTED_BUTTON) / 2);
        for (int i = 0; i < visible; i++) {
            int buttonX = ContextMenuPanel.promotedButtonX(menuW, visible, i);
            if (relX < buttonX || relX >= buttonX + ContextMenuPanel.PROMOTED_BUTTON || relY < y || relY >= y + ContextMenuPanel.PROMOTED_BUTTON) {
                continue;
            }
            ContextAction action = promoted.get(i);
            ContextMenuAnimation.finish(state, ContextMenuAnimation.DEFAULT_KEY);
            action.action().run();
            if (!action.closeAfterClick()) {
                return true;
            }
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextMenuScroll = 0;
            state.contextMenuScrollMax = 0;
            state.contextDeleteConfirmKey = "";
            state.contextQuestCompletionSoundMenuOpen = false;
            return true;
        }
        return true;
    }

    public static void scrollContextMenu(TabletUiState state, double wheelDelta) {
        if (!state.contextMenuOpen || state.contextMenuScrollMax <= 0 || wheelDelta == 0) {
            return;
        }
        int step = wheelDelta > 0 ? -1 : 1;
        state.contextMenuScroll = ScrollController.clamp(state.contextMenuScroll + step, state.contextMenuScrollMax);
    }

    public static boolean canCopyContext(CanvasViewport canvasViewport, TabletUiState state) {
        return CanvasClipboardController.canCopyContext(canvasViewport, state);
    }

    public static void copyContextToClipboard(CanvasViewport canvasViewport, TabletUiState state) {
        CanvasClipboardController.copyContextToClipboard(canvasViewport, state);
    }

    public static void pasteClipboard(Player player, TabletUiState state) {
        CanvasClipboardController.pasteAtContext(player, state);
    }

    public static boolean hasOtherQuest(CanvasViewport canvasViewport, String questId) {
        if (questId == null || questId.isBlank()) {
            return false;
        }
        for (QuestCardLayout card : canvasViewport.cardCache()) {
            if (!questId.equals(card.questId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canMoveLayer(CanvasViewport canvasViewport, TabletUiState state, String group, String key, boolean front) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return false;
        }
        List<CanvasImageLayer> images = state.canvasImagesByGroup.getOrDefault(group, List.of());
        List<CanvasTextLayer> texts = state.canvasTextsByGroup.getOrDefault(group, List.of());
        List<String> order = CanvasLayerOrdering.normalize(state, group, canvasViewport.cardCache(), images, texts);
        int index = order.indexOf(key);
        if (index < 0 || order.size() <= 1) {
            return false;
        }
        return front ? index < order.size() - 1 : index > 0;
    }

    public static String readableQuestTitle(String questId) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        String title = quest.getString("title");
        String value = title == null || title.isBlank() ? questId : title;
        if (value == null) {
            return "";
        }
        return value.length() <= 18 ? value : value.substring(0, 15) + "...";
    }
}
