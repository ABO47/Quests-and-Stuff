package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class ChapterListRenderer {
    private ChapterListRenderer() {
    }

    static void rebuild(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh) {
        chapterList.clearAllWidgets();
        List<String> groups = ClientQuestCache.groupOrder();
        int listH = chapterList.getSize().height;
        int listW = chapterList.getSize().width;
        boolean collapsed = state.chapterPanelCollapsed || listW <= 54;
        int listOriginX = chapterList.getSelfPositionX();
        int listOriginY = chapterList.getSelfPositionY();
        int baseCardX = 4;
        int rowStartY = collapsed ? 0 : 8;
        if (groups.isEmpty() && !TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename)) {
            ChapterListMetrics.rememberEmpty(state, listOriginX, listOriginY, listW, listH, baseCardX, rowStartY);
            if (!collapsed) {
                chapterList.addWidget(TabletUiFactory.label(8, 8, ChapterRenameActions.tr("ui.questsandstuff.chapter.none"), ModColors.TEXT_MUTED));
            }
            return;
        }

        int trackY = 4;
        int trackH = listH - 8;
        int rowStep = collapsed ? TabletUiFactory.CHAPTER_COLLAPSED_ROW_STEP : TabletUiFactory.CHAPTER_CARD_H + TabletUiFactory.CHAPTER_CARD_GAP;
        List<String> chapterGroups = ChapterListMetrics.filteredGroups(groups, state.chapterSearch, state.canEdit);

        if (chapterGroups.isEmpty() && !TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename)) {
            ChapterListMetrics.rememberEmpty(state, listOriginX, listOriginY, listW, listH, baseCardX, rowStartY);
            if (!collapsed) {
                String emptyKey = state.chapterSearch == null || state.chapterSearch.isBlank()
                        ? "ui.questsandstuff.chapter.none"
                        : "ui.questsandstuff.chapter.none_matching";
                chapterList.addWidget(TabletUiFactory.label(8, 8, ChapterRenameActions.tr(emptyKey), ModColors.TEXT_MUTED));
            }
            return;
        }

        int baseRows = chapterGroups.size() + (TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename) ? 1 : 0);
        int totalHeight = (collapsed ? 0 : 16) + baseRows * rowStep;
        state.chapterScrollMax = Math.max(0, totalHeight - (listH - 8));
        state.chapterScroll = ScrollController.clamp(state.chapterScroll, state.chapterScrollMax);
        boolean showScrollBar = !collapsed && state.chapterScrollMax > 0;
        if (collapsed) {
            state.chapterScrollDragging = false;
        }

        ChapterListMetrics.Layout layout = ChapterListMetrics.Layout.create(listW, collapsed, showScrollBar);
        ChapterListMetrics.remember(state, listOriginX, listOriginY, listW, listH, layout, trackY, trackH, rowStartY);

        int y = rowStartY - state.chapterScroll;
        int ghostInsertIndex = Math.max(0, Math.min(chapterGroups.size(), state.chapterDragTargetIndex));
        boolean showGhostCard = state.chapterDragActive && !state.chapterDragName.isBlank();

        for (int groupIndex = 0; groupIndex < chapterGroups.size(); groupIndex++) {
            if (showGhostCard && groupIndex == ghostInsertIndex) {
                y = ChapterRowRenderer.addGhostIfVisible(chapterList, state.chapterDragName, layout.cardX(), y, layout.cardW(), listH, rowStep);
            }

            String group = chapterGroups.get(groupIndex);
            if (y >= listH || y + TabletUiFactory.CHAPTER_CARD_H <= 0) {
                y += rowStep;
                continue;
            }

            if (!state.pendingChapterRename.equals(group)) {
                ChapterRowRenderer.addChapterRow(chapterList, state, refresh, group, y, layout, collapsed);
            } else {
                ChapterRowRenderer.addRenameRow(chapterList, state, player, refresh, group, y, layout);
            }
            y += rowStep;
        }

        if (showGhostCard && ghostInsertIndex == chapterGroups.size()) {
            y = ChapterRowRenderer.addGhostIfVisible(chapterList, state.chapterDragName, layout.cardX(), y, layout.cardW(), listH, rowStep);
        }

        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename)) {
            if (y < listH && y + TabletUiFactory.CHAPTER_CARD_H > 0) {
                ChapterRowRenderer.addDraftRow(chapterList, state, player, refresh, y, layout);
            }
        }

        if (showScrollBar) {
            ChapterListMetrics.addScrollBar(chapterList, state, refresh, layout.trackX(), trackY, trackH, totalHeight);
        } else {
            state.chapterScrollKnobH = 18;
        }

    }
}
