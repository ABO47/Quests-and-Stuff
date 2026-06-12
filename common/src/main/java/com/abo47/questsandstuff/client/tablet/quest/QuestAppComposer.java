package com.abo47.questsandstuff.client.tablet.quest;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterPanel;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterPanelInteractionWidget;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterSplitterWidget;
import com.abo47.questsandstuff.client.tablet.controls.TabletScissoredWidgetGroup;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalLayerWidget;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.shell.TabletShellBootstrap;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.quest.tools.TabletToolsMenu;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuLayerWidget;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiPerfProfiler;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelOutlines;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelOutline;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_PANEL_GUTTER_BOTTOM;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_PANEL_GUTTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GRID_SIZES;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.HEADER_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.PANEL_INSET;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasPanelX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.canvasViewportBounds;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletState;

public final class QuestAppComposer {
    private QuestAppComposer() {
    }
    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletShellBootstrap.prepare(player);
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);
        int initialChapterH = chapterHeight(state);
        int initialCanvasH = canvasHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        refreshRootBackground(root, state);
        WidgetGroup rootMaskTop = new WidgetGroup(0, 0, initialRootW, 0);
        rootMaskTop.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskLeft = new WidgetGroup(0, 0, 0, 0);
        rootMaskLeft.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskRight = new WidgetGroup(0, 0, 0, 0);
        rootMaskRight.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        WidgetGroup rootMaskBottom = new WidgetGroup(0, 0, initialRootW, 0);
        rootMaskBottom.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        int initialChapterW = chapterPanelWidth(state);
        int initialCanvasX = canvasPanelX(state);
        int initialCanvasW = canvasPanelWidth(state);
        WidgetGroup chapterPanel = new WidgetGroup(CHAPTER_X, CHAPTER_Y, initialChapterW, initialChapterH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawPanelChrome(graphics, this);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawPanelOutline(graphics, this);
            }
        };
        WidgetGroup[] chapterPanelRef = new WidgetGroup[]{chapterPanel};
        WidgetGroup canvasPanel = new WidgetGroup(initialCanvasX, CANVAS_Y, initialCanvasW, initialCanvasH) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, state);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, state);
            }
        };

        final int contentInset = PANEL_INSET;
        final int topY = contentInset;
        final int headerH = HEADER_H;
        final int toolsW = headerH;
        final int topGap = HEADER_GAP;
        final int chapterTopY = topY;
        final int chapterSideInset = CHAPTER_PANEL_GUTTER_X;
        final int chapterBottomInset = CHAPTER_PANEL_GUTTER_BOTTOM;
        final int chapterHeaderH = headerH;
        final int chapterListGap = contentInset;
        final int chapterListY = chapterTopY + chapterHeaderH + chapterListGap;

        WidgetGroup chapterList = new TabletScissoredWidgetGroup(chapterSideInset, chapterListY, Math.max(24, initialChapterW - chapterSideInset * 2), Math.max(1, initialChapterH - chapterListY - chapterBottomInset));
        chapterList.setBackground(isChapterPanelCollapsed(state) ? Surfaces.fill(ModColors.SURFACE_BASE) : Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        WidgetGroup chapterMenuOverlay = new WidgetGroup(0, 0, initialRootW, initialRootH);
        WidgetGroup[] splitterRef = new WidgetGroup[1];
        Runnable[] refresh = new Runnable[1];
        Runnable[] refreshCanvas = new Runnable[1];
        Runnable[] refreshChapterViews = new Runnable[1];
        WidgetGroup modalLayer = new ModalLayerWidget(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        int initialTop = CANVAS_TOP_H_COMPACT;
        int[] initialViewport = canvasViewportBounds(initialCanvasW, initialCanvasH, initialTop);
        CanvasViewport canvasViewport = new CanvasViewport(initialViewport[0], initialViewport[1], Math.max(64, initialViewport[2]), Math.max(32, initialViewport[3]), state, player);
        canvasViewport.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));

        QuestAppHeaderControls headers = QuestAppHeaderControls.create(player, state, () -> refresh[0].run(), chapterSideInset, chapterTopY, chapterHeaderH, initialChapterW, initialViewport[0], topY, headerH);
        TextFieldWidget chapterSearchField = headers.chapterSearchField();
        WidgetGroup toolsMenu = new ToolMenuLayerWidget(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());
        WidgetGroup questDetailsLayer = new QuestDetailsLayerWidget(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        int HOME_BTN_SIZE = 10;
        ButtonWidget questHomeBtn = new ButtonWidget(0, 0, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.openTabletUiFromItem(player));
        questHomeBtn.setClientSideWidget();
        questHomeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        questHomeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        root.addWidget(questHomeBtn);
        root.setHomeButton(questHomeBtn);

        refresh[0] = () -> {
            refreshRootBackground(root, state);
            rootMaskTop.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskLeft.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskRight.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            rootMaskBottom.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
            state.root.editorAvailable = player.hasPermissions(2);
            state.root.canEdit = state.root.editorAvailable && state.root.editMode;
            if (!state.root.canEdit) {
                state.canvas.toolsGridSizeMenuOpen = false;
                state.canvas.toolsGridOpacityMenuOpen = false;
                state.chapterPanel.chapterMenuOpen = false;
                ContextMenuState.close(state);
                state.canvas.canvasSelection.questIds().clear();
            }
            TabletShellBootstrap.keepSelectedGroupValid(state, true);
            int topH = CANVAS_TOP_H_COMPACT;
            int currentRootW = rootWidth(state);
            int currentRootH = rootHeight(state);
            int chapterH = chapterHeight(state);
            int canvasH = canvasHeight(state);
            root.setSize(currentRootW, currentRootH);
            state.chapterPanel.chapterPanelWidth = chapterPanelWidth(state);
            state.chapterPanel.chapterPanelCollapsed = isChapterPanelCollapsed(state);
            int chapterW = chapterPanelWidth(state);
            int canvasX = canvasPanelX(state);
            int canvasW = canvasPanelWidth(state);
            boolean chapterCollapsed = state.chapterPanel.chapterPanelCollapsed;
            int dynamicListY = chapterCollapsed ? 0 : chapterListY;
            int collapsedChapterInset = 0;
            int dynamicListX = chapterCollapsed ? collapsedChapterInset : chapterSideInset;
            int dynamicListW = chapterCollapsed ? Math.max(18, chapterW - collapsedChapterInset * 2) : Math.max(24, chapterW - chapterSideInset * 2);
            int dynamicListH = Math.max(1, chapterCollapsed ? chapterH : chapterH - dynamicListY - chapterBottomInset);

            chapterList.setBackground(chapterCollapsed ? Surfaces.fill(ModColors.SURFACE_BASE) : Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            canvasViewport.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
            headers.refreshSurfaces(state);

            chapterPanelRef[0].setSize(chapterW, chapterH);
            headers.layoutChapter(chapterCollapsed, dynamicListX, dynamicListW, chapterTopY, chapterHeaderH);
            chapterList.setSelfPosition(dynamicListX, dynamicListY);
            chapterList.setSize(dynamicListW, dynamicListH);
            chapterPanelRef[0].setSelfPosition(CHAPTER_X, CHAPTER_Y);
            canvasPanel.setSelfPosition(canvasX, CANVAS_Y);
            canvasPanel.setSize(canvasW, canvasH);
            chapterMenuOverlay.setSize(currentRootW, currentRootH);
            toolsMenu.setSize(currentRootW, currentRootH);
            questDetailsLayer.setSize(currentRootW, currentRootH);
            modalLayer.setSize(currentRootW, currentRootH);
            if (splitterRef[0] != null) {
                int splitterX = CHAPTER_X + chapterW + Math.max(0, (GAP - SPLITTER_W) / 2);
                splitterRef[0].setSelfPosition(splitterX, CHAPTER_Y);
                splitterRef[0].setSize(SPLITTER_W, chapterH);
            }

            state.canvas.canvasPanelX = canvasX;
            state.canvas.canvasPanelY = CANVAS_Y;
            state.canvas.canvasPanelW = canvasW;
            state.canvas.canvasPanelH = canvasH;

            int[] viewport = canvasViewportBounds(canvasW, canvasH, topH);
            int viewportX = viewport[0];
            int viewportY = viewport[1];
            int viewportW = viewport[2];
            int viewportH = viewport[3];
            int innerAvailableW = Math.max(1, viewportW - 1);
            int innerAvailableH = Math.max(1, viewportH - 1);
            state.canvas.gridSizeIndex = clampGridSizeIndex(state.canvas.gridSizeIndex);
            int cell = Math.max(1, GRID_SIZES[state.canvas.gridSizeIndex]);
            int gridCols = Math.max(1, innerAvailableW / cell);
            int gridRows = Math.max(1, innerAvailableH / cell);
            state.canvas.gridCellPx = cell;
            state.canvas.gridCols = gridCols;
            state.canvas.gridRows = gridRows;
            canvasViewport.setSelfPosition(viewportX, viewportY);
            canvasViewport.setSize(viewportW, viewportH);
            state.canvas.canvasViewportX = viewportX;
            state.canvas.canvasViewportY = viewportY;
            state.canvas.canvasViewportW = viewportW;
            state.canvas.canvasViewportH = viewportH;
            int homeBtnX = ROOT_W - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int homeBtnY = ROOT_PAD_Y + ((currentRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            questHomeBtn.setSelfPosition(homeBtnX, homeBtnY);
            int holeX = canvasX + viewportX;
            int holeY = CANVAS_Y + viewportY;
            int holeW = viewportW;
            int holeH = viewportH;
            rootMaskTop.setSelfPosition(0, 0);
            rootMaskTop.setSize(currentRootW, Math.max(0, holeY));
            rootMaskLeft.setSelfPosition(0, holeY);
            rootMaskLeft.setSize(Math.max(0, holeX), holeH);
            rootMaskRight.setSelfPosition(holeX + holeW, holeY);
            rootMaskRight.setSize(Math.max(0, currentRootW - (holeX + holeW)), holeH);
            rootMaskBottom.setSelfPosition(0, holeY + holeH);
            rootMaskBottom.setSize(currentRootW, Math.max(0, currentRootH - (holeY + holeH)));

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

        root.setUndoRedoActions(TabletShellBootstrap.undoAction(state, player), TabletShellBootstrap.redoAction(state, player));
        chapterPanel = new ChapterPanelInteractionWidget(CHAPTER_X, CHAPTER_Y, initialChapterW, initialChapterH, state, player, refresh[0], refreshChapterViews[0]);
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

    private static void refreshRootBackground(TabletRootWidget root, TabletUiState state) {
        root.setBackground(state != null && state.root.fullScreenMode
                ? Surfaces.transparent()
                : Surfaces.transparentBorder(ModColors.BORDER_BASE));
    }
}
