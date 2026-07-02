package com.abo47.questsandstuff.client.tablet.quest.canvas;


import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestMatch;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasLayoutService {
    private static final int MIN_PAN_RENDER_OVERSCAN = 192;

    private CanvasLayoutService() {
    }

    public static boolean jumpToBestMatch(TabletUiState state) {
        if (state.root.search.isBlank()) {
            return false;
        }

        QuestMatch inGroup = null;
        QuestMatch crossGroup = null;
        List<Map.Entry<String, CompoundTag>> quests = new ArrayList<>(ClientQuestStateFacade.questEntries());
        quests.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<String, CompoundTag> quest : quests) {
            if (!matchesSearchOnly(quest.getValue(), state.root.search)) {
                continue;
            }
            Set<String> groups = quest.getValue().getCompound("chapters").getAllKeys();
            if (groups.contains(state.root.selectedChapter)) {
                inGroup = new QuestMatch(quest.getKey(), state.root.selectedChapter);
                break;
            }
            String firstGroup = groups.stream().sorted().findFirst().orElse("");
            if (crossGroup == null) {
                crossGroup = new QuestMatch(quest.getKey(), firstGroup);
            }
        }

        QuestMatch selected = inGroup != null ? inGroup : crossGroup;
        if (selected == null) {
            return false;
        }
        state.root.selectedChapter = selected.group();
        state.chapterPanel.lastJumpQuest = selected.questId();
        state.canvas.pendingCameraGroup = selected.group();
        state.canvas.pendingCameraQuestId = selected.questId();
        persistUiState(state);
        return true;
    }

    public static boolean matchesSearchOnly(CompoundTag questTag, String search) {
        String normalized = SearchFilter.normalize(search);
        if (normalized.isBlank()) {
            return true;
        }
        return SearchFilter.matches(normalized, questTag.getString("title"), questTag.getString("subtitle"));
    }

    public static List<QuestCardLayout> layoutVisibleCards(
            List<Map.Entry<String, CompoundTag>> quests,
            TabletUiState state
    ) {
        List<QuestCardLayout> visibleCards = new ArrayList<>();
        String selectedChapter = selectedChapterName(state);
        for (Map.Entry<String, CompoundTag> entry : quests) {
            if (!CanvasRenderer.matchesFilters(entry.getValue(), state)) {
                continue;
            }
            visibleCards.add(CanvasGeometry.layoutQuest(entry.getKey(), entry.getValue(), state, selectedChapter));
        }
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(selectedChapter, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByChapter.getOrDefault(selectedChapter, List.of());
        Map<String, QuestCardLayout> byQuestId = new HashMap<>();
        for (QuestCardLayout card : visibleCards) {
            byQuestId.put(card.questId(), card);
        }
        List<String> connectionKeys = ConnectionRenderer.prerequisiteConnectionLayerKeys(state, visibleCards, byQuestId, 1_000_000, 1_000_000);
        List<CanvasExclusiveChoice> exclusiveChoices = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(selectedChapter, List.of());
        List<String> layerOrder = CanvasLayerOrdering.normalize(state, selectedChapter, visibleCards, images, texts, connectionKeys, exclusiveChoices);
        Map<String, Integer> layerIndexes = CanvasLayerOrdering.indexMap(layerOrder);
        visibleCards.sort(Comparator.comparingInt(card -> CanvasLayerOrdering.layerIndex(layerIndexes, CanvasLayerOrdering.questKey(card.questId()))));
        return visibleCards;
    }

    public static int panRenderOverscanX(int viewportW) {
        return Math.max(MIN_PAN_RENDER_OVERSCAN, viewportW * 3);
    }

    public static int panRenderOverscanY(int viewportH) {
        return Math.max(MIN_PAN_RENDER_OVERSCAN, viewportH * 3);
    }

    public static boolean intersectsPanRenderWindow(QuestCardLayout card, int viewportW, int viewportH) {
        int marginX = panRenderOverscanX(viewportW);
        int marginY = panRenderOverscanY(viewportH);
        return card.x() + card.width() >= -marginX
                && card.y() + card.height() >= -marginY
                && card.x() <= viewportW + marginX
                && card.y() <= viewportH + marginY;
    }

    public static void clampCanvasOffset(TabletUiState state, List<QuestCardLayout> cards, int contentW, int contentH) {
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(selectedChapterName(state), List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByChapter.getOrDefault(selectedChapterName(state), List.of());
        if (cards.isEmpty() && images.isEmpty() && texts.isEmpty()) {
            int worldW = Math.max(1, Math.round(contentW / CanvasRenderer.clampZoom(state.canvas.canvasZoom)));
            int worldH = Math.max(1, Math.round(contentH / CanvasRenderer.clampZoom(state.canvas.canvasZoom)));
            setCanvasWorldBounds(state, 0, 0, worldW, worldH);
            if (state.canvas.gridCanvasLocked) {
                clampLockedCanvasOffset(state, contentW, contentH);
            }
            CanvasCameraController.rememberCurrentGroup(state);
            return;
        }

        int minLogicalX = Integer.MAX_VALUE;
        int minLogicalY = Integer.MAX_VALUE;
        int maxLogicalX = Integer.MIN_VALUE;
        int maxLogicalY = Integer.MIN_VALUE;

        for (QuestCardLayout card : cards) {
            minLogicalX = Math.min(minLogicalX, card.visualLogicalX());
            minLogicalY = Math.min(minLogicalY, card.visualLogicalY());
            maxLogicalX = Math.max(maxLogicalX, card.logicalRight());
            maxLogicalY = Math.max(maxLogicalY, card.logicalBottom());
        }
        for (CanvasImageLayer image : images) {
            minLogicalX = Math.min(minLogicalX, image.x());
            minLogicalY = Math.min(minLogicalY, image.y());
            maxLogicalX = Math.max(maxLogicalX, image.x() + image.w());
            maxLogicalY = Math.max(maxLogicalY, image.y() + image.h());
        }
        for (CanvasTextLayer text : texts) {
            minLogicalX = Math.min(minLogicalX, text.x());
            minLogicalY = Math.min(minLogicalY, text.y());
            maxLogicalX = Math.max(maxLogicalX, text.x() + text.w());
            maxLogicalY = Math.max(maxLogicalY, text.y() + text.h());
        }

        setCanvasWorldBounds(state, minLogicalX, minLogicalY, maxLogicalX, maxLogicalY);

        if (!state.root.canEdit && QuestsAndStuffConfig.readOnlyCanvasFocusEnabled()) {
            CanvasPoint clamped = CanvasCameraController.clampedOffsetToWorldBounds(
                    state,
                    state.canvas.canvasOffsetX,
                    state.canvas.canvasOffsetY,
                    minLogicalX,
                    minLogicalY,
                    maxLogicalX,
                    maxLogicalY
            );
            state.canvas.canvasOffsetX = clamped.x;
            state.canvas.canvasOffsetY = clamped.y;
            CanvasCameraController.rememberCurrentGroup(state);
            return;
        }
        if (state.canvas.gridCanvasLocked) {
            clampLockedCanvasOffset(state, contentW, contentH);
        }
        CanvasCameraController.rememberCurrentGroup(state);
    }

    private static void setCanvasWorldBounds(TabletUiState state, int minLogicalX, int minLogicalY, int maxLogicalX, int maxLogicalY) {
        int width = Math.max(1, maxLogicalX - minLogicalX);
        int height = Math.max(1, maxLogicalY - minLogicalY);
        state.canvas.minimapWorldMinX = minLogicalX;
        state.canvas.minimapWorldMinY = minLogicalY;
        state.canvas.minimapWorldWidth = width;
        state.canvas.minimapWorldHeight = height;
        state.canvas.canvasNavigationMinX = minLogicalX;
        state.canvas.canvasNavigationMinY = minLogicalY;
        state.canvas.canvasNavigationWidth = width;
        state.canvas.canvasNavigationHeight = height;
    }

    private static void clampLockedCanvasOffset(TabletUiState state, int contentW, int contentH) {
        float zoom = CanvasRenderer.clampZoom(state.canvas.canvasZoom);
        int scaledW = Math.max(contentW, Math.round(contentW * zoom));
        int scaledH = Math.max(contentH, Math.round(contentH * zoom));
        int minX = Math.min(0, contentW - scaledW);
        int minY = Math.min(0, contentH - scaledH);
        state.canvas.canvasOffsetX = Math.max(minX, Math.min(0, state.canvas.canvasOffsetX));
        state.canvas.canvasOffsetY = Math.max(minY, Math.min(0, state.canvas.canvasOffsetY));
    }
}
