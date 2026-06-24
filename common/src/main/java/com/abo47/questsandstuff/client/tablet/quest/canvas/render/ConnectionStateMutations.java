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

    static void setEcConnectionTexture(TabletUiState state, String group, String ecId, String questId, String texture) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withConnectionTexture(ecId, texture));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withConnectionTexture(questId, texture));
    }

    static void removeEcConnectionTexture(TabletUiState state, String group, String ecId, String questId) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withoutConnectionTexture(ecId));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withoutConnectionTexture(questId));
    }

    static void setEcConnectionTextureSpacing(TabletUiState state, String group, String ecId, String questId, int spacing) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withConnectionTextureSpacing(ecId, spacing));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withConnectionTextureSpacing(questId, spacing));
    }

    static void removeEcConnectionTextureSpacing(TabletUiState state, String group, String ecId, String questId) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withoutConnectionTextureSpacing(ecId));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withoutConnectionTextureSpacing(questId));
    }

    static void setEcConnectionHidden(TabletUiState state, String group, String ecId, String questId, boolean hidden) {
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecId);
        if (ec == null) {
            CanvasExclusiveChoice other = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, questId);
            if (other == null) return;
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, other.withHiddenConnection(ecId, hidden));
            return;
        }
        CanvasLayerMutations.putCanvasExclusiveChoice(state, group, ec.withHiddenConnection(questId, hidden));
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
        Map<String, String> textures = state.canvas.connectionTexturesByGroup.get(group);
        if (textures != null) {
            textures.remove(key);
            if (textures.isEmpty()) state.canvas.connectionTexturesByGroup.remove(group);
        }
        Map<String, Integer> spacings = state.canvas.connectionTextureSpacingsByGroup.get(group);
        if (spacings != null) {
            spacings.remove(key);
            if (spacings.isEmpty()) state.canvas.connectionTextureSpacingsByGroup.remove(group);
        }
        Map<String, Integer> colors = state.canvas.connectionColorsByGroup.get(group);
        if (colors != null) {
            colors.remove(key);
            if (colors.isEmpty()) state.canvas.connectionColorsByGroup.remove(group);
        }
        Set<String> grid = state.canvas.gridConnectionsByGroup.get(group);
        if (grid != null) {
            grid.remove(key);
            if (grid.isEmpty()) state.canvas.gridConnectionsByGroup.remove(group);
        }
        Set<String> hidden = state.canvas.hiddenConnectionsByGroup.get(group);
        if (hidden != null) {
            hidden.remove(key);
            if (hidden.isEmpty()) state.canvas.hiddenConnectionsByGroup.remove(group);
        }
    }
}
