package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class TabletLayout {
    static final int ROOT_W = 544;
    static final int ROOT_H = 352;
    static final int PAD = 16;
    static final int PAD_Y = 7;
    static final int GAP = 8;
    static final int BODY_X = PAD;
    static final int BODY_Y = PAD_Y;
    static final int BODY_W = ROOT_W - PAD * 2;
    static final int BODY_H = ROOT_H - BODY_Y - PAD_Y;

    static final int CHAPTER_W = 168;
    static final int CHAPTER_W_MIN = 44;
    static final int CHAPTER_W_ICON = 26;
    static final int CHAPTER_W_MAX = 248;
    static final int CHAPTER_W_ICON_SNAP = 56;
    static final int SPLITTER_W = GAP;
    static final int CANVAS_W = BODY_W - CHAPTER_W - GAP;
    static final int CHAPTER_CARD_H = 32;
    static final int CHAPTER_CARD_GAP = 8;
    static final int CHAPTER_COLLAPSED_ROW_STEP = 30;
    static final String DRAFT_CHAPTER = "__draft_chapter__";
    static final Path ASSETS_ROOT_DIR = Path.of("config", "questsandstuff", "assets");

    static final int CHAPTER_X = BODY_X;
    static final int CHAPTER_Y = BODY_Y;
    static final int CHAPTER_H = BODY_H;

    static final int CANVAS_X = CHAPTER_X + CHAPTER_W + GAP;
    static final int CANVAS_Y = BODY_Y;
    static final int CANVAS_H = BODY_H;
    static final int CANVAS_TOP_H_COMPACT = 20;
    static final int CANVAS_TOP_H_EXPANDED = 20;
    static final int CANVAS_GRID_ROWS = 20;
    static final int CANVAS_GRID_COLS = 18;
    static final int CARD_W = 15;
    static final int CARD_H = 15;
    static final int CONTEXT_ROW_H = 14;
    static final int[] GRID_SIZES = {16};
    static final int[] GRID_OPACITY = {20, 35, 50, 65, 80};
    static final int[] CANVAS_BG_OPACITY = {0, 15, 30, 45, 60, 75, 90, 100};
    static final int[] CANVAS_LIMIT_WIDTH = {132, 164, 196, 228};
    static final int[] CANVAS_LIMIT_HEIGHT = {64, 78, 92, 104};
    static final String[] CANVAS_LIMIT_LABELS = {"S", "M", "L", "XL"};
    static final int CHAPTER_SCROLL_W = 4;
    static final int SHARED_MENU_W = 168;
    static final int CHAPTER_TEXT_MENU_H = 38;
    static final int FONT_SIZE_SLIDER_POPOVER_H = 72;
    static final int FONT_SIZE_SLIDER_POPOVER_GAP = 3;

    private TabletLayout() {
    }

    static void applyRootSize(TabletUiState state, int width, int height, boolean fullScreenMode) {
        if (state == null) {
            return;
        }
        state.fullScreenMode = fullScreenMode;
        state.tabletRootWidth = Math.max(1, width);
        state.tabletRootHeight = Math.max(1, height);
    }

    static int rootWidth(TabletUiState state) {
        return state == null || state.tabletRootWidth <= 0 ? ROOT_W : state.tabletRootWidth;
    }

    static int rootHeight(TabletUiState state) {
        return state == null || state.tabletRootHeight <= 0 ? ROOT_H : state.tabletRootHeight;
    }

    static int bodyWidth(TabletUiState state) {
        return Math.max(160, rootWidth(state) - PAD * 2);
    }

    static int bodyHeight(TabletUiState state) {
        return Math.max(120, rootHeight(state) - BODY_Y - PAD_Y);
    }

    static int chapterHeight(TabletUiState state) {
        return bodyHeight(state);
    }

    static int canvasHeight(TabletUiState state) {
        return bodyHeight(state);
    }

    static int chapterPanelWidth(TabletUiState state) {
        if (state == null) {
            return CHAPTER_W;
        }
        int width = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanelWidth));
        if (width <= CHAPTER_W_ICON_SNAP || state.chapterPanelCollapsed) {
            return CHAPTER_W_ICON;
        }
        return width;
    }

    static boolean isChapterPanelCollapsed(TabletUiState state) {
        return chapterPanelWidth(state) <= CHAPTER_W_ICON;
    }

    static int canvasPanelX(TabletUiState state) {
        return CHAPTER_X + chapterPanelWidth(state) + GAP;
    }

    static int canvasPanelWidth(TabletUiState state) {
        return Math.max(120, bodyWidth(state) - chapterPanelWidth(state) - GAP);
    }

    static int indexAtY(int localY, TabletUiState state) {
        int slot = (localY - state.chapterRowStartY + state.chapterScroll) / chapterRowStep(state);
        int size = Math.max(1, visibleChapterGroups(state).size());
        return Math.max(0, Math.min(size - 1, slot));
    }

    static int chapterInsertIndexAtY(int localY, TabletUiState state) {
        int size = visibleChapterGroups(state).size();
        if (size <= 0) {
            return 0;
        }
        int scrolledY = localY - state.chapterRowStartY + state.chapterScroll;
        if (scrolledY <= 0) {
            return 0;
        }
        int slotH = chapterRowStep(state);
        int idx = scrolledY / slotH;
        int inSlotY = scrolledY % slotH;
        int insert = inSlotY < (slotH / 2) ? idx : idx + 1;
        if (state.chapterDragActive && !state.chapterDragName.isBlank()) {
            int ghostIdx = Math.max(0, Math.min(size, state.chapterDragTargetIndex));
            if (insert > ghostIdx) {
                insert--;
            }
        }
        return Math.max(0, Math.min(size, insert));
    }

    static int chapterIndexAtY(int localY, TabletUiState state) {
        int scrolledY = localY - state.chapterRowStartY + state.chapterScroll;
        if (scrolledY < 0) {
            return -1;
        }
        int slotH = chapterRowStep(state);
        int idx = scrolledY / slotH;
        int inSlotY = scrolledY % slotH;
        if (inSlotY >= Math.min(CHAPTER_CARD_H, slotH)) {
            return -1;
        }
        List<String> groups = visibleChapterGroups(state);
        return idx >= 0 && idx < groups.size() ? idx : -1;
    }

    static boolean isChapterScrollBarHit(int localX, int localY, TabletUiState state) {
        if (state.chapterScrollMax <= 0) {
            return false;
        }
        if (isChapterPanelCollapsed(state) || state.chapterListWidth <= 54) {
            return false;
        }
        int listLeft = state.chapterListOriginX;
        int listRight = listLeft + Math.max(0, state.chapterListWidth);
        int x = Math.max(listLeft, state.chapterScrollTrackX - 3);
        int y = state.chapterListOriginY;
        int w = Math.max(CHAPTER_SCROLL_W, listRight - x);
        int h = Math.max(state.chapterScrollTrackH, state.chapterListHeight);
        return ScrollController.hit(localX, localY, x, y, w, h);
    }

    static boolean isChapterCardAreaHit(int localX, int localY, TabletUiState state) {
        int left = Math.max(0, state.chapterCardHitLeft);
        int right = Math.max(left, state.chapterCardHitRight);
        int top = Math.max(0, state.chapterCardHitTop);
        int bottom = Math.max(top, state.chapterCardHitBottom);
        return localX >= left && localX <= right && localY >= top && localY <= bottom;
    }

    static void updateChapterScrollByMouse(double mouseY, TabletUiState state) {
        state.chapterScroll = ScrollController.byMouse((int) Math.round(mouseY), state.chapterScrollTrackY, state.chapterScrollTrackH, state.chapterScrollKnobH, state.chapterScrollMax);
    }

    static String chapterAtY(int localY, TabletUiState state) {
        int idx = chapterIndexAtY(localY, state);
        List<String> groups = visibleChapterGroups(state);
        if (groups.isEmpty() || idx < 0 || idx >= groups.size()) {
            return "";
        }
        return groups.get(idx);
    }

    static int chapterTextMenuY(TabletUiState state, int listHeight) {
        List<String> groups = visibleChapterGroups(state);
        int idx = Math.max(0, groups.indexOf(state.chapterTextMenuTarget));
        int menuHeight = chapterTextMenuHeight(state);
        int rowTop = 8 + idx * chapterRowStep(state) - state.chapterScroll;
        int rowBottom = rowTop + CHAPTER_CARD_H;
        int aboveY = rowTop - menuHeight - 6;
        int belowY = rowBottom + 3;
        if (aboveY >= 2) {
            return aboveY;
        }
        if (belowY <= listHeight - menuHeight) {
            return belowY;
        }
        return Math.max(2, Math.min(belowY, listHeight - menuHeight));
    }

    static int chapterTextMenuX(TabletUiState state) {
        return Math.max(1, state.chapterCardHitLeft - state.chapterListOriginX);
    }

    static int chapterTextMenuWidth(TabletUiState state) {
        return Math.max(44, state.chapterCardHitRight - state.chapterCardHitLeft);
    }

    static int chapterTextMenuHeight(TabletUiState state) {
        return CHAPTER_TEXT_MENU_H;
    }

    static int chapterRowStep(TabletUiState state) {
        if (state != null && (state.chapterPanelCollapsed || state.chapterListWidth <= 54)) {
            return CHAPTER_COLLAPSED_ROW_STEP;
        }
        return CHAPTER_CARD_H + CHAPTER_CARD_GAP;
    }

    static boolean isChapterFontSizeSliderOpen(TabletUiState state) {
        return state != null
                && !state.chapterTextMenuTarget.isBlank()
                && state.chapterTextMenuTarget.equals(state.chapterTextFontSizeSliderTarget);
    }

    static int[] chapterTextFontSizeSliderBounds(TabletUiState state) {
        int listHeight = state.chapterListHeight > 0 ? state.chapterListHeight : chapterHeight(state) - 12;
        int fx = chapterTextMenuX(state);
        int fy = chapterTextMenuY(state, listHeight);
        int fw = Math.min(Math.max(1, state.chapterListWidth - fx - 1), chapterTextMenuWidth(state));
        boolean wrap = fw < 132;
        int toolCount = 8;
        int firstRowCount = wrap ? 4 : toolCount;
        int secondRowCount = wrap ? toolCount - firstRowCount : 0;
        int btnW = wrap ? 16 : Math.min(16, Math.max(12, (fw - 4 - (toolCount - 1)) / toolCount));
        int[] topXs = distributedChapterToolXs(fw, btnW, firstRowCount);
        int[] bottomXs = distributedChapterToolXs(fw, btnW, Math.max(1, secondRowCount));
        int styleY = wrap ? 20 : 2;
        int sizeX = wrap ? bottomXs[3] : topXs[7];
        return new int[]{
                fx + sizeX,
                fy + styleY + 16 + FONT_SIZE_SLIDER_POPOVER_GAP,
                btnW,
                FONT_SIZE_SLIDER_POPOVER_H
        };
    }

    private static int[] distributedChapterToolXs(int width, int buttonWidth, int count) {
        int safeCount = Math.max(1, count);
        int[] xs = new int[safeCount];
        if (safeCount == 1) {
            xs[0] = Math.max(2, (width - buttonWidth) / 2);
            return xs;
        }
        int usable = Math.max(0, width - 4 - buttonWidth);
        for (int i = 0; i < safeCount; i++) {
            xs[i] = 2 + Math.round((float) usable * ((float) i / (float) (safeCount - 1)));
        }
        return xs;
    }

    private static List<String> visibleChapterGroups(TabletUiState state) {
        String query = SearchFilter.normalize(state.chapterSearch);
        List<String> groups = new ArrayList<>();
        for (String group : ClientQuestCache.groupOrder()) {
            if (DRAFT_CHAPTER.equals(group)) {
                continue;
            }
            if (!state.canEdit && ClientQuestCache.groupHiddenPreview(group)) {
                continue;
            }
            if (!SearchFilter.matches(query, group)) {
                continue;
            }
            groups.add(group);
        }
        return groups;
    }
}
