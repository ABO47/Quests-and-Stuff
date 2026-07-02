package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletKeybindings;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasConnectionAnimation;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorGroupCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasConnectionClickActions {
    private CanvasConnectionClickActions() {
    }

    static boolean handleQuickConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, CanvasExclusiveChoice ecHit, int button) {
        if (shouldClearQuickConnect(state)) {
            clearQuickConnect(state);
        }
        if (button != 0 || !isQuickConnectActive(state)) {
            return false;
        }
        if (hit != null && !state.canvas.quickConnectEcId.isBlank()) {
            return connectEcToQuest(state, refresher, hit);
        }
        if (!state.canvas.quickConnectEcId.isBlank()) {
            return true;
        }
        if (hit == null && ecHit != null) {
            return connectQuestToEc(state, refresher, ecHit);
        }
        if (hit == null) {
            return true;
        }
        if (state.canvas.quickConnectSourceQuestId.isBlank() || state.canvas.quickConnectSourceQuestId.equals(hit.questId())) {
            return setQuickConnectSource(state, refresher, hit);
        }
        return finalizeQuickConnect(state, player, refresher, hit);
    }

    static boolean handlePendingConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, CanvasExclusiveChoice ecHit, int button) {
        if (button != 0 || !hasPendingConnectSource(state)) {
            return false;
        }
        if (!state.canvas.connectEcId.isBlank()) {
            return handlePendingEcConnect(state, refresher, hit);
        }
        if (!state.canvas.connectSourceQuestId.isBlank() && ecHit != null && hit == null) {
            return handlePendingEcPrerequisite(state, refresher, ecHit);
        }
        if (hit == null) {
            return cancelPendingConnect(state, refresher);
        }
        return createPendingConnections(state, player, refresher, hit);
    }

    static boolean handleConnectMode(CanvasViewport canvasViewport, TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        if (hit == null) {
            return true;
        }
        if (state.canvas.connectSourceQuestId.isBlank() || state.canvas.connectSourceQuestId.equals(hit.questId())) {
            state.canvas.connectSourceQuestId = hit.questId();
            state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
            refresher.run();
            return true;
        }
        return finalizeConnectMode(canvasViewport, state, player, refresher, hit, button);
    }

    private static boolean shouldClearQuickConnect(TabletUiState state) {
        return !isQuickConnectActive(state) && !state.canvas.quickConnectSourceQuestId.isBlank() && !state.canvas.quickConnectEcId.isBlank();
    }

    private static void clearQuickConnect(TabletUiState state) {
        state.canvas.quickConnectSourceQuestId = "";
        state.canvas.quickConnectEcId = "";
    }

    private static boolean isQuickConnectActive(TabletUiState state) {
        return state.canvas.quickConnectHeld || TabletKeybindings.quickConnectDown();
    }

    private static boolean hasPendingConnectSource(TabletUiState state) {
        return !state.canvas.connectSourceQuestId.isBlank()
                || !state.canvas.connectSourceQuestIds.isEmpty()
                || !state.canvas.connectEcId.isBlank();
    }

    private static boolean connectEcToQuest(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        String group = EditorGroupCommandClient.selectedGroupName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, state.canvas.quickConnectEcId);
        if (ec != null && !ec.connectionQuestIds().contains(hit.questId())) {
            CanvasExclusiveChoice updated = ec.addConnection(hit.questId());
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, updated.id());
            CanvasConnectionAnimation.startIfNew(state, hit.questId(), state.canvas.quickConnectEcId);
        }
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect exclusive_choice={} quest={}", state.canvas.quickConnectEcId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean connectQuestToEc(TabletUiState state, Runnable refresher, CanvasExclusiveChoice ecHit) {
        if (!state.canvas.quickConnectSourceQuestId.isBlank()) {
            String group = EditorGroupCommandClient.selectedGroupName(state);
            CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecHit.id());
            if (ec != null && !ec.prerequisiteQuestIds().contains(state.canvas.quickConnectSourceQuestId)) {
                CanvasExclusiveChoice updated = ec.addPrerequisite(state.canvas.quickConnectSourceQuestId);
                CanvasLayerMutations.putCanvasExclusiveChoice(state, group, updated);
                CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, updated.id());
                CanvasConnectionAnimation.startIfNew(state, ecHit.id(), state.canvas.quickConnectSourceQuestId);
            }
            state.canvas.quickConnectEcId = ecHit.id();
            state.canvas.quickConnectSourceQuestId = "";
            state.canvas.canvasSelection.selectOnlyEc(ecHit.id());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect quest={} exclusive_choice={}", state.canvas.quickConnectSourceQuestId, ecHit.id());
            refresher.run();
            return true;
        }
        state.canvas.quickConnectEcId = ecHit.id();
        state.canvas.quickConnectSourceQuestId = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect exclusive_choice source={}", ecHit.id());
        refresher.run();
        return true;
    }

    private static boolean setQuickConnectSource(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        state.canvas.quickConnectSourceQuestId = hit.questId();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect source quest={}", hit.questId());
        refresher.run();
        return true;
    }

    private static boolean finalizeQuickConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit) {
        String sourceQuestId = state.canvas.quickConnectSourceQuestId;
        CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
        TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
        state.canvas.quickConnectSourceQuestId = hit.questId();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect prerequisite={} quest={}", sourceQuestId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean handlePendingEcConnect(TabletUiState state, Runnable refresher, QuestCardLayout hit) {
        if (hit == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec connect cancelled id={}", state.canvas.connectEcId);
            state.canvas.connectEcId = "";
            refresher.run();
            return true;
        }
        String group = EditorGroupCommandClient.selectedGroupName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, state.canvas.connectEcId);
        if (ec != null && !ec.connectionQuestIds().contains(hit.questId())) {
            CanvasExclusiveChoice updated = ec.addConnection(hit.questId());
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, updated.id());
            CanvasConnectionAnimation.startIfNew(state, hit.questId(), state.canvas.connectEcId);
        }
        state.canvas.connectEcId = "";
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.chapterPanel.lastJumpQuest = hit.questId();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec connect exclusive_choice={} quest={}", state.canvas.connectEcId, hit.questId());
        refresher.run();
        return true;
    }

    private static boolean handlePendingEcPrerequisite(TabletUiState state, Runnable refresher, CanvasExclusiveChoice ecHit) {
        String group = EditorGroupCommandClient.selectedGroupName(state);
        CanvasExclusiveChoice ec = CanvasLayerMutations.findCanvasExclusiveChoice(state, group, ecHit.id());
        if (ec != null && !ec.prerequisiteQuestIds().contains(state.canvas.connectSourceQuestId)) {
            CanvasExclusiveChoice updated = ec.addPrerequisite(state.canvas.connectSourceQuestId);
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, updated);
            CanvasLayerMutations.persistCanvasExclusiveChoice(state, group, updated.id());
            CanvasConnectionAnimation.startIfNew(state, ecHit.id(), state.canvas.connectSourceQuestId);
        }
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        state.canvas.canvasSelection.selectOnlyEc(ecHit.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas ec prerequisite connect quest={} ec={}", state.canvas.connectSourceQuestId, ecHit.id());
        refresher.run();
        return true;
    }

    private static boolean cancelPendingConnect(TabletUiState state, Runnable refresher) {
        if (!state.canvas.connectSourceQuestIds.isEmpty() || !state.canvas.connectSourceQuestId.isBlank()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect cancelled sources={}", state.canvas.connectSourceQuestIds);
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        refresher.run();
        return true;
    }

    private static boolean createPendingConnections(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit) {
        if (state.canvas.connectSourceQuestIds.isEmpty() && !state.canvas.connectSourceQuestId.isBlank()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        int connected = 0;
        for (String sourceQuestId : List.copyOf(state.canvas.connectSourceQuestIds)) {
            if (sourceQuestId == null || sourceQuestId.isBlank() || sourceQuestId.equals(hit.questId())) {
                continue;
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect prerequisite={} quest={}", sourceQuestId, hit.questId());
            CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
            TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
            connected++;
        }
        List<String> attemptedSources = List.copyOf(state.canvas.connectSourceQuestIds);
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        if (connected > 0) {
            state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
            state.chapterPanel.lastJumpQuest = hit.questId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect completed sources={} target={}", attemptedSources, hit.questId());
        } else {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect ended without new edge sources={} target={}", attemptedSources, hit.questId());
        }
        refresher.run();
        return true;
    }

    private static boolean finalizeConnectMode(CanvasViewport canvasViewport, TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        boolean addPrerequisite = !(canvasViewport.shiftDown() || canvasViewport.ctrlDown() || button == 1);
        if (state.canvas.connectSourceQuestIds.isEmpty()) {
            state.canvas.connectSourceQuestIds.add(state.canvas.connectSourceQuestId);
        }
        for (String sourceQuestId : List.copyOf(state.canvas.connectSourceQuestIds)) {
            if (!sourceQuestId.equals(hit.questId())) {
                if (addPrerequisite) {
                    CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
                }
                TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, addPrerequisite);
            }
        }
        state.canvas.canvasSelection.selectOnlyQuest(hit.questId());
        state.canvas.connectSourceQuestId = "";
        state.canvas.connectSourceQuestIds.clear();
        refresher.run();
        return true;
    }
}
