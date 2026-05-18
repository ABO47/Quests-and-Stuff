package com.abo47.questsandstuff.client.canvas;


import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.model.QuestMatch;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;

public final class CanvasLayoutService {
    private CanvasLayoutService() {
    }

    public static boolean jumpToBestMatch(TabletUiState state) {
        if (state.search.isBlank()) {
            return false;
        }

        QuestMatch inGroup = null;
        QuestMatch crossGroup = null;
        List<Map.Entry<String, CompoundTag>> quests = new ArrayList<>(ClientQuestCache.quests().entrySet());
        quests.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<String, CompoundTag> quest : quests) {
            if (!matchesSearchOnly(quest.getValue(), state.search)) {
                continue;
            }
            Set<String> groups = quest.getValue().getCompound("groups").getAllKeys();
            if (groups.contains(state.selectedGroup)) {
                inGroup = new QuestMatch(quest.getKey(), state.selectedGroup);
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
        state.selectedGroup = selected.group();
        state.lastJumpQuest = selected.questId();
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
        String selectedGroup = selectedGroupName(state);
        for (Map.Entry<String, CompoundTag> entry : quests) {
            if (!CanvasRenderer.matchesFilters(entry.getValue(), state)) {
                continue;
            }
            visibleCards.add(CanvasGeometry.layoutQuest(entry.getKey(), entry.getValue(), state, selectedGroup));
        }
        List<CanvasImageLayer> images = state.canvasImagesByGroup.getOrDefault(selectedGroup, List.of());
        List<CanvasTextLayer> texts = state.canvasTextsByGroup.getOrDefault(selectedGroup, List.of());
        List<String> layerOrder = CanvasLayerOrdering.normalize(state, selectedGroup, visibleCards, images, texts);
        visibleCards.sort(Comparator.comparingInt(card -> CanvasLayerOrdering.layerIndex(layerOrder, CanvasLayerOrdering.questKey(card.questId()))));
        return visibleCards;
    }

    public static void clampCanvasOffset(TabletUiState state, List<QuestCardLayout> cards, int contentW, int contentH) {
        List<CanvasImageLayer> images = state.canvasImagesByGroup.getOrDefault(selectedGroupName(state), List.of());
        List<CanvasTextLayer> texts = state.canvasTextsByGroup.getOrDefault(selectedGroupName(state), List.of());
        if (cards.isEmpty() && images.isEmpty() && texts.isEmpty()) {
            state.minimapWorldMinX = 0;
            state.minimapWorldMinY = 0;
            state.minimapWorldWidth = Math.max(1, Math.round(contentW / CanvasRenderer.clampZoom(state.canvasZoom)));
            state.minimapWorldHeight = Math.max(1, Math.round(contentH / CanvasRenderer.clampZoom(state.canvasZoom)));
            if (state.gridCanvasLocked) {
                clampLockedCanvasOffset(state, contentW, contentH);
            }
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

        state.minimapWorldMinX = minLogicalX;
        state.minimapWorldMinY = minLogicalY;
        state.minimapWorldWidth = Math.max(1, maxLogicalX - minLogicalX);
        state.minimapWorldHeight = Math.max(1, maxLogicalY - minLogicalY);

        if (!state.canEdit) {
            clampOffsetToElementBounds(state, minLogicalX, minLogicalY, maxLogicalX, maxLogicalY, contentW, contentH);
            return;
        }
        if (state.gridCanvasLocked) {
            clampLockedCanvasOffset(state, contentW, contentH);
        }
    }

    private static void clampOffsetToElementBounds(TabletUiState state, int minLogicalX, int minLogicalY, int maxLogicalX, int maxLogicalY, int contentW, int contentH) {
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        int pad = 24;
        int boundsW = Math.max(1, maxLogicalX - minLogicalX);
        int boundsH = Math.max(1, maxLogicalY - minLogicalY);
        int scaledBoundsW = Math.round(boundsW * zoom);
        int scaledBoundsH = Math.round(boundsH * zoom);

        if (scaledBoundsW + pad * 2 <= contentW) {
            state.canvasOffsetX = Math.round((contentW - scaledBoundsW) / 2.0f - minLogicalX * zoom);
        } else {
            int minOffset = Math.round(contentW - pad - maxLogicalX * zoom);
            int maxOffset = Math.round(pad - minLogicalX * zoom);
            state.canvasOffsetX = Math.max(minOffset, Math.min(maxOffset, state.canvasOffsetX));
        }

        if (scaledBoundsH + pad * 2 <= contentH) {
            state.canvasOffsetY = Math.round((contentH - scaledBoundsH) / 2.0f - minLogicalY * zoom);
        } else {
            int minOffset = Math.round(contentH - pad - maxLogicalY * zoom);
            int maxOffset = Math.round(pad - minLogicalY * zoom);
            state.canvasOffsetY = Math.max(minOffset, Math.min(maxOffset, state.canvasOffsetY));
        }
    }

    private static void clampLockedCanvasOffset(TabletUiState state, int contentW, int contentH) {
        float zoom = CanvasRenderer.clampZoom(state.canvasZoom);
        int scaledW = Math.max(contentW, Math.round(contentW * zoom));
        int scaledH = Math.max(contentH, Math.round(contentH * zoom));
        int minX = Math.min(0, contentW - scaledW);
        int minY = Math.min(0, contentH - scaledH);
        state.canvasOffsetX = Math.max(minX, Math.min(0, state.canvasOffsetX));
        state.canvasOffsetY = Math.max(minY, Math.min(0, state.canvasOffsetY));
    }
}
