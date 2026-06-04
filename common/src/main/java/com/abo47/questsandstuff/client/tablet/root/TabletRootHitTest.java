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
        int listHeight = state.chapterListHeight > 0 ? state.chapterListHeight : TabletUiFactory.chapterHeight(state) - 12;
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
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletUiFactory.rootWidth(state), TabletUiFactory.rootHeight(state));
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
        if (state.assetContextMenuW <= 0 || state.assetContextMenuH <= 0) {
            return false;
        }
        int rootW = TabletUiFactory.rootWidth(state);
        int rootH = TabletUiFactory.rootHeight(state);
        int w = Math.max(1, Math.min(432, rootW - 32));
        int h = Math.max(1, Math.min(260, rootH - 32));
        int mx = (rootW - w) / 2;
        int my = (rootH - h) / 2;
        int absX = rootX + mx + state.assetContextMenuX;
        int absY = rootY + my + state.assetContextMenuY;
        return mouseX >= absX && mouseX <= absX + state.assetContextMenuW
                && mouseY >= absY && mouseY <= absY + state.assetContextMenuH;
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
        int h = TabletUiFactory.chapterHeight(state);
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
