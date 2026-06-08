package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.hit.CanvasHitTester;
import com.abo47.questsandstuff.client.tablet.quest.canvas.layer.CanvasElementStore;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasChapterSwitchAnimation;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasSelectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_LIMIT_HEIGHT;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_LIMIT_WIDTH;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class CanvasRenderer {
    public static final float MIN_CANVAS_ZOOM = 0.5f;
    public static final float MAX_CANVAS_ZOOM = 3.0f;

    private CanvasRenderer() {
    }
    public static void rebuildQuestCanvas(CanvasViewport canvasViewport, TabletUiState state) {
        CanvasCameraController.beforeCanvasRebuild(state);
        canvasViewport.clearAllWidgets();
        String selectedGroup = selectedGroupName(state);
        state.canvasZoom = clampZoom(state.canvasZoom);
        CanvasChapterSwitchAnimation.trackSelectedGroup(state, selectedGroup);
        CanvasSceneRenderer.applyCanvasBackground(canvasViewport);
        List<Map.Entry<String, CompoundTag>> quests = new ArrayList<>(ClientQuestCache.questEntries());
        quests.sort(Comparator.comparing(Map.Entry::getKey));

        int viewportW = canvasViewport.getSize().width;
        int viewportH = canvasViewport.getSize().height;
        int usableW = Math.max(1, viewportW - 1);
        int usableH = Math.max(1, viewportH - 1);
        if (state.canvasLimitEnabled) {
            usableW = Math.min(usableW, CANVAS_LIMIT_WIDTH[state.canvasLimitIndex]);
            usableH = Math.min(usableH, CANVAS_LIMIT_HEIGHT[state.canvasLimitIndex]);
        }
        int cell = CanvasGeometry.gridSize(state);
        int contentW = CanvasSceneRenderer.snapCanvasContentSize(usableW, cell);
        int contentH = CanvasSceneRenderer.snapCanvasContentSize(usableH, cell);
        int contentX = Math.max(0, (usableW - contentW) / 2);
        int contentY = Math.max(0, (usableH - contentH) / 2);
        state.canvasContentX = contentX;
        state.canvasContentY = contentY;
        state.canvasContentW = contentW;
        state.canvasContentH = contentH;
        CanvasCameraController.afterCanvasLayout(state, selectedGroup);
        CanvasSceneRenderer.renderCanvasSurfaces(canvasViewport, state, contentX, contentY, contentW, contentH, viewportW, viewportH);

        if (state.canvasLimitEnabled && (contentW < viewportW - 12 || contentH < viewportH - 12)) {
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
        state.selectedQuestIds.retainAll(byQuestId.keySet());
        if (!state.connectSourceQuestId.isBlank() && !ClientQuestCache.containsQuest(state.connectSourceQuestId)) {
            state.connectSourceQuestId = "";
        }
        state.connectSourceQuestIds.removeIf(questId -> !ClientQuestCache.containsQuest(questId));
        WidgetGroup canvasContent = new WidgetGroup(0, 0, viewportW, viewportH);
        if (state.canEdit && state.gridEnabled) {
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
        if (!state.canEdit) {
            state.contextMenuOpen = false;
            state.contextMenuRows = 0;
            state.contextMenuScroll = 0;
            state.contextMenuScrollMax = 0;
            state.contextMenuScrollDragging = false;
            state.createQuestModalOpen = false;
            state.boxSelecting = false;
        }
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
        if (!state.canEdit && isVisualHiddenOutsideEdit(questTag)) {
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

    public static void setConnectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, int color) {
        ConnectionRenderer.setConnectionColor(state, group, sourceQuestId, targetQuestId, color);
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionHidden(state, group, sourceQuestId, targetQuestId);
    }

    public static void setConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean hidden) {
        ConnectionRenderer.setConnectionHidden(state, group, sourceQuestId, targetQuestId, hidden);
    }

    public static void toggleConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        ConnectionRenderer.toggleConnectionHidden(state, group, sourceQuestId, targetQuestId);
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return ConnectionRenderer.isConnectionDirect(state, group, sourceQuestId, targetQuestId);
    }

    public static void toggleConnectionMode(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        ConnectionRenderer.toggleConnectionMode(state, group, sourceQuestId, targetQuestId);
    }

    public static int snapToGrid(TabletUiState state, int value) {
        return CanvasGeometry.snapToGrid(state, value);
    }

    public static void moveQuestLayer(TabletUiState state, String group, String questId, boolean front) {
        CanvasLayerOrdering.moveQuestLayer(state, group, questId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveImageLayer(TabletUiState state, String group, String imageId, boolean front) {
        CanvasLayerOrdering.moveImageLayer(state, group, imageId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveTextLayer(TabletUiState state, String group, String textId, boolean front) {
        CanvasLayerOrdering.moveTextLayer(state, group, textId, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveConnectionLayer(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean front) {
        CanvasLayerOrdering.moveConnectionLayer(state, group, ConnectionRenderer.edgeKey(sourceQuestId, targetQuestId), front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static void moveCanvasLayers(TabletUiState state, String group, List<String> layerKeys, boolean front) {
        CanvasLayerOrdering.moveLayers(state, group, layerKeys, front);
        CanvasElementStore.persistLayerOrder(state, group);
    }

    public static boolean isImageAboveQuest(TabletUiState state, String group, String imageId, String questId) {
        return CanvasLayerOrdering.isImageAboveQuest(state, group, imageId, questId);
    }

    public static boolean isTextAboveQuest(TabletUiState state, String group, String textId, String questId) {
        return CanvasLayerOrdering.isTextAboveQuest(state, group, textId, questId);
    }

    public static boolean isTextAboveImage(TabletUiState state, String group, String textId, String imageId) {
        return CanvasLayerOrdering.isTextAboveImage(state, group, textId, imageId);
    }

    public static boolean isImageSelected(TabletUiState state, String imageId) {
        return imageId != null && (imageId.equals(state.selectedCanvasImageId) || state.selectedCanvasImageIds.contains(imageId));
    }

    public static boolean isTextSelected(TabletUiState state, String textId) {
        return textId != null && (textId.equals(state.selectedCanvasTextId) || state.selectedCanvasTextIds.contains(textId));
    }

    public static Set<String> selectedCanvasImageIds(TabletUiState state) {
        Set<String> images = new LinkedHashSet<>(state.selectedCanvasImageIds);
        if (!state.selectedCanvasImageId.isBlank()) {
            images.add(state.selectedCanvasImageId);
        }
        return images;
    }

    public static Set<String> selectedCanvasTextIds(TabletUiState state) {
        Set<String> texts = new LinkedHashSet<>(state.selectedCanvasTextIds);
        if (!state.selectedCanvasTextId.isBlank()) {
            texts.add(state.selectedCanvasTextId);
        }
        return texts;
    }

    public static int totalCanvasSelectionCount(TabletUiState state) {
        return CanvasSelectionSet.current(state).size();
    }

    public static void clearCanvasSelection(TabletUiState state) {
        state.selectedQuestIds.clear();
        state.selectedCanvasImageId = "";
        state.selectedCanvasTextId = "";
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasTextIds.clear();
        CanvasTransformSessions.clearMainCanvasSession(state);
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

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image) {
        CanvasElementStore.putCanvasImage(state, group, image);
    }

    public static void putCanvasImage(TabletUiState state, String group, CanvasImageLayer image, boolean syncServer) {
        CanvasElementStore.putCanvasImage(state, group, image, syncServer);
    }

    public static boolean removeCanvasImage(TabletUiState state, String group, String imageId) {
        return CanvasElementStore.removeCanvasImage(state, group, imageId);
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text) {
        CanvasElementStore.putCanvasText(state, group, text);
    }

    public static void putCanvasText(TabletUiState state, String group, CanvasTextLayer text, boolean syncServer) {
        CanvasElementStore.putCanvasText(state, group, text, syncServer);
    }

    public static boolean removeCanvasText(TabletUiState state, String group, String textId) {
        return CanvasElementStore.removeCanvasText(state, group, textId);
    }

    public static CanvasTextLayer findCanvasText(TabletUiState state, String group, String textId) {
        return CanvasElementStore.findCanvasText(state, group, textId);
    }

    public static CanvasImageLayer findCanvasImage(TabletUiState state, String group, String imageId) {
        return CanvasElementStore.findCanvasImage(state, group, imageId);
    }

    public static CanvasImageLayer effectiveCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.transientCanvasImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.transientCanvasTexts.getOrDefault(text.id(), text);
    }

    public static CanvasImageLayer effectiveQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null) {
            return image;
        }
        return state.questDetailsTransientImages.getOrDefault(image.id(), image);
    }

    public static CanvasTextLayer effectiveQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null) {
            return text;
        }
        return state.questDetailsTransientTexts.getOrDefault(text.id(), text);
    }

    public static void putTransientCanvasImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.transientCanvasImages.put(image.id(), image);
    }

    public static void putTransientCanvasText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.transientCanvasTexts.put(text.id(), text);
    }

    public static void putTransientQuestDetailsImage(TabletUiState state, CanvasImageLayer image) {
        if (state == null || image == null || image.id().isBlank()) {
            return;
        }
        state.questDetailsTransientImages.put(image.id(), image);
    }

    public static void putTransientQuestDetailsText(TabletUiState state, CanvasTextLayer text) {
        if (state == null || text == null || text.id().isBlank()) {
            return;
        }
        state.questDetailsTransientTexts.put(text.id(), text);
    }

    public static boolean commitTransientCanvasImage(TabletUiState state, String group, String imageId) {
        if (state == null || group == null || group.isBlank() || imageId == null || imageId.isBlank()) {
            return false;
        }
        CanvasImageLayer preview = state.transientCanvasImages.remove(imageId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasImage(state, group, preview, false);
        return true;
    }

    public static boolean commitTransientCanvasText(TabletUiState state, String group, String textId) {
        if (state == null || group == null || group.isBlank() || textId == null || textId.isBlank()) {
            return false;
        }
        CanvasTextLayer preview = state.transientCanvasTexts.remove(textId);
        if (preview == null) {
            return false;
        }
        CanvasElementStore.putCanvasText(state, group, preview, false);
        return true;
    }

    public static void commitSelectedTransientCanvasLayers(TabletUiState state, String group) {
        for (String imageId : selectedCanvasImageIds(state)) {
            commitTransientCanvasImage(state, group, imageId);
        }
        for (String textId : selectedCanvasTextIds(state)) {
            commitTransientCanvasText(state, group, textId);
        }
    }

    public static void updateCanvasText(TabletUiState state, String group, String textId, java.util.function.UnaryOperator<CanvasTextLayer> updater) {
        CanvasElementStore.updateCanvasText(state, group, textId, updater);
    }

    public static void persistCanvasImage(TabletUiState state, String group, String imageId) {
        CanvasElementStore.persistCanvasImage(state, group, imageId);
    }

    public static void persistCanvasText(TabletUiState state, String group, String textId) {
        CanvasElementStore.persistCanvasText(state, group, textId);
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
