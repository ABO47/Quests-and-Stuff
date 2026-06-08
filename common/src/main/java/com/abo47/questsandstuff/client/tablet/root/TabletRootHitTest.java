package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.chapter.menu.ChapterContextMenuLayout;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class TabletRootHitTest {
    private TabletRootHitTest() {
    }

    public static boolean isChapterTextMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (state.chapterPanel.chapterTextMenuTarget.isBlank()) {
            return false;
        }
        int listHeight = state.chapterPanel.chapterListHeight > 0 ? state.chapterPanel.chapterListHeight : TabletUiFactory.chapterHeight(state) - 12;
        int fy = TabletUiFactory.chapterTextMenuY(state, listHeight);
        int menuX = TabletUiFactory.chapterTextMenuX(state);
        int absX = rootX + TabletUiFactory.CHAPTER_X + state.chapterPanel.chapterListOriginX + menuX;
        int absY = rootY + TabletUiFactory.CHAPTER_Y + state.chapterPanel.chapterListOriginY + fy;
        int w = TabletUiFactory.chapterTextMenuWidth(state);
        int h = TabletUiFactory.chapterTextMenuHeight(state);
        return mouseX >= absX && mouseX <= absX + w && mouseY >= absY && mouseY <= absY + h;
    }

    public static boolean isChapterMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.chapterPanel.chapterMenuOpen) {
            return false;
        }
        ChapterContextMenuLayout layout = ChapterContextMenuLayout.resolve(state, TabletStateQueries.rootWidth(state), TabletStateQueries.rootHeight(state));
        int absMenuX = rootX + layout.menuX();
        int absMenuY = rootY + layout.menuY();
        return mouseX >= absMenuX && mouseX <= absMenuX + layout.menuW()
                && mouseY >= absMenuY && mouseY <= absMenuY + layout.menuH();
    }

    public static boolean isCanvasContextMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.contextMenu.contextMenuOpen) {
            return false;
        }
        int viewportX = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int viewportY = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int localX = (int) Math.round(mouseX - viewportX);
        int localY = (int) Math.round(mouseY - viewportY);
        return CanvasRenderer.isContextMenuHit(state, localX, localY);
    }

    public static boolean isCanvasTextMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.canvas.canvasTextMenuOpen || state.canvas.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, TabletStateQueries.selectedGroupName(state), state.canvas.canvasTextMenuTarget);
        if (text == null) {
            return false;
        }
        int viewportX = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int viewportY = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int localX = (int) Math.round(mouseX - viewportX);
        int localY = (int) Math.round(mouseY - viewportY);
        if (inside(localX, localY, CanvasRenderer.canvasTextMenuBounds(state, text, state.canvas.canvasViewportW, state.canvas.canvasViewportH, 8))) {
            return true;
        }
        return CanvasRenderer.isCanvasTextOwnerHit(state, text, localX, localY);
    }

    public static boolean isToolsMenuHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if ((!state.canvas.toolsMenuOpen && !state.canvas.toolsMenuClosing) || state.canvas.toolsMenuW <= 0 || state.canvas.toolsMenuH <= 0) {
            return false;
        }
        int localX = (int) Math.round(mouseX - rootX);
        int localY = (int) Math.round(mouseY - rootY);
        return localX >= state.canvas.toolsMenuX && localX <= state.canvas.toolsMenuX + state.canvas.toolsMenuW
                && localY >= state.canvas.toolsMenuY && localY <= state.canvas.toolsMenuY + state.canvas.toolsMenuH;
    }

    public static boolean isAssetContextHit(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        if (!state.pickers.assetContextOpen || !ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.ASSET_PICKER)) {
            return false;
        }
        if (state.pickers.assetContextMenuW <= 0 || state.pickers.assetContextMenuH <= 0) {
            return false;
        }
        int rootW = TabletStateQueries.rootWidth(state);
        int rootH = TabletStateQueries.rootHeight(state);
        int w = Math.max(1, Math.min(432, rootW - 32));
        int h = Math.max(1, Math.min(260, rootH - 32));
        int mx = (rootW - w) / 2;
        int my = (rootH - h) / 2;
        int absX = rootX + mx + state.pickers.assetContextMenuX;
        int absY = rootY + my + state.pickers.assetContextMenuY;
        return mouseX >= absX && mouseX <= absX + state.pickers.assetContextMenuW
                && mouseY >= absY && mouseY <= absY + state.pickers.assetContextMenuH;
    }

    public static boolean isInsideCanvasViewport(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + state.canvas.canvasPanelX + state.canvas.canvasViewportX;
        int y = rootY + state.canvas.canvasPanelY + state.canvas.canvasViewportY;
        int w = state.canvas.canvasViewportW;
        int h = state.canvas.canvasViewportH;
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static boolean inside(int x, int y, int[] bounds) {
        return bounds != null && bounds.length >= 4
                && x >= bounds[0] && x <= bounds[0] + bounds[2]
                && y >= bounds[1] && y <= bounds[1] + bounds[3];
    }

    public static boolean isInsideChapterPanel(TabletUiState state, int rootX, int rootY, double mouseX, double mouseY) {
        int x = rootX + TabletUiFactory.CHAPTER_X;
        int y = rootY + TabletUiFactory.CHAPTER_Y;
        int w = TabletUiFactory.chapterPanelWidth(state);
        int h = TabletUiFactory.chapterHeight(state);
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
