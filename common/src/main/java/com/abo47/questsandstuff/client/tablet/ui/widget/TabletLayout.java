package com.abo47.questsandstuff.client.tablet.ui.widget;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TabletLayout {
    public static final int ROOT_W = 563;
    public static final int ROOT_H = 352;
    public static final int ROOT_PAD_X = 16;
    public static final int ROOT_PAD_Y = 8;
    public static final int PANEL_GAP = 8;
    public static final int PANEL_INSET = 6;
    public static final int CANVAS_VIEWPORT_GUTTER_X = 9;
    public static final int CANVAS_VIEWPORT_GUTTER_TOP = 5;
    public static final int CANVAS_VIEWPORT_GUTTER_BOTTOM = 6;
    public static final int CHAPTER_PANEL_GUTTER_X = CANVAS_VIEWPORT_GUTTER_X;
    public static final int CHAPTER_PANEL_GUTTER_BOTTOM = CANVAS_VIEWPORT_GUTTER_BOTTOM;
    public static final int HEADER_H = 14;
    public static final int HEADER_GAP = 4;
    public static final int PAD = ROOT_PAD_X;
    public static final int PAD_Y = ROOT_PAD_Y;
    public static final int GAP = PANEL_GAP;
    public static final int BODY_X = PAD;
    public static final int BODY_Y = PAD_Y;
    public static final int BODY_W = ROOT_W - PAD * 2;
    public static final int BODY_H = ROOT_H - BODY_Y - PAD_Y;

    public static final int CHAPTER_W = 168;
    public static final int CHAPTER_W_MIN = 72;
    public static final int CHAPTER_W_ICON = 24;
    public static final int CHAPTER_W_MAX = 248;
    public static final int CHAPTER_W_ICON_SNAP = 56;
    public static final int SPLITTER_W = GAP;
    public static final int PANEL_W_MIN = 120;
    public static final int CANVAS_W = BODY_W - CHAPTER_W - GAP;
    public static final int CHAPTER_CARD_H = 32;
    public static final int CHAPTER_CARD_GAP = PANEL_GAP;
    public static final int CHAPTER_COLLAPSED_ROW_STEP = 30;
    public static final String DRAFT_CHAPTER = "__draft_chapter__";
    public static final Path ASSETS_ROOT_DIR = Path.of("config", "questsandstuff", "assets");

    public static final int CHAPTER_X = BODY_X;
    public static final int CHAPTER_Y = BODY_Y;
    public static final int CHAPTER_H = BODY_H;

    public static final int CANVAS_X = CHAPTER_X + CHAPTER_W + GAP;
    public static final int CANVAS_Y = BODY_Y;
    public static final int CANVAS_H = BODY_H;
    public static final int CANVAS_TOP_H_COMPACT = 20;
    public static final int CANVAS_TOP_H_EXPANDED = 20;
    public static final int CANVAS_GRID_ROWS = 20;
    public static final int CANVAS_GRID_COLS = 18;
    public static final int CARD_W = 15;
    public static final int CARD_H = 15;
    public static final int CONTEXT_ROW_H = 12;
    public static final int[] GRID_SIZES = {16};
    public static final int[] GRID_OPACITY = {20, 35, 50, 65, 80};
    public static final int[] CANVAS_BG_OPACITY = {0, 15, 30, 45, 60, 75, 90, 100};
    public static final int[] CANVAS_LIMIT_WIDTH = {132, 164, 196, 228};
    public static final int[] CANVAS_LIMIT_HEIGHT = {64, 78, 92, 104};
    public static final String[] CANVAS_LIMIT_LABELS = {"S", "M", "L", "XL"};
    public static final int CHAPTER_SCROLL_W = DragScrollBarWidget.RESERVED_WIDTH;
    public static final int SHARED_MENU_W = 168;

    private TabletLayout() {
    }

    public static void applyRootSize(TabletUiState state, int width, int height, boolean fullScreenMode) {
        if (state == null) {
            return;
        }
        state.root.fullScreenMode = fullScreenMode;
        state.root.tabletRootWidth = Math.max(1, width);
        state.root.tabletRootHeight = Math.max(1, height);
    }

    public static int rootWidth(TabletUiState state) {
        return state == null || state.root.tabletRootWidth <= 0 ? ROOT_W : state.root.tabletRootWidth;
    }

    public static int rootHeight(TabletUiState state) {
        return state == null || state.root.tabletRootHeight <= 0 ? ROOT_H : state.root.tabletRootHeight;
    }

    public static int bodyWidth(TabletUiState state) {
        return Math.max(160, rootWidth(state) - PAD * 2);
    }

    public static int bodyHeight(TabletUiState state) {
        return Math.max(120, rootHeight(state) - BODY_Y - PAD_Y);
    }

    public static int chapterHeight(TabletUiState state) {
        return bodyHeight(state);
    }

    public static int canvasHeight(TabletUiState state) {
        return bodyHeight(state);
    }

    public static int chapterPanelWidth(TabletUiState state) {
        if (state == null) {
            return CHAPTER_W;
        }
        int width = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanel.chapterPanelWidth));
        if (width <= CHAPTER_W_ICON_SNAP || state.chapterPanel.chapterPanelCollapsed) {
            return CHAPTER_W_ICON;
        }
        return width;
    }

    public static boolean isChapterPanelCollapsed(TabletUiState state) {
        return chapterPanelWidth(state) <= CHAPTER_W_ICON;
    }

    public static int canvasPanelX(TabletUiState state) {
        return CHAPTER_X + chapterPanelWidth(state) + GAP;
    }

    public static int canvasPanelWidth(TabletUiState state) {
        return Math.max(PANEL_W_MIN, bodyWidth(state) - chapterPanelWidth(state) - GAP);
    }

    public static int[] canvasViewportBounds(int panelW, int panelH, int topH) {
        int safeGutterX = Math.max(0, CANVAS_VIEWPORT_GUTTER_X);
        int safeTopGutter = Math.max(0, CANVAS_VIEWPORT_GUTTER_TOP);
        int safeBottomGutter = Math.max(0, CANVAS_VIEWPORT_GUTTER_BOTTOM);
        int safeTopH = Math.max(0, topH);
        int x = safeGutterX;
        int y = safeTopH + safeTopGutter;
        int width = Math.max(1, panelW - safeGutterX * 2);
        int height = Math.max(1, panelH - safeTopH - safeTopGutter - safeBottomGutter);
        return new int[]{x, y, width, height};
    }

    public static int indexAtY(int localY, TabletUiState state) {
        int slot = (localY - state.chapterPanel.chapterRowStartY + state.chapterPanel.chapterScroll) / chapterRowStep(state);
        int size = Math.max(1, visibleChapterGroups(state).size());
        return Math.max(0, Math.min(size - 1, slot));
    }

    public static int chapterInsertIndexAtY(int localY, TabletUiState state) {
        int size = visibleChapterGroups(state).size();
        if (size <= 0) {
            return 0;
        }
        int scrolledY = localY - state.chapterPanel.chapterRowStartY + state.chapterPanel.chapterScroll;
        if (scrolledY <= 0) {
            return 0;
        }
        int slotH = chapterRowStep(state);
        int idx = scrolledY / slotH;
        int inSlotY = scrolledY % slotH;
        int insert = inSlotY < (slotH / 2) ? idx : idx + 1;
        if (state.chapterPanel.chapterDragActive && !state.chapterPanel.chapterDragName.isBlank()) {
            int ghostIdx = Math.max(0, Math.min(size, state.chapterPanel.chapterDragTargetIndex));
            if (insert > ghostIdx) {
                insert--;
            }
        }
        return Math.max(0, Math.min(size, insert));
    }

    public static int chapterIndexAtY(int localY, TabletUiState state) {
        int scrolledY = localY - state.chapterPanel.chapterRowStartY + state.chapterPanel.chapterScroll;
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

    public static boolean isChapterScrollBarHit(int localX, int localY, TabletUiState state) {
        if (state.chapterPanel.chapterScrollMax <= 0) {
            return false;
        }
        if (isChapterPanelCollapsed(state) || state.chapterPanel.chapterListWidth <= 54) {
            return false;
        }
        int listLeft = state.chapterPanel.chapterListOriginX;
        int listRight = listLeft + Math.max(0, state.chapterPanel.chapterListWidth);
        int x = Math.max(listLeft, state.chapterPanel.chapterScrollTrackX - 3);
        int y = state.chapterPanel.chapterListOriginY;
        int w = Math.max(CHAPTER_SCROLL_W, listRight - x);
        int h = Math.max(state.chapterPanel.chapterScrollTrackH, state.chapterPanel.chapterListHeight);
        return ScrollMath.hit(localX, localY, x, y, w, h);
    }

    public static boolean isChapterCardAreaHit(int localX, int localY, TabletUiState state) {
        int left = Math.max(0, state.chapterPanel.chapterCardHitLeft);
        int right = Math.max(left, state.chapterPanel.chapterCardHitRight);
        int top = Math.max(0, state.chapterPanel.chapterCardHitTop);
        int bottom = Math.max(top, state.chapterPanel.chapterCardHitBottom);
        return localX >= left && localX <= right && localY >= top && localY <= bottom;
    }

    public static void updateChapterScrollByMouse(double mouseY, TabletUiState state) {
        state.chapterPanel.chapterScroll = ScrollMath.byMouse((int) Math.round(mouseY), state.chapterPanel.chapterScrollTrackY, state.chapterPanel.chapterScrollTrackH, state.chapterPanel.chapterScrollKnobH, state.chapterPanel.chapterScrollMax);
    }

    public static String chapterAtY(int localY, TabletUiState state) {
        int idx = chapterIndexAtY(localY, state);
        List<String> groups = visibleChapterGroups(state);
        if (groups.isEmpty() || idx < 0 || idx >= groups.size()) {
            return "";
        }
        return groups.get(idx);
    }

    public static int chapterTextMenuY(TabletUiState state, int listHeight) {
        List<String> groups = visibleChapterGroups(state);
        int idx = Math.max(0, groups.indexOf(state.chapterPanel.chapterTextMenuTarget));
        int menuHeight = chapterTextMenuHeight(state);
        int rowTop = PANEL_GAP + idx * chapterRowStep(state) - state.chapterPanel.chapterScroll;
        int rowBottom = rowTop + CHAPTER_CARD_H;
        int aboveY = rowTop - menuHeight - TextStyleButtons.CHAPTER_FRAME_GAP;
        int belowY = rowBottom + TextStyleButtons.CHAPTER_FRAME_GAP;
        if (aboveY >= 2) {
            return aboveY;
        }
        if (belowY <= listHeight - menuHeight) {
            return belowY;
        }
        return Math.max(2, Math.min(belowY, listHeight - menuHeight));
    }

    public static int chapterTextMenuX(TabletUiState state) {
        int menuW = chapterTextMenuWidth(state);
        int cardCenter = ((state.chapterPanel.chapterCardHitLeft + state.chapterPanel.chapterCardHitRight) / 2) - state.chapterPanel.chapterListOriginX;
        return cardCenter - menuW / 2;
    }

    public static int chapterTextMenuWidth(TabletUiState state) {
        return TextStyleButtons.preferredSingleRowWidth();
    }

    public static int chapterTextMenuHeight(TabletUiState state) {
        return TextStyleButtons.menuHeightForWidth(chapterTextMenuWidth(state));
    }

    public static int chapterRowStep(TabletUiState state) {
        if (state != null && (state.chapterPanel.chapterPanelCollapsed || state.chapterPanel.chapterListWidth <= 54)) {
            return CHAPTER_COLLAPSED_ROW_STEP;
        }
        return CHAPTER_CARD_H + CHAPTER_CARD_GAP;
    }

    private static List<String> visibleChapterGroups(TabletUiState state) {
        String query = SearchFilter.normalize(state.chapterPanel.chapterSearch);
        List<String> groups = new ArrayList<>();
        for (String chapter : ClientQuestStateFacade.chapterOrder()) {
            if (DRAFT_CHAPTER.equals(chapter)) {
                continue;
            }
            if (!state.root.canEdit && ClientQuestStateFacade.chapterHiddenPreview(chapter)) {
                continue;
            }
            if (!SearchFilter.matches(query, chapter)) {
                continue;
            }
            groups.add(chapter);
        }
        return groups;
    }
}
