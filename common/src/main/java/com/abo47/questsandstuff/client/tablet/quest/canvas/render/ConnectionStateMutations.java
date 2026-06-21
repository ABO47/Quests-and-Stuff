package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class ConnectionStateMutations {
    private ConnectionStateMutations() {
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
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withConnectionColor(ecId, color));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withConnectionColor(questId, color));
    }

    static void removeEcConnectionColor(TabletUiState state, String group, String ecId, String questId) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withoutConnectionColor(ecId));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withoutConnectionColor(questId));
    }

    static void setEcConnectionMode(TabletUiState state, String group, String ecId, String questId, boolean direct) {
        String mode = direct ? "direct" : "grid";
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withConnectionMode(ecId, mode));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withConnectionMode(questId, mode));
    }

    static void removeEcConnectionMode(TabletUiState state, String group, String ecId, String questId) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withoutConnectionMode(ecId));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withoutConnectionMode(questId));
    }
}
