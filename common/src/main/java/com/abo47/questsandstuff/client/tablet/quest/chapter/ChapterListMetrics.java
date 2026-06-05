package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.ArrayList;
import java.util.List;

final class ChapterListMetrics {
    private static final int COLLAPSED_TILE_W = 28;

    private ChapterListMetrics() {
    }

    static List<String> filteredGroups(List<String> groups, String queryText) {
        return filteredGroups(groups, queryText, true);
    }

    static List<String> filteredGroups(List<String> groups, String queryText, boolean canEdit) {
        String chapterQuery = SearchFilter.normalize(queryText);
        List<String> chapterGroups = new ArrayList<>();
        for (String group : groups) {
            if (TabletUiFactory.DRAFT_CHAPTER.equals(group)) {
                continue;
            }
            if (!canEdit && ClientQuestCache.groupHiddenPreview(group)) {
                continue;
            }
            if (!SearchFilter.matches(chapterQuery, group)) {
                continue;
            }
            chapterGroups.add(group);
        }
        return chapterGroups;
    }

    static void addScrollBar(WidgetGroup chapterList, TabletUiState state, Runnable refresh, int trackX, int trackY, int trackH, int totalHeight) {
        int knobH = Math.max(18, (int) ((float) trackH * ((float) trackH / (float) Math.max(trackH, totalHeight))));
        state.chapterScrollKnobH = knobH;
        chapterList.addWidget(new DragScrollBarWidget(
                trackX,
                trackY,
                DragScrollBarWidget.RESERVED_WIDTH,
                trackH,
                () -> state.chapterScroll,
                () -> state.chapterScrollMax,
                () -> knobH,
                value -> state.chapterScroll = value,
                () -> state.chapterScrollDragging,
                dragging -> state.chapterScrollDragging = dragging,
                refresh,
                ModColors.scrollTrack(state.chapterScrollDragging),
                ModColors.scrollThumb(false),
                ModColors.scrollThumb(true),
                DragScrollBarWidget.WIDTH
        ));
    }

    static void rememberEmpty(TabletUiState state, int listOriginX, int listOriginY, int listW, int listH, int baseCardX, int rowStartY) {
        state.chapterScroll = 0;
        state.chapterScrollMax = 0;
        state.chapterScrollKnobH = 18;
        state.chapterListOriginX = listOriginX;
        state.chapterListOriginY = listOriginY;
        state.chapterListWidth = listW;
        state.chapterListHeight = listH;
        state.chapterRowStartY = listOriginY + rowStartY;
        state.chapterCardHitLeft = listOriginX + baseCardX;
        state.chapterCardHitRight = listOriginX + Math.max(16, listW - 8) - 3;
        state.chapterCardHitTop = listOriginY + 6;
        state.chapterCardHitBottom = listOriginY + listH - 6;
    }

    static void remember(TabletUiState state, int listOriginX, int listOriginY, int listW, int listH, Layout layout, int trackY, int trackH, int rowStartY) {
        state.chapterListOriginX = listOriginX;
        state.chapterListOriginY = listOriginY;
        state.chapterListWidth = listW;
        state.chapterListHeight = listH;
        state.chapterScrollTrackX = listOriginX + layout.trackX();
        state.chapterScrollTrackY = listOriginY + trackY;
        state.chapterScrollTrackH = trackH;
        state.chapterScrollKnobH = 18;
        state.chapterRowStartY = listOriginY + rowStartY;
        state.chapterCardHitLeft = listOriginX + layout.cardX();
        state.chapterCardHitRight = listOriginX + layout.cardX() + layout.cardW();
        state.chapterCardHitTop = listOriginY + 6;
        state.chapterCardHitBottom = listOriginY + listH - 6;
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
