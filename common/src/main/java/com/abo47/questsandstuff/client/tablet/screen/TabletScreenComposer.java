package com.abo47.questsandstuff.client.tablet.screen;

import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.chapter.ChapterPanel;
import com.abo47.questsandstuff.client.tablet.chapter.ChapterPanelInteractionWidget;
import com.abo47.questsandstuff.client.tablet.chapter.ChapterSplitterWidget;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.tools.TabletToolsMenu;
import com.abo47.questsandstuff.client.tablet.tools.ToolMenuLayerWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelOutlines;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GRID_SIZES;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasPanelX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletState;

public final class TabletScreenComposer {
    private TabletScreenComposer() {
    }
    public static WidgetGroup create(Player player) {
        TabletUiState state = TabletScreenBootstrap.prepare(player);

        TabletRootWidget root = new TabletRootWidget(0, 0, ROOT_W, ROOT_H, state);
        root.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));
        WidgetGroup rootMaskTop = new WidgetGroup(0, 0, ROOT_W, 0);
        rootMaskTop.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskLeft = new WidgetGroup(0, 0, 0, 0);
        rootMaskLeft.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskRight = new WidgetGroup(0, 0, 0, 0);
        rootMaskRight.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskBottom = new WidgetGroup(0, 0, ROOT_W, 0);
        rootMaskBottom.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        int initialChapterW = chapterPanelWidth(state);
        boolean initialChapterCollapsed = isChapterPanelCollapsed(state);
        int initialCanvasX = canvasPanelX(state);
        int initialCanvasW = canvasPanelWidth(state);
        WidgetGroup chapterPanel = panel(CHAPTER_X, CHAPTER_Y, initialChapterW, CHAPTER_H, ModColors.SURFACE_PANEL, ModColors.BORDER_BASE);
        WidgetGroup[] chapterPanelRef = new WidgetGroup[]{chapterPanel};
        WidgetGroup canvasPanel = new WidgetGroup(initialCanvasX, CANVAS_Y, initialCanvasW, CANVAS_H) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, state);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, state);
            }
        };

        final int contentInset = 6;
        final int topY = contentInset;
        final int headerH = 14;
        final int toolsW = headerH;
        final int topGap = 4;
        final int chapterTopY = topY;
        final int chapterHeaderH = headerH;
        final int chapterListGap = contentInset;
        final int chapterListY = chapterTopY + chapterHeaderH + chapterListGap;

        WidgetGroup chapterList = new TabletScissoredWidgetGroup(contentInset, chapterListY, Math.max(24, initialChapterW - contentInset * 2), CHAPTER_H - chapterListY - contentInset - 1);
        chapterList.setBackground(initialChapterCollapsed ? Surfaces.fill(ModColors.SURFACE_BASE) : Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        WidgetGroup chapterMenuOverlay = new WidgetGroup(0, 0, ROOT_W, ROOT_H);
        WidgetGroup[] splitterRef = new WidgetGroup[1];
        Runnable[] refresh = new Runnable[1];
        Runnable[] refreshCanvas = new Runnable[1];
        Runnable[] refreshChapterViews = new Runnable[1];
        WidgetGroup modalLayer = new ModalLayerWidget(0, 0, ROOT_W, ROOT_H, state, () -> refresh[0].run());

        int initialTop = CANVAS_TOP_H_COMPACT;
        CanvasViewport canvasViewport = new CanvasViewport(contentInset, initialTop + contentInset, Math.max(64, initialCanvasW - contentInset * 2), CANVAS_H - initialTop - contentInset * 2, state, player);
        canvasViewport.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));

        TabletHeaderControls headers = TabletHeaderControls.create(state, () -> refresh[0].run(), contentInset, chapterTopY, chapterHeaderH, initialChapterW, topY, headerH);
        TextFieldWidget chapterSearchField = headers.chapterSearchField();
        WidgetGroup toolsMenu = new ToolMenuLayerWidget(0, 0, ROOT_W, ROOT_H, state, () -> refresh[0].run());
        WidgetGroup questDetailsLayer = new QuestDetailsLayerWidget(0, 0, ROOT_W, ROOT_H, state, () -> refresh[0].run());

        refresh[0] = () -> {
            root.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));
            rootMaskTop.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskLeft.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskRight.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskBottom.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            state.editorAvailable = player.hasPermissions(2);
            state.canEdit = state.editorAvailable && state.editMode;
            if (!state.canEdit) {
                state.toolsGridSizeMenuOpen = false;
                state.toolsGridOpacityMenuOpen = false;
                state.chapterMenuOpen = false;
                state.contextMenuOpen = false;
                state.createQuestModalOpen = false;
                state.selectedQuestIds.clear();
            }
            TabletScreenBootstrap.keepSelectedGroupValid(state, true);
            int topH = CANVAS_TOP_H_COMPACT;
            state.chapterPanelWidth = chapterPanelWidth(state);
            state.chapterPanelCollapsed = isChapterPanelCollapsed(state);
            int chapterW = chapterPanelWidth(state);
            int canvasX = canvasPanelX(state);
            int canvasW = canvasPanelWidth(state);
            boolean chapterCollapsed = state.chapterPanelCollapsed;
            int dynamicListY = chapterCollapsed ? 6 : chapterListY;
            int chapterSideInset = 7;
            int dynamicListX = chapterCollapsed ? 4 : chapterSideInset;
            int dynamicListW = chapterCollapsed ? Math.max(18, chapterW - 8) : Math.max(24, chapterW - chapterSideInset * 2);

            chapterPanelRef[0].setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL, ModColors.BORDER_BASE));
            chapterList.setBackground(chapterCollapsed ? Surfaces.fill(ModColors.SURFACE_BASE) : Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            canvasViewport.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            headers.refreshSurfaces(state);

            chapterPanelRef[0].setSize(chapterW, CHAPTER_H);
            headers.layoutChapter(chapterCollapsed, dynamicListX, dynamicListW, chapterTopY, chapterHeaderH);
            chapterList.setSelfPosition(dynamicListX, dynamicListY);
            chapterList.setSize(dynamicListW, CHAPTER_H - dynamicListY - contentInset - 1);
            chapterPanelRef[0].setSelfPosition(CHAPTER_X, CHAPTER_Y);
            canvasPanel.setSelfPosition(canvasX, CANVAS_Y);
            canvasPanel.setSize(canvasW, CANVAS_H);
            toolsMenu.setSize(ROOT_W, ROOT_H);
            questDetailsLayer.setSize(ROOT_W, ROOT_H);
            if (splitterRef[0] != null) {
                int splitterX = CHAPTER_X + chapterW + Math.max(0, (GAP - SPLITTER_W) / 2);
                splitterRef[0].setSelfPosition(splitterX, CHAPTER_Y);
                splitterRef[0].setSize(SPLITTER_W, CHAPTER_H);
            }

            state.canvasPanelX = canvasX;
            state.canvasPanelY = CANVAS_Y;
            state.canvasPanelW = canvasW;
            state.canvasPanelH = CANVAS_H;

            int availableViewportW = canvasW - contentInset * 2;
            int availableViewportH = CANVAS_H - topH - contentInset * 2;
            int innerAvailableW = Math.max(1, availableViewportW - 1);
            int innerAvailableH = Math.max(1, availableViewportH - 1);
            state.gridSizeIndex = clampGridSizeIndex(state.gridSizeIndex);
            int cell = Math.max(1, GRID_SIZES[state.gridSizeIndex]);
            int gridCols = Math.max(1, innerAvailableW / cell);
            int gridRows = Math.max(1, innerAvailableH / cell);
            state.gridCellPx = cell;
            state.gridCols = gridCols;
            state.gridRows = gridRows;
            int viewportW = Math.max(cell + 1, gridCols * cell + 1);
            int viewportH = Math.max(cell + 1, gridRows * cell + 1);
            int viewportX = contentInset + Math.max(0, (availableViewportW - viewportW) / 2);
            int viewportY = topH + contentInset + Math.max(0, (availableViewportH - viewportH) / 2);
            canvasViewport.setSelfPosition(viewportX, viewportY);
            canvasViewport.setSize(viewportW, viewportH);
            state.canvasViewportX = viewportX;
            state.canvasViewportY = viewportY;
            state.canvasViewportW = viewportW;
            state.canvasViewportH = viewportH;
            int holeX = canvasX + viewportX;
            int holeY = CANVAS_Y + viewportY;
            int holeW = viewportW;
            int holeH = viewportH;
            rootMaskTop.setSelfPosition(0, 0);
            rootMaskTop.setSize(ROOT_W, Math.max(0, holeY));
            rootMaskLeft.setSelfPosition(0, holeY);
            rootMaskLeft.setSize(Math.max(0, holeX), holeH);
            rootMaskRight.setSelfPosition(holeX + holeW, holeY);
            rootMaskRight.setSize(Math.max(0, ROOT_W - (holeX + holeW)), holeH);
            rootMaskBottom.setSelfPosition(0, holeY + holeH);
            rootMaskBottom.setSize(ROOT_W, Math.max(0, ROOT_H - (holeY + holeH)));

            int headerX = viewportX;
            int headerW = viewportW;
            headers.layoutCanvas(state, headerX, headerW, topY, headerH, toolsW, topGap);
            TabletToolsMenu.rebuild(toolsMenu, state, player, refresh[0], canvasX, headers.toolsX(), topY, headerH, toolsW);
            TabletUiPerfProfiler.profile("ui.rebuildQuestDetails", () -> QuestDetailsWindow.rebuild(questDetailsLayer, state, player, refresh[0]));
            refreshChapterViews[0].run();
            TabletUiPerfProfiler.profile("ui.rebuildChapterModal", () -> TabletModalPanel.rebuildChapterModal(modalLayer, state, player, refresh[0]));
            refreshCanvas[0].run();
        };
        refreshCanvas[0] = () -> TabletUiPerfProfiler.profile("ui.rebuildQuestCanvas", () -> CanvasRenderer.rebuildQuestCanvas(canvasViewport, state));
        refreshChapterViews[0] = () -> {
            TabletUiPerfProfiler.profile("ui.rebuildChapterList", () -> ChapterPanel.rebuildChapterList(chapterList, state, player, refresh[0]));
            TabletUiPerfProfiler.profile("ui.rebuildChapterMenu", () -> ChapterPanel.rebuildChapterMenu(chapterMenuOverlay, state, player, refresh[0]));
        };
        root.setRefresher(refresh[0]);
        canvasViewport.setRefresher(refresh[0]);
        canvasViewport.setCanvasRefresher(refreshCanvas[0]);
        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setModalLayer(modalLayer);
        root.setFrontWindowLayer(questDetailsLayer);
        root.setCanvasViewport(canvasViewport);

        root.setUndoRedoActions(TabletScreenBootstrap.undoAction(state, player), TabletScreenBootstrap.redoAction(state, player));
        chapterPanel = new ChapterPanelInteractionWidget(CHAPTER_X, CHAPTER_Y, initialChapterW, CHAPTER_H, state, player, refresh[0], refreshChapterViews[0]);
        chapterPanel.addWidgets(chapterSearchField, chapterList);
        chapterPanelRef[0] = chapterPanel;

        headers.syncFocus(state);

        canvasPanel.addWidget(canvasViewport);
        headers.addToCanvas(canvasPanel);

        WidgetGroup splitter = new ChapterSplitterWidget(state, refresh[0]);
        splitterRef[0] = splitter;

        root.addWidgets(
                rootMaskTop,
                rootMaskLeft,
                rootMaskRight,
                rootMaskBottom,
                chapterPanel,
                splitter,
                canvasPanel,
                chapterMenuOverlay,
                toolsMenu,
                questDetailsLayer,
                modalLayer
        );
        refresh[0].run();
        return root;
    }

}
