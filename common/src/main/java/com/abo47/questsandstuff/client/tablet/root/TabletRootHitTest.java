package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;

public final class TabletRootHitTest {
    private TabletRootHitTest() {
    }

    public static boolean isChapterTextMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (state.chapterTextMenuTarget.isBlank()) {
            return false;
        }
        int listHeight = state.chapterListHeight > 0 ? state.chapterListHeight : TabletUiFactory.CHAPTER_H - 12;
        int fy = TabletUiFactory.chapterTextMenuY(state, listHeight);
        int menuX = TabletUiFactory.chapterTextMenuX(state);
        int absX = rootX + TabletUiFactory.CHAPTER_X + state.chapterListOriginX + menuX;
        int absY = rootY + TabletUiFactory.CHAPTER_Y + state.chapterListOriginY + fy;
        int w = Math.min(Math.max(1, state.chapterListWidth - menuX - 1), TabletUiFactory.chapterTextMenuWidth(state));
        int h = TabletUiFactory.chapterTextMenuHeight(state);
        if (mouseX >= absX && mouseX <= absX + w && mouseY >= absY && mouseY <= absY + h) {
            return true;
        }
        if (!TabletUiFactory.isChapterFontSizeSliderOpen(state)) {
            return false;
        }
        int[] slider = TabletUiFactory.chapterTextFontSizeSliderBounds(state);
        int sliderX = rootX + TabletUiFactory.CHAPTER_X + state.chapterListOriginX + slider[0];
        int sliderY = rootY + TabletUiFactory.CHAPTER_Y + state.chapterListOriginY + slider[1];
        return mouseX >= sliderX && mouseX <= sliderX + slider[2]
                && mouseY >= sliderY && mouseY <= sliderY + slider[3];
    }

    public static boolean isChapterMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletUiFactory.ROOT_W, TabletUiFactory.ROOT_H);
        int absMenuX = rootX + layout.menuX();
        int absMenuY = rootY + layout.menuY();
        return mouseX >= absMenuX && mouseX <= absMenuX + layout.menuW()
                && mouseY >= absMenuY && mouseY <= absMenuY + layout.menuH();
    }

    public static boolean isCanvasContextMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.contextMenuOpen) {
            return false;
        }
        int viewportX = rootX + state.canvasPanelX + state.canvasViewportX;
        int viewportY = rootY + state.canvasPanelY + state.canvasViewportY;
        int localX = (int) Math.round(mouseX - viewportX);
        int localY = (int) Math.round(mouseY - viewportY);
        return CanvasRenderer.isContextMenuHit(state, localX, localY);
    }

    public static boolean isToolsMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if ((!state.toolsMenuOpen && !state.toolsMenuClosing) || state.toolsMenuW <= 0 || state.toolsMenuH <= 0) {
            return false;
        }
        int localX = (int) Math.round(mouseX - rootX);
        int localY = (int) Math.round(mouseY - rootY);
        return localX >= state.toolsMenuX && localX <= state.toolsMenuX + state.toolsMenuW
                && localY >= state.toolsMenuY && localY <= state.toolsMenuY + state.toolsMenuH;
    }

    public static boolean isAssetContextHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.assetContextOpen || !state.assetPickerOpen) {
            return false;
        }
        int w = Math.min(432, TabletUiFactory.ROOT_W - 32);
        int h = Math.min(260, TabletUiFactory.ROOT_H - 32);
        int mx = (TabletUiFactory.ROOT_W - w) / 2;
        int my = (TabletUiFactory.ROOT_H - h) / 2;
        int ctxX = Math.max(170, Math.min(166 + state.assetContextX, w - TabletUiFactory.SHARED_MENU_W - 6));
        int ctxY = Math.max(26, Math.min(22 + state.assetContextY, h - (state.assetRenameOpen ? 120 : 66)));
        int ctxW = TabletUiFactory.SHARED_MENU_W;
        int ctxH = state.assetRenameOpen ? 106 : 52;
        int absX = rootX + mx + ctxX;
        int absY = rootY + my + ctxY;
        return mouseX >= absX && mouseX <= absX + ctxW && mouseY >= absY && mouseY <= absY + ctxH;
    }

    public static boolean isInsideCanvasViewport(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + state.canvasPanelX + state.canvasViewportX;
        int y = rootY + state.canvasPanelY + state.canvasViewportY;
        int w = state.canvasViewportW;
        int h = state.canvasViewportH;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public static boolean isInsideChapterPanel(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + TabletUiFactory.CHAPTER_X;
        int y = rootY + TabletUiFactory.CHAPTER_Y;
        int w = TabletUiFactory.chapterPanelWidth(state);
        int h = TabletUiFactory.CHAPTER_H;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
