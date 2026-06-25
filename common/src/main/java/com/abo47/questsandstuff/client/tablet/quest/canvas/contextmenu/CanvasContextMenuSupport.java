package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;


import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuSystem;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTEXT_ROW_H;

public final class CanvasContextMenuSupport {
    private CanvasContextMenuSupport() {
    }

    public static int contextMenuWidth(List<ContextAction> actions, int maxAvailableWidth) {
        return ContextMenuSystem.CONTEXT_MENU_WIDTH;
    }

    public static int contextMenuWidth(TabletUiState state) {
        return state.contextMenu.contextMenuWidthPx > 0 ? state.contextMenu.contextMenuWidthPx : ContextMenuSystem.CONTEXT_MENU_WIDTH;
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
        int h = state.contextMenu.contextMenuHeightPx > 0 ? state.contextMenu.contextMenuHeightPx : contextMenuHeight(Math.max(1, state.contextMenu.contextMenuRows));
        return x >= state.contextMenu.contextMenuX && y >= state.contextMenu.contextMenuY
                && x <= state.contextMenu.contextMenuX + w
                && y <= state.contextMenu.contextMenuY + h;
    }

    public static boolean clickContextMenu(CanvasViewport canvasViewport, TabletUiState state, int x, int y) {
        if (!ContextMenuState.isOpen(state) || !isContextMenuHit(state, x, y)) {
            return false;
        }
        List<ContextAction> actions = CanvasContextMenuController.buildContextActions(canvasViewport, state);
        if (actions.isEmpty()) {
            ContextMenuState.close(state);
            return true;
        }

        int maxVisibleRows = maxContextVisibleRows(canvasViewport);
        List<ContextAction> promoted = ContextMenuPanel.promotedActions(actions);
        List<ContextAction> rows = ContextMenuPanel.rowActions(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rows.size(), maxVisibleRows);
        int scrollMax = Math.max(0, rows.size() - visibleRows);
        int scroll = ScrollController.clamp(state.contextMenu.contextMenuScroll, scrollMax);
        boolean needsScroll = scrollMax > 0;
        int menuW = contextMenuWidth(actions, canvasViewport.getSize().width);
        int rowWidth = needsScroll ? menuW - 14 : menuW - 8;

        int relX = x - state.contextMenu.contextMenuX;
        int relY = y - state.contextMenu.contextMenuY;
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
            ContextMenuState.setLastClick(state, x, y);
            ContextMenuAnimation.finish(state, ContextMenuAnimation.DEFAULT_KEY);
            action.action().run();
            if (!action.closeAfterClick()) {
                return true;
            }
        }
        ContextMenuState.close(state);
        return true;
    }

    private static boolean handlePromotedClick(List<ContextAction> promoted, int relX, int relY, int menuW, TabletUiState state) {
        if (promoted.isEmpty() || relY < 4 || relY >= 4 + ContextMenuPanel.PROMOTED_BAR_H) {
            return false;
        }
        List<ContextAction> visiblePromoted = ContextMenuPanel.visiblePromotedActions(promoted, menuW);
        int visible = visiblePromoted.size();
        int y = 4 + Math.max(0, (ContextMenuPanel.PROMOTED_BAR_H - ContextMenuPanel.PROMOTED_BUTTON) / 2);
        for (int i = 0; i < visible; i++) {
            int buttonX = ContextMenuPanel.promotedButtonX(menuW, visible, i);
            if (relX < buttonX || relX >= buttonX + ContextMenuPanel.PROMOTED_BUTTON || relY < y || relY >= y + ContextMenuPanel.PROMOTED_BUTTON) {
                continue;
            }
            ContextAction action = visiblePromoted.get(i);
            ContextMenuState.setLastClick(state, state.contextMenu.contextMenuX + relX, state.contextMenu.contextMenuY + relY);
            ContextMenuAnimation.finish(state, ContextMenuAnimation.DEFAULT_KEY);
            action.action().run();
            if (!action.closeAfterClick()) {
                return true;
            }
            ContextMenuState.close(state);
            return true;
        }
        return true;
    }

    public static void scrollContextMenu(TabletUiState state, double wheelDelta) {
        ContextMenuState.scrollByWheel(state, wheelDelta);
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
        for (String candidate : ClientQuestCache.questIds()) {
            if (!questId.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canMoveLayer(CanvasViewport canvasViewport, TabletUiState state, String group, String key, boolean front) {
        if (group == null || group.isBlank() || key == null || key.isBlank()) {
            return false;
        }
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of());
        List<CanvasImageLayer> images = state.canvas.canvasImagesByGroup.getOrDefault(group, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByGroup.getOrDefault(group, List.of());
        List<QuestCardLayout> cards = canvasViewport.cardCache();
        Map<String, QuestCardLayout> byQuestId = new HashMap<>();
        for (QuestCardLayout card : cards) {
            byQuestId.put(card.questId(), card);
        }
        List<String> connectionKeys = ConnectionRenderer.prerequisiteConnectionLayerKeys(state, cards, byQuestId, canvasViewport.getSize().width, canvasViewport.getSize().height);
        List<String> order = CanvasLayerOrdering.normalize(state, group, cards, images, texts, connectionKeys, ecs);
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
