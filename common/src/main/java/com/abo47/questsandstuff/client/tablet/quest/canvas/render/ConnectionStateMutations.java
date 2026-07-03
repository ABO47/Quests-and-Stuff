package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class ConnectionStateMutations {
    private ConnectionStateMutations() {
    }

    private static void modifyEcConnection(TabletUiState state, String chapter, String ecId, String questId, Function<CanvasExclusiveChoice, CanvasExclusiveChoice> modifier) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, modifier.apply(other));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, chapter, modifier.apply(ec));
    }

    static void setConnectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, int color) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		Map<String, Integer> colors = state.canvas.connectionColorsByGroup.computeIfAbsent(chapter, ignored -> new HashMap<>());
		colors.put(key, color);
    }

    static void setConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, boolean hidden) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		Set<String> groupHidden = state.canvas.hiddenConnectionsByGroup.computeIfAbsent(chapter, ignored -> new HashSet<>());
        if (hidden) {
            groupHidden.add(key);
        } else {
            groupHidden.remove(key);
            if (groupHidden.isEmpty()) {
                state.canvas.hiddenConnectionsByGroup.remove(chapter);
            }
        }
    }

    static void toggleConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        boolean hidden = ConnectionStyleResolver.isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId));
        setConnectionHidden(state, chapter, sourceQuestId, targetQuestId, !hidden);
    }

    static void toggleConnectionMode(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		Set<String> groupGrid = state.canvas.gridConnectionsByGroup.computeIfAbsent(chapter, ignored -> new HashSet<>());
        if (ConnectionStyleResolver.isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, ClientQuestStateFacade.quest(targetQuestId))) {
            groupGrid.add(key);
        } else {
            groupGrid.remove(key);
        }
        if (groupGrid.isEmpty()) {
            state.canvas.gridConnectionsByGroup.remove(chapter);
        }
    }

    static void setEcConnectionColor(TabletUiState state, String chapter, String ecId, String questId, int color) {
        modifyEcConnection(state, chapter, ecId, questId, ec -> ec.withConnectionColor(questId, color));
    }

    static void setEcConnectionMode(TabletUiState state, String chapter, String ecId, String questId, boolean direct) {
        String mode = direct ? "direct" : "grid";
        modifyEcConnection(state, chapter, ecId, questId, ec -> ec.withConnectionMode(questId, mode));
    }

    static void setEcConnectionTexture(TabletUiState state, String chapter, String ecId, String questId, String texture) {
        modifyEcConnection(state, chapter, ecId, questId, ec -> ec.withConnectionTexture(questId, texture));
    }

    static void setEcConnectionTextureSpacing(TabletUiState state, String chapter, String ecId, String questId, int spacing) {
        modifyEcConnection(state, chapter, ecId, questId, ec -> ec.withConnectionTextureSpacing(questId, spacing));
    }

    static void setEcConnectionHidden(TabletUiState state, String chapter, String ecId, String questId, boolean hidden) {
        modifyEcConnection(state, chapter, ecId, questId, ec -> ec.withHiddenConnection(questId, hidden));
    }

    static void setConnectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, String texture) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		Map<String, String> textures = state.canvas.connectionTexturesByGroup.computeIfAbsent(chapter, ignored -> new HashMap<>());
        if (texture == null || texture.isBlank()) {
            textures.remove(key);
        } else {
            textures.put(key, texture);
        }
    }

    static void setConnectionTextureSpacing(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, int spacing) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		Map<String, Integer> spacings = state.canvas.connectionTextureSpacingsByGroup.computeIfAbsent(chapter, ignored -> new HashMap<>());
        if (spacing <= 0) {
            spacings.remove(key);
        } else {
            spacings.put(key, spacing);
        }
    }

	static void removeConnectionTransientState(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
		String key = QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId);
		removeFromMap(state.canvas.connectionTexturesByGroup, chapter, key);
        removeFromMap(state.canvas.connectionTextureSpacingsByGroup, chapter, key);
        removeFromMap(state.canvas.connectionColorsByGroup, chapter, key);
        removeFromSet(state.canvas.gridConnectionsByGroup, chapter, key);
        removeFromSet(state.canvas.hiddenConnectionsByGroup, chapter, key);
    }

    private static <V> void removeFromMap(Map<String, Map<String, V>> groupMap, String chapter, String key) {
        Map<String, V> map = groupMap.get(chapter);
        if (map == null) return;
        map.remove(key);
        if (map.isEmpty()) groupMap.remove(chapter);
    }

    private static void removeFromSet(Map<String, Set<String>> groupMap, String chapter, String key) {
        Set<String> set = groupMap.get(chapter);
        if (set == null) return;
        set.remove(key);
        if (set.isEmpty()) groupMap.remove(chapter);
    }
}
