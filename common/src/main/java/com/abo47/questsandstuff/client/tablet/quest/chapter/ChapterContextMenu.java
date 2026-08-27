package com.abo47.questsandstuff.client.tablet.quest.chapter;

import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuRows;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;

public final class ChapterContextMenu {
    private ChapterContextMenu() {
    }

    public static void rebuild(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        overlay.clearAllWidgets();
        if (!state.chapterPanel.chapterMenuOpen) {
            return;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, overlay.getSize().width, overlay.getSize().height, player, refresh);
        state.chapterPanel.chapterMenuX = layout.menuX();
        state.chapterPanel.chapterMenuY = layout.menuY();
        List<ContextAction> actions = ChapterContextMenuRows.actions(layout, state, player, refresh);
        overlay.addWidget(ContextMenuPanel.build(
                layout.menuX(),
                layout.menuY(),
                layout.menuW(),
                actions,
                0,
                ContextMenuPanel.rowActionCount(actions),
                TabletColors.BORDER_BASE,
                state,
                action -> {
                    if (action.closeAfterClick()) {
                        state.chapterPanel.chapterMenuOpen = false;
                    }
                    refresh.run();
                },
                ContextMenuAnimationBridge.CHAPTER_KEY
        ));
        if (state.chapterPanel.modeEditorOpen) {
            int w = 240;
            int h = 116;
            int x = Math.max(4, (overlay.getSizeWidth() - w) / 2);
            int y = Math.max(4, (overlay.getSizeHeight() - h) / 2);
            String mode = state.chapterPanel.modeEditorMode;
            boolean tile = "tile".equals(mode);
            String title = tile ? "ui.questsandstuff.skin.mode_tile_size" : "ui.questsandstuff.skin.mode_hrstretch";
            String leftKey = tile ? "ui.questsandstuff.skin.tile_size_w" : "ui.questsandstuff.skin.hrstretch_left";
            String rightKey = tile ? "ui.questsandstuff.skin.tile_size_h" : "ui.questsandstuff.skin.hrstretch_right";
            WidgetGroup popup = com.abo47.questsandstuff.client.tablet.controls.TwoFieldEditor.build(state, x, y, w, h, title, leftKey, rightKey,
                    state.chapterPanel.modeEditorLeft, state.chapterPanel.modeEditorRight,
                    (l, r) -> {
                        EditorChapterCommandClient.runChapterAction(player, state, "set_background", state.chapterPanel.modeEditorTarget,
                                new com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride(mode, l, r, state.chapterPanel.modeEditorPath).encode(), 0);
                        state.chapterPanel.modeEditorOpen = false;
                        refresh.run();
                    },
                    () -> {
                        state.chapterPanel.modeEditorOpen = false;
                        refresh.run();
                    });
            overlay.addWidget(popup);
        }
    }

    public static int width(TabletUiState state, int maxAvailableWidth) {
        return ChapterContextMenuLayout.width(state, maxAvailableWidth);
    }

    public static boolean click(TabletUiState state, Player player, Runnable refresh, int x, int y) {
        if (!state.chapterPanel.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletStateQueries.rootWidth(state), TabletStateQueries.rootHeight(state), player, refresh);
        return ChapterContextMenuRows.click(layout, state, player, refresh, x, y);
    }
}
