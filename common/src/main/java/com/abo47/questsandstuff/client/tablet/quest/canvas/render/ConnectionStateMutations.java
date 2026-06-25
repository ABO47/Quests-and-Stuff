package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

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

    private static void modifyEcConnection(TabletUiState state, String group, String ecId, String questId, Function<CanvasExclusiveChoice, CanvasExclusiveChoice> modifier) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, modifier.apply(other));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, modifier.apply(ec));
    }

    static void setConnectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, int color) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        Map<String, Integer> colors = state.canvas.connectionColorsByGroup.computeIfAbsent(group, ignored -> new HashMap<>());
        colors.put(key, color);
    }

    static void setConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean hidden) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        Set<String> groupHidden = state.canvas.hiddenConnectionsByGroup.computeIfAbsent(group, ignored -> new HashSet<>());
        if (hidden) {
            groupHidden.add(key);
        } else {
            groupHidden.remove(key);
            if (groupHidden.isEmpty()) {
                state.canvas.hiddenConnectionsByGroup.remove(group);
            }
        }
    }

    static void toggleConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        boolean hidden = ConnectionStyleResolver.isConnectionHidden(state, group, sourceQuestId, targetQuestId);
        setConnectionHidden(state, group, sourceQuestId, targetQuestId, !hidden);
    }

    static void toggleConnectionMode(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        Set<String> groupGrid = state.canvas.gridConnectionsByGroup.computeIfAbsent(group, ignored -> new HashSet<>());
        if (ConnectionStyleResolver.isConnectionDirect(state, group, sourceQuestId, targetQuestId)) {
            groupGrid.add(key);
        } else {
            groupGrid.remove(key);
        }
        if (groupGrid.isEmpty()) {
            state.canvas.gridConnectionsByGroup.remove(group);
        }
    }

    static void setEcConnectionColor(TabletUiState state, String group, String ecId, String questId, int color) {
        modifyEcConnection(state, group, ecId, questId, ec -> ec.withConnectionColor(questId, color));
    }

    static void setEcConnectionMode(TabletUiState state, String group, String ecId, String questId, boolean direct) {
        String mode = direct ? "direct" : "grid";
        modifyEcConnection(state, group, ecId, questId, ec -> ec.withConnectionMode(questId, mode));
    }

    static void setEcConnectionTexture(TabletUiState state, String group, String ecId, String questId, String texture) {
        modifyEcConnection(state, group, ecId, questId, ec -> ec.withConnectionTexture(questId, texture));
    }

    static void setEcConnectionTextureSpacing(TabletUiState state, String group, String ecId, String questId, int spacing) {
        modifyEcConnection(state, group, ecId, questId, ec -> ec.withConnectionTextureSpacing(questId, spacing));
    }

    static void setEcConnectionHidden(TabletUiState state, String group, String ecId, String questId, boolean hidden) {
        modifyEcConnection(state, group, ecId, questId, ec -> ec.withHiddenConnection(questId, hidden));
    }

    static void setConnectionTexture(TabletUiState state, String group, String sourceQuestId, String targetQuestId, String texture) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        Map<String, String> textures = state.canvas.connectionTexturesByGroup.computeIfAbsent(group, ignored -> new HashMap<>());
        if (texture == null || texture.isBlank()) {
            textures.remove(key);
        } else {
            textures.put(key, texture);
        }
    }

    static void setConnectionTextureSpacing(TabletUiState state, String group, String sourceQuestId, String targetQuestId, int spacing) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        Map<String, Integer> spacings = state.canvas.connectionTextureSpacingsByGroup.computeIfAbsent(group, ignored -> new HashMap<>());
        if (spacing <= 0) {
            spacings.remove(key);
        } else {
            spacings.put(key, spacing);
        }
    }

    static void removeEdgeTransientState(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        String key = QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
        removeFromMap(state.canvas.connectionTexturesByGroup, group, key);
        removeFromMap(state.canvas.connectionTextureSpacingsByGroup, group, key);
        removeFromMap(state.canvas.connectionColorsByGroup, group, key);
        removeFromSet(state.canvas.gridConnectionsByGroup, group, key);
        removeFromSet(state.canvas.hiddenConnectionsByGroup, group, key);
    }

    private static <V> void removeFromMap(Map<String, Map<String, V>> groupMap, String group, String key) {
        Map<String, V> map = groupMap.get(group);
        if (map == null) return;
        map.remove(key);
        if (map.isEmpty()) groupMap.remove(group);
    }

    private static void removeFromSet(Map<String, Set<String>> groupMap, String group, String key) {
        Set<String> set = groupMap.get(group);
        if (set == null) return;
        set.remove(key);
        if (set.isEmpty()) groupMap.remove(group);
    }
}
