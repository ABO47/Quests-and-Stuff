package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class ChapterListRenderer {
    private ChapterListRenderer() {
    }

    static void rebuild(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh) {
        chapterList.clearAllWidgets();
        List<String> groups = ClientQuestStateFacade.chapterOrder();
        int listH = chapterList.getSize().height;
        int listW = chapterList.getSize().width;
        boolean collapsed = state.chapterPanel.chapterPanelCollapsed || listW <= 54;
        int listOriginX = chapterList.getSelfPositionX();
        int listOriginY = chapterList.getSelfPositionY();
        int baseCardX = 4;
        int rowStartY = collapsed ? 0 : 8;
        if (groups.isEmpty() && !TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)) {
            ChapterListMetrics.rememberEmpty(state, listOriginX, listOriginY, listW, listH, baseCardX, rowStartY);
            if (!collapsed) {
                chapterList.addWidget(TabletUiFactory.label(8, 8, ChapterRenameActions.tr("ui.questsandstuff.chapter.none"), TabletColors.TEXT_MUTED));
            }
            return;
        }

        int trackY = 4;
        int trackH = listH - 8;
        int rowStep = collapsed ? TabletUiFactory.CHAPTER_COLLAPSED_ROW_STEP : TabletUiFactory.CHAPTER_CARD_H + TabletUiFactory.CHAPTER_CARD_GAP;
        List<String> chapterList = ChapterListMetrics.filteredGroups(groups, state.chapterPanel.chapterSearch, state.root.canEdit);

        if (chapterList.isEmpty() && !TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)) {
            ChapterListMetrics.rememberEmpty(state, listOriginX, listOriginY, listW, listH, baseCardX, rowStartY);
            if (!collapsed) {
                String emptyKey = state.chapterPanel.chapterSearch == null || state.chapterPanel.chapterSearch.isBlank()
                        ? "ui.questsandstuff.chapter.none"
                        : "ui.questsandstuff.chapter.none_matching";
                chapterList.addWidget(TabletUiFactory.label(8, 8, ChapterRenameActions.tr(emptyKey), TabletColors.TEXT_MUTED));
            }
            return;
        }

        int baseRows = chapterList.size() + (TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename) ? 1 : 0);
        int totalHeight = (collapsed ? 0 : 16) + baseRows * rowStep;
        state.chapterPanel.chapterScrollMax = Math.max(0, totalHeight - (listH - 8));
        state.chapterPanel.chapterScroll = ScrollMath.clamp(state.chapterPanel.chapterScroll, state.chapterPanel.chapterScrollMax);
        boolean showScrollBar = !collapsed && state.chapterPanel.chapterScrollMax > 0;
        if (collapsed) {
            state.chapterPanel.chapterScrollDragging = false;
        }

        ChapterListMetrics.Layout layout = ChapterListMetrics.Layout.create(listW, collapsed, showScrollBar);
        ChapterListMetrics.remember(state, listOriginX, listOriginY, listW, listH, layout, trackY, trackH, rowStartY);

        int y = rowStartY - state.chapterPanel.chapterScroll;
        int ghostInsertIndex = Math.max(0, Math.min(chapterList.size(), state.chapterPanel.chapterDragTargetIndex));
        boolean showGhostCard = state.chapterPanel.chapterDragActive && !state.chapterPanel.chapterDragName.isBlank();

        for (int groupIndex = 0; groupIndex < chapterList.size(); groupIndex++) {
            if (showGhostCard && groupIndex == ghostInsertIndex) {
                y = ChapterRowRenderer.addGhostIfVisible(chapterList, state.chapterPanel.chapterDragName, layout.cardX(), y, layout.cardW(), listH, rowStep);
            }

            String group = chapterList.get(groupIndex);
            if (y >= listH || y + TabletUiFactory.CHAPTER_CARD_H <= 0) {
                y += rowStep;
                continue;
            }

            if (!state.canvas.pendingChapterRename.equals(group)) {
                ChapterRowRenderer.addChapterRow(chapterList, state, refresh, group, y, layout, collapsed);
            } else {
                ChapterRowRenderer.addRenameRow(chapterList, state, player, refresh, group, y, layout);
            }
            y += rowStep;
        }

        if (showGhostCard && ghostInsertIndex == chapterList.size()) {
            y = ChapterRowRenderer.addGhostIfVisible(chapterList, state.chapterPanel.chapterDragName, layout.cardX(), y, layout.cardW(), listH, rowStep);
        }

        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)) {
            if (y < listH && y + TabletUiFactory.CHAPTER_CARD_H > 0) {
                ChapterRowRenderer.addDraftRow(chapterList, state, player, refresh, y, layout);
            }
        }

        if (showScrollBar) {
            ChapterListMetrics.addScrollBar(chapterList, state, refresh, layout.trackX(), trackY, trackH, totalHeight);
        } else {
            state.chapterPanel.chapterScrollKnobH = 18;
        }

    }
}
