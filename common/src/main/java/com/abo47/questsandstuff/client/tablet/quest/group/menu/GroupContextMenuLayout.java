package com.abo47.questsandstuff.client.tablet.quest.group.menu;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorGroupCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public record GroupContextMenuLayout(
        String target,
        boolean hasTarget,
        boolean entityIcon,
        boolean entityVariants,
        int menuW,
        int menuH,
        int menuX,
        int menuY,
        int rowCount
) {
    public static final int ROW_TOP_PAD = 4;
    public static final int ROW_STEP = TabletUiFactory.CONTEXT_ROW_H;

    public static GroupContextMenuLayout resolve(TabletUiState state, int availableW, int availableH) {
        String target = resolveTarget(state);
        boolean hasTarget = target != null && !target.isBlank();
        boolean entityIcon = hasTarget && isEntityChapterIcon(target);
        boolean entityVariants = entityIcon && hasEntityVariants(target);
        int rowCount = rowCount(hasTarget, entityIcon, entityVariants);
        int menuW = width(state, availableW);
        int menuH = height(hasTarget, rowCount);
        int menuX = Math.max(4, Math.min(state.chapterPanel.chapterMenuX, availableW - menuW - 4));
        int menuY = Math.max(4, Math.min(state.chapterPanel.chapterMenuY, availableH - menuH - 4));
        return new GroupContextMenuLayout(target, hasTarget, entityIcon, entityVariants, menuW, menuH, menuX, menuY, rowCount);
    }

    public static int width(TabletUiState state, int maxAvailableWidth) {
        return ContextMenuPanel.CONTEXT_MENU_WIDTH;
    }

    public boolean contains(int x, int y) {
        return x >= menuX && x <= menuX + menuW && y >= menuY && y <= menuY + menuH;
    }

    public int relativeY(int y) {
        return y - menuY;
    }

    public static boolean isContextRowHit(int relY, int rowY) {
        return relY >= rowY && relY < rowY + TabletUiFactory.CONTEXT_ROW_H;
    }

    public static int rowCount(boolean hasTarget, boolean entityIcon, boolean entityVariants) {
        if (!hasTarget) {
            return 1;
        }
        int count = 14;
        if (entityVariants) {
            count++;
        }
        if (entityIcon) {
            count++;
        }
        return count;
    }

    public static String deleteKey(String target) {
        return "chapter:delete:" + target;
    }

    public static String removeIconKey(String target) {
        return "chapter:remove_icon:" + target;
    }

    public static String removeBackgroundKey(String target) {
        return "chapter:remove_bg:" + target;
    }

    public static boolean isEntityChapterIcon(String target) {
        return EntityIconControls.isEntityIcon(chapterIcon(target));
    }

    public static boolean hasEntityVariants(String target) {
        return EntityIconControls.hasVariants(chapterIcon(target));
    }

    public static String chapterIcon(String target) {
        String icon = ClientQuestCache.groupIcon(target);
        return icon == null ? "" : icon;
    }

    private static String resolveTarget(TabletUiState state) {
        return state.chapterPanel.chapterMenuTarget.isBlank() ? EditorGroupCommandClient.selectedGroupName(state) : state.chapterPanel.chapterMenuTarget;
    }

    private static int height(boolean hasTarget, int rowCount) {
        int promotedCount = hasTarget ? 3 : 0;
        int rowActionCount = Math.max(0, rowCount - promotedCount);
        return ContextMenuPanel.heightForCounts(promotedCount, rowActionCount, rowActionCount);
    }

}
