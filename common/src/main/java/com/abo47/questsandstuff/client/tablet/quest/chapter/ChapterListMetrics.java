package com.abo47.questsandstuff.client.tablet.quest.chapter;

import java.util.ArrayList;
import java.util.List;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

final class ChapterListMetrics {
    private static final int COLLAPSED_TILE_W = 28;

    private ChapterListMetrics() {
    }

    static List<String> filteredGroups(List<String> groups, String queryText) {
        return filteredGroups(groups, queryText, true);
    }

    static List<String> filteredGroups(List<String> groups, String queryText, boolean canEdit) {
        String chapterQuery = SearchFilter.normalize(queryText);
        List<String> chapterList = new ArrayList<>();
        for (String chapter : groups) {
            if (TabletUiFactory.DRAFT_CHAPTER.equals(chapter)) {
                continue;
            }
            if (!canEdit && ClientQuestStateFacade.chapterHiddenPreview(chapter)) {
                continue;
            }
            if (!SearchFilter.matches(chapterQuery, chapter)) {
                continue;
            }
            chapterList.add(chapter);
        }
        return chapterList;
    }

    static void addScrollBar(WidgetGroup chapterList, TabletUiState state, Runnable refresh, int trackX, int trackY, int trackH, int totalHeight) {
        int knobH = Math.max(18, (int) ((float) trackH * ((float) trackH / (float) Math.max(trackH, totalHeight))));
        state.chapterPanel.chapterScrollKnobH = knobH;
        chapterList.addWidget(new DragScrollBarWidget(
                trackX,
                trackY,
                DragScrollBarWidget.RESERVED_WIDTH,
                trackH,
                () -> state.chapterPanel.chapterScroll,
                () -> state.chapterPanel.chapterScrollMax,
                () -> knobH,
                value -> state.chapterPanel.chapterScroll = value,
                () -> state.chapterPanel.chapterScrollDragging,
                dragging -> state.chapterPanel.chapterScrollDragging = dragging,
                refresh,
                TabletColors.scrollTrack(state.chapterPanel.chapterScrollDragging),
                TabletColors.scrollThumb(false),
                TabletColors.scrollThumb(true),
                DragScrollBarWidget.WIDTH
        ));
    }

    static void rememberEmpty(TabletUiState state, int listOriginX, int listOriginY, int listW, int listH, int baseCardX, int rowStartY) {
        state.chapterPanel.chapterScroll = 0;
        state.chapterPanel.chapterScrollMax = 0;
        state.chapterPanel.chapterScrollKnobH = 18;
        state.chapterPanel.chapterListOriginX = listOriginX;
        state.chapterPanel.chapterListOriginY = listOriginY;
        state.chapterPanel.chapterListWidth = listW;
        state.chapterPanel.chapterListHeight = listH;
        state.chapterPanel.chapterRowStartY = listOriginY + rowStartY;
        state.chapterPanel.chapterCardHitLeft = listOriginX + baseCardX;
        state.chapterPanel.chapterCardHitRight = listOriginX + Math.max(16, listW - 8) - 3;
        state.chapterPanel.chapterCardHitTop = listOriginY + 6;
        state.chapterPanel.chapterCardHitBottom = listOriginY + listH - 6;
    }

    static void remember(TabletUiState state, int listOriginX, int listOriginY, int listW, int listH, Layout layout, int trackY, int trackH, int rowStartY) {
        state.chapterPanel.chapterListOriginX = listOriginX;
        state.chapterPanel.chapterListOriginY = listOriginY;
        state.chapterPanel.chapterListWidth = listW;
        state.chapterPanel.chapterListHeight = listH;
        state.chapterPanel.chapterScrollTrackX = listOriginX + layout.trackX();
        state.chapterPanel.chapterScrollTrackY = listOriginY + trackY;
        state.chapterPanel.chapterScrollTrackH = trackH;
        state.chapterPanel.chapterScrollKnobH = 18;
        state.chapterPanel.chapterRowStartY = listOriginY + rowStartY;
        state.chapterPanel.chapterCardHitLeft = listOriginX + layout.cardX();
        state.chapterPanel.chapterCardHitRight = listOriginX + layout.cardX() + layout.cardW();
        state.chapterPanel.chapterCardHitTop = listOriginY + 6;
        state.chapterPanel.chapterCardHitBottom = listOriginY + listH - 6;
    }

    record Layout(int trackX, int cardX, int cardW, int iconX) {
        static Layout create(int listW, boolean collapsed, boolean showScrollBar) {
            int trackX;
            int cardX;
            int cardW;
            if (showScrollBar) {
                trackX = listW - DragScrollBarWidget.RESERVED_WIDTH - 2;
                if (collapsed) {
                    cardW = Math.min(COLLAPSED_TILE_W, Math.max(16, trackX - 3));
                    cardX = Math.max(1, (trackX - cardW) / 2);
                } else {
                    cardX = 4;
                    cardW = Math.max(96, trackX - cardX - 3);
                }
            } else {
                trackX = listW + 1;
                cardW = collapsed ? Math.min(COLLAPSED_TILE_W, Math.max(16, listW - 2)) : Math.max(96, listW - 8);
                cardW = Math.min(cardW, Math.max(1, listW - 2));
                cardX = Math.max(1, (listW - cardW) / 2);
            }
            return new Layout(trackX, cardX, cardW, cardX + 2);
        }
    }
}
