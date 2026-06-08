package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.hit.CanvasHitTester;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasChapterSwitchAnimation;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasSelectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_LIMIT_HEIGHT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_LIMIT_WIDTH;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class CanvasRenderer {
    public static final float MIN_CANVAS_ZOOM = 0.5f;
    public static final float MAX_CANVAS_ZOOM = 3.0f;

    private CanvasRenderer() {
    }
    public static void rebuildQuestCanvas(CanvasViewport canvasViewport, TabletUiState state) {
        CanvasCameraController.beforeCanvasRebuild(state);
        canvasViewport.clearAllWidgets();
        String selectedGroup = CanvasRenderStateController.prepareRebuild(state);
        CanvasChapterSwitchAnimation.trackSelectedGroup(state, selectedGroup);
        CanvasSceneRenderer.applyCanvasBackground(canvasViewport);
        List<Map.Entry<String, CompoundTag>> quests = new ArrayList<>(ClientQuestCache.questEntries());
        quests.sort(Comparator.comparing(Map.Entry::getKey));

        int viewportW = canvasViewport.getSize().width;
        int viewportH = canvasViewport.getSize().height;
        int usableW = Math.max(1, viewportW - 1);
        int usableH = Math.max(1, viewportH - 1);
        if (state.canvas.canvasLimitEnabled) {
            usableW = Math.min(usableW, CANVAS_LIMIT_WIDTH[state.canvas.canvasLimitIndex]);
            usableH = Math.min(usableH, CANVAS_LIMIT_HEIGHT[state.canvas.canvasLimitIndex]);
        }
        int cell = CanvasGeometry.gridSize(state);
        int contentW = CanvasSceneRenderer.snapCanvasContentSize(usableW, cell);
        int contentH = CanvasSceneRenderer.snapCanvasContentSize(usableH, cell);
        int contentX = Math.max(0, (usableW - contentW) / 2);
        int contentY = Math.max(0, (usableH - contentH) / 2);
        CanvasRenderStateController.setContentBounds(state, contentX, contentY, contentW, contentH);
        CanvasCameraController.afterCanvasLayout(state, selectedGroup);
        CanvasSceneRenderer.renderCanvasSurfaces(canvasViewport, state, contentX, contentY, contentW, contentH, viewportW, viewportH);

        if (state.canvas.canvasLimitEnabled && (contentW < viewportW - 12 || contentH < viewportH - 12)) {
            WidgetGroup bounds = panel(4, 4, contentW + 4, contentH + 4, withAlpha(ModColors.SURFACE_PANEL_ALT, 36), ModColors.BORDER_ACCENT);
            canvasViewport.addWidget(bounds);
        }

        List<QuestCardLayout> visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        CanvasLayoutService.clampCanvasOffset(state, visibleCards, contentW, contentH);
        visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        if (CanvasCameraController.consumePendingQuestFocus(state, visibleCards, selectedGroup)) {
            visibleCards = CanvasLayoutService.layoutVisibleCards(quests, state);
        }
        CanvasCameraController.rememberCurrentGroup(state);

        Map<String, QuestCardLayout> byQuestId = new HashMap<>();
        for (QuestCardLayout card : visibleCards) {
            byQuestId.put(card.questId(), card);
        }
        CanvasRenderStateController.pruneStaleInteractiveState(state, byQuestId.keySet());
        WidgetGroup canvasContent = new WidgetGroup(0, 0, viewportW, viewportH);
        if (state.root.canEdit && state.canvas.gridEnabled) {
            CanvasSceneRenderer.renderGridOverlay(canvasContent, state, contentX, contentY, contentW, contentH);
        }
        CanvasSceneRenderer.renderCanvasElements(
                canvasContent,
                state,
                canvasViewport.player(),
                canvasViewport::refresh,
                visibleCards,
                viewportW,
                viewportH,
                canvasViewport::registerQuestCardLayer
        );
        ConnectionRenderer.renderPendingConnections(canvasContent, state, byQuestId, viewportW, viewportH);
        CanvasSelectionRenderer.renderAlignmentGuides(canvasContent, state);
        CanvasSelectionRenderer.updateSelectionBounds(state, visibleCards);
        CanvasRenderStateController.closeEditOnlyStateWhenReadOnly(state);
        CanvasSelectionRenderer.renderSelectionOverlay(canvasContent, state, visibleCards);
        WidgetGroup canvasContentLayer = CanvasChapterSwitchAnimation.wrap(state, canvasContent);
        canvasViewport.setCanvasContentLayer(canvasContentLayer);
        canvasViewport.addWidget(canvasContentLayer);
        WidgetGroup blueprintGhost = CanvasBlueprintController.placementGhost(canvasViewport, state);
        if (blueprintGhost != null) {
            canvasViewport.addWidget(blueprintGhost);
        }
        renderCanvasMetaPanels(canvasViewport, state, visibleCards, byQuestId, contentX, contentY, contentW, contentH);
        WidgetGroup miniNotification = CanvasMiniNotificationController.render(canvasViewport, state);
        if (miniNotification != null) {
            canvasViewport.addWidget(miniNotification);
        }
        canvasViewport.updateCardCache(visibleCards, byQuestId);
    }

    static boolean matchesFilters(CompoundTag questTag, TabletUiState state) {
        String selectedGroup = selectedGroupName(state);
        CompoundTag groups = questTag.getCompound("groups");
        if (selectedGroup.isBlank()) {
            return false;
        }
        if (!state.root.canEdit && isVisualHiddenOutsideEdit(questTag)) {
            return false;
        }
        return groups.contains(selectedGroup);
    }

    private static boolean isVisualHiddenOutsideEdit(CompoundTag questTag) {
        return ClientQuestCache.questHiddenPreview(questTag);
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.edgeKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.connectionColor(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionHidden(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionDirect(state, group, sourceQuestId, targetQuestId);
    }

    public static int snapToGrid(TabletUiState state, int value) {
        return CanvasGeometry.snapToGrid(state, value);
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        return CanvasHitTester.hitTestCard(cards, x, y);
    }

    public static CanvasImageLayer hitTestCanvasImage(TabletUiState state, int x, int y) {
        return CanvasHitTester.hitTestCanvasImage(state, x, y);
    }

    public static CanvasImageLayer hitTestSelectedCanvasImageControls(TabletUiState state, int x, int y) {
        return CanvasHitTester.hitTestSelectedCanvasImageControls(state, x, y);
    }

    public static CanvasTextLayer hitTestCanvasText(TabletUiState state, int x, int y) {
        return CanvasHitTester.hitTestCanvasText(state, x, y);
    }

    public static CanvasTextLayer hitTestSelectedCanvasTextControls(TabletUiState state, int x, int y) {
        return CanvasHitTester.hitTestSelectedCanvasTextControls(state, x, y);
    }

    public static boolean isCanvasTextResizeHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextResizeHandleHit(state, text, x, y);
    }

    public static boolean isCanvasTextRotateHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextRotateHandleHit(state, text, x, y);
    }

    public static double[] canvasTextLocalScreenPoint(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.canvasTextLocalScreenPoint(state, text, x, y);
    }

    public static boolean isCanvasTextOwnerHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasHitTester.isCanvasTextOwnerHit(state, text, x, y);
    }

    public static int canvasTextCursorAt(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasTextRenderer.canvasTextCursorAt(state, text, x, y);
    }

    public static int[] canvasTextMenuBounds(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        return CanvasHitTester.canvasTextMenuBounds(state, text, viewportW, viewportH, toolCount);
    }

    public static boolean isCanvasImageResizeHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasHitTester.isCanvasImageResizeHandleHit(state, image, x, y);
    }

    public static boolean isCanvasImageRotateHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasHitTester.isCanvasImageRotateHandleHit(state, image, x, y);
    }


    public static EdgeHit hitTestEdge(TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        return CanvasHitTester.hitTestEdge(state, cards, byQuestId, x, y);
    }

    public static boolean jumpToBestMatch(TabletUiState state) {
        return CanvasLayoutService.jumpToBestMatch(state);
    }

    public static boolean matchesSearchOnly(CompoundTag questTag, String search) {
        return CanvasLayoutService.matchesSearchOnly(questTag, search);
    }


    public static int textSelectionStart(TabletUiState state) {
        return CanvasTextRenderer.textSelectionStart(state);
    }

    public static int textSelectionEnd(TabletUiState state) {
        return CanvasTextRenderer.textSelectionEnd(state);
    }

    public static boolean hasTextSelection(TabletUiState state) {
        return CanvasTextRenderer.hasTextSelection(state);
    }

    public static CanvasTextLayer applyTextStyleSelection(TabletUiState state, CanvasTextLayer text, String style) {
        return CanvasTextRenderer.applyTextStyleSelection(state, text, style);
    }

    public static CanvasTextLayer toggleTextStyleSelection(TabletUiState state, CanvasTextLayer text, String flag) {
        return CanvasTextRenderer.toggleTextStyleSelection(state, text, flag);
    }

    public static boolean isTextStyleFlagActive(TabletUiState state, CanvasTextLayer text, String flag) {
        return CanvasTextRenderer.isTextStyleFlagActive(state, text, flag);
    }

    public static int activeTextColor(TabletUiState state, CanvasTextLayer text) {
        return CanvasTextRenderer.activeTextColor(state, text);
    }

    public static CanvasTextLayer applyTextColorSelection(TabletUiState state, CanvasTextLayer text, int color) {
        return CanvasTextRenderer.applyTextColorSelection(state, text, color);
    }

    private static void renderCanvasMetaPanels(
            CanvasViewport canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> visibleCards,
            Map<String, QuestCardLayout> byQuestId,
            int contentX,
            int contentY,
            int contentW,
            int contentH
    ) {
        CanvasOverlayController.renderCanvasMetaPanels(canvasViewport, state, visibleCards, byQuestId, contentX, contentY, contentW, contentH);
    }

    public static boolean isContextMenuHit(TabletUiState state, int x, int y) {
        return CanvasOverlayController.isContextMenuHit(state, x, y);
    }

    public static boolean isSelectionBoundsHit(TabletUiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionBoundsHit(state, x, y);
    }

    public static boolean isSelectionResizeHandleHit(TabletUiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionResizeHandleHit(state, x, y);
    }

    public static boolean isSelectionRotateHandleHit(TabletUiState state, int x, int y) {
        return CanvasSelectionRenderer.isSelectionRotateHandleHit(state, x, y);
    }

    public static void drawSelectionForeground(
            GuiGraphics graphics,
            TabletUiState state,
            List<QuestCardLayout> cards,
            int originX,
            int originY,
            int maxW,
            int maxH
    ) {
        CanvasSelectionRenderer.drawSelectionForeground(graphics, state, cards, originX, originY, maxW, maxH);
    }

    public static float clampZoom(float zoom) {
        if (Float.isNaN(zoom) || Float.isInfinite(zoom)) {
            return 1.0f;
        }
        return Math.max(MIN_CANVAS_ZOOM, Math.min(MAX_CANVAS_ZOOM, zoom));
    }

}
