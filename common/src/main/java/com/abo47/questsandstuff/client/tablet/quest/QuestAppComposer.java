package com.abo47.questsandstuff.client.tablet.quest;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.bootstrap.TabletBootstrap;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.TabletScissoredWidgetGroup;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.modal.ModalDismissGuard;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterPanel;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterPanelInteractionWidget;
import com.abo47.questsandstuff.client.tablet.quest.chapter.ChapterSplitterWidget;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsLayerWidget;
import com.abo47.questsandstuff.client.tablet.quest.tools.TabletToolsMenu;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuLayerWidget;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinEditManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiPerfProfiler;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CANVAS_TOP_H_COMPACT;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_PANEL_GUTTER_BOTTOM;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_PANEL_GUTTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.GRID_SIZES;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.PANEL_INSET;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.canvasHeight;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.canvasPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.canvasPanelX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.canvasViewportBounds;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.setActiveTabletState;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.rootWidth;

public final class QuestAppComposer {
    private QuestAppComposer() {
    }
    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletBootstrap.prepare(player);
        TabletLifecycle.restoreRememberedWindow(state);
        state.root.currentApp = "quest";
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);
        int initialChapterH = chapterHeight(state);
        int initialCanvasH = canvasHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        refreshRootBackground(root, state);
        int initialChapterW = chapterPanelWidth(state);
        int initialCanvasX = canvasPanelX(state);
        int initialCanvasW = canvasPanelWidth(state);
        WidgetGroup chapterPanel = null;
        WidgetGroup[] chapterPanelRef = new WidgetGroup[1];
        WidgetGroup canvasPanel = SplitPanelLayout.rightPanel(initialCanvasX, CANVAS_Y, initialCanvasW, initialCanvasH, state);

        final int contentInset = PANEL_INSET;
        final int topY = contentInset;
        final int headerH = HEADER_H;
        final int toolsW = headerH;
        final int topGap = HEADER_GAP;
        final int chapterTopY = topY;
        final int chapterSideInset = CHAPTER_PANEL_GUTTER_X;
        final int chapterBottomInset = CHAPTER_PANEL_GUTTER_BOTTOM;
        final int chapterHeaderH = headerH;
        final int chapterListGap = contentInset - 1;
        final int chapterListY = chapterTopY + chapterHeaderH + chapterListGap;

        WidgetGroup chapterList = new TabletScissoredWidgetGroup(chapterSideInset, chapterListY, Math.max(24, initialChapterW - chapterSideInset * 2), Math.max(1, initialChapterH - chapterListY - chapterBottomInset));
        chapterList.setBackground(isChapterPanelCollapsed(state) ? SurfaceFactory.fill(TabletColors.SURFACE_BASE) : SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
        WidgetGroup chapterMenuOverlay = new WidgetGroup(0, 0, initialRootW, initialRootH);
        WidgetGroup[] splitterRef = new WidgetGroup[1];
        Runnable[] refresh = new Runnable[1];
        Runnable[] refreshCanvas = new Runnable[1];
        Runnable[] refreshChapterViews = new Runnable[1];
        WidgetGroup[] viewportBgRef = new WidgetGroup[1];
        WidgetGroup modalLayer = new ModalDismissGuard(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());

        int initialTop = CANVAS_TOP_H_COMPACT;
        int[] initialViewport = canvasViewportBounds(initialCanvasW, initialCanvasH, initialTop);
        CanvasViewport canvasViewport = new CanvasViewport(0, 0, Math.max(64, initialViewport[2]), Math.max(32, initialViewport[3]), state, player);

        WidgetGroup viewportBg = new WidgetGroup(initialViewport[0], initialViewport[1], Math.max(64, initialViewport[2]), Math.max(32, initialViewport[3])) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                IGuiTexture bg = getBackgroundTexture();
                if (bg != null && !bg.equals(IGuiTexture.EMPTY)) {
                    bg.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                } else if (!TabletPanelChrome.hasPanelOverride(canvasPanel, state)) {
                    SurfaceFactory.fill(TabletColors.SURFACE_PANEL).draw(graphics, 0, 0, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
                }
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
            }
        };
        viewportBgRef[0] = viewportBg;

        QuestAppHeaderControls headers = QuestAppHeaderControls.create(player, state, () -> refresh[0].run(), chapterSideInset, chapterTopY, chapterHeaderH, initialChapterW, initialViewport[0], topY, headerH);
        TextFieldWidget chapterSearchField = headers.chapterSearchField();
        WidgetGroup toolsMenu = new ToolMenuLayerWidget(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());
        WidgetGroup questDetailsLayer = new QuestDetailsLayerWidget(0, 0, initialRootW, initialRootH, state, () -> refresh[0].run());
        SkinAnchorRegistry.register("quest_details_layer", questDetailsLayer);

        int HOME_BTN_SIZE = 10;
        ButtonWidget questHomeBtn = new ButtonWidget(0, 0, HOME_BTN_SIZE, HOME_BTN_SIZE,
                SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.subtleBorder()),
                cd -> TabletLifecycle.openTabletUiHome(player));
        questHomeBtn.setClientSideWidget();
        questHomeBtn.setHoverTexture(GlowShaderHelper.hoverGlow());
        questHomeBtn.setClickedTexture(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_ACCENT));
        root.addWidget(questHomeBtn);
        root.setHomeButton(questHomeBtn);

        refresh[0] = () -> {
            SkinAnchorRegistry.clear();
            refreshRootBackground(root, state);
            state.root.editorAvailable = player.hasPermissions(2);
            state.root.canEdit = state.root.editorAvailable && state.root.editMode;
            if (!state.root.canEdit) {
                state.canvas.toolsGridSizeMenuOpen = false;
                state.canvas.toolsGridOpacityMenuOpen = false;
                state.chapterPanel.chapterMenuOpen = false;
                ContextMenuController.close(state);
                state.canvas.canvasSelection.questIds().clear();
            }
            TabletBootstrap.keepSelectedChapterValid(state, true);
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

            chapterList.setBackground(chapterCollapsed ? SurfaceFactory.fill(TabletColors.SURFACE_BASE) : SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
            headers.refreshSurfaceFactory(state);

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
                splitterRef[0].setSelfPosition(SplitPanelLayout.splitterX(CHAPTER_X, chapterW), CHAPTER_Y);
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
            canvasViewport.setSelfPosition(0, 0);
            canvasViewport.setSize(viewportW, viewportH);
            if (viewportBgRef[0] != null) {
                viewportBgRef[0].setSelfPosition(viewportX, viewportY);
                viewportBgRef[0].setSize(viewportW, viewportH);
            }
            state.canvas.canvasViewportX = viewportX;
            state.canvas.canvasViewportY = viewportY;
            state.canvas.canvasViewportW = viewportW;
            state.canvas.canvasViewportH = viewportH;
            int homeBtnX = currentRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int homeBtnY = ROOT_PAD_Y + ((currentRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            questHomeBtn.setSelfPosition(homeBtnX, homeBtnY);
            questHomeBtn.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.subtleBorder()));

            int headerX = viewportX;
            int headerW = viewportW;
            headers.layoutCanvas(state, headerX, headerW, topY, headerH, toolsW, topGap);
            TabletToolsMenu.rebuild(toolsMenu, state, player, refresh[0], canvasX, headers.toolsX(), topY, headerH, toolsW);
            TabletUiPerfProfiler.profile("ui.rebuildQuestDetails", () -> QuestDetailsWindow.rebuild(questDetailsLayer, state, player, refresh[0]));
            refreshChapterViews[0].run();
            TabletUiPerfProfiler.profile("ui.rebuildChapterModal", () -> TabletModalPanel.rebuildChapterModal(modalLayer, state, player, refresh[0]));
            if (!QuestDetailsWindow.isVisible(state)) {
                refreshCanvas[0].run();
            }
            SkinAnchorRegistry.register("root", root);
            SkinAnchorRegistry.register("quests_home_btn", questHomeBtn);
            SkinAnchorRegistry.register("home_btn", questHomeBtn);
            SkinAnchorRegistry.register("quests_search", headers.searchField());
            SkinAnchorRegistry.register("quests_chapter_search", headers.chapterSearchField());
            SkinAnchorRegistry.register("quests_tools_btn", headers.toolsButton());

            SkinAnchorRegistry.register("quests_blueprint_btn", headers.blueprintButton());
            SkinAnchorRegistry.register("quests_claim_all_btn", headers.claimAllButton());
            SkinAnchorRegistry.register("quests_editor_btn", headers.editorButton());
            SkinAnchorRegistry.register("quests_chapter", chapterPanelRef[0]);
            SkinAnchorRegistry.register("quests_chapter_list", chapterList);
            SkinAnchorRegistry.register("quests_splitter", splitterRef[0]);
            SkinAnchorRegistry.register("quests_canvas", canvasPanel);
            SkinAnchorRegistry.register("quest_details_layer", questDetailsLayer);
            if (viewportBgRef[0] != null) {
                SkinAnchorRegistry.register("quests_canvas_background", viewportBgRef[0]);
            }
            SkinEditManager.reapplyOverrides(state, root);
            canvasViewport.setViewportBorderHidden(TabletPanelChrome.shouldHideViewportBorder(canvasPanel, state));
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

        root.setUndoRedoActions(TabletBootstrap.undoAction(state, player), TabletBootstrap.redoAction(state, player));
        chapterPanel = new ChapterPanelInteractionWidget(CHAPTER_X, CHAPTER_Y, initialChapterW, initialChapterH, state, player, refresh[0], refreshChapterViews[0]);
        chapterPanel.addWidgets(chapterSearchField, chapterList);
        chapterPanelRef[0] = chapterPanel;

        headers.syncFocus(state);

        viewportBg.addWidget(canvasViewport);
        canvasPanel.addWidget(viewportBg);
        headers.addToCanvas(canvasPanel);

        WidgetGroup splitter = new ChapterSplitterWidget(state, refresh[0], SplitPanelLayout.splitterX(CHAPTER_X, initialChapterW));
        splitterRef[0] = splitter;

        root.addWidgets(
                chapterPanel,
                splitter,
                canvasPanel,
                chapterMenuOverlay,
                toolsMenu,
                modalLayer,
                questDetailsLayer
        );
        refresh[0].run();
        return root;
    }

    private static void refreshRootBackground(TabletRootWidget root, TabletUiState state) {
        TabletRootWidget.refreshRootBackground(root, state);
    }
}
