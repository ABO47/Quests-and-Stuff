package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasConnectionAnimation;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.world.entity.player.Player;

import java.util.List;

final class CanvasConnectionClickActions {
    private CanvasConnectionClickActions() {
    }

    static boolean handleQuickConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        boolean quickConnectActive = state.quickConnectHeld || TabletClientHooks.quickConnectDown();
        if (!quickConnectActive && !state.quickConnectSourceQuestId.isBlank()) {
            state.quickConnectSourceQuestId = "";
        }
        if (button != 0 || !quickConnectActive) {
            return false;
        }
        if (hit == null) {
            return true;
        }
        if (state.quickConnectSourceQuestId.isBlank() || state.quickConnectSourceQuestId.equals(hit.questId())) {
            state.quickConnectSourceQuestId = hit.questId();
            state.canvasSelection.questIds().clear();
            state.canvasSelection.questIds().add(hit.questId());
            state.lastJumpQuest = hit.questId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect source quest={}", hit.questId());
            refresher.run();
            return true;
        }
        String sourceQuestId = state.quickConnectSourceQuestId;
        CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
        TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
        state.quickConnectSourceQuestId = hit.questId();
        state.canvasSelection.questIds().clear();
        state.canvasSelection.questIds().add(hit.questId());
        state.lastJumpQuest = hit.questId();
        state.connectSourceQuestId = "";
        state.connectSourceQuestIds.clear();
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas quick-connect prerequisite={} quest={}", sourceQuestId, hit.questId());
        refresher.run();
        return true;
    }

    static boolean handlePendingConnect(TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        if ((state.connectSourceQuestId.isBlank() && state.connectSourceQuestIds.isEmpty()) || button != 0) {
            return false;
        }
        if (state.connectSourceQuestIds.isEmpty() && !state.connectSourceQuestId.isBlank()) {
            state.connectSourceQuestIds.add(state.connectSourceQuestId);
        }
        if (hit == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect cancelled sources={}", state.connectSourceQuestIds);
            state.connectSourceQuestId = "";
            state.connectSourceQuestIds.clear();
            refresher.run();
            return true;
        }
        int connected = 0;
        for (String sourceQuestId : List.copyOf(state.connectSourceQuestIds)) {
            if (sourceQuestId == null || sourceQuestId.isBlank() || sourceQuestId.equals(hit.questId())) {
                continue;
            }
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect prerequisite={} quest={}", sourceQuestId, hit.questId());
            CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
            TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, true);
            connected++;
        }
        List<String> attemptedSources = List.copyOf(state.connectSourceQuestIds);
        state.connectSourceQuestId = "";
        state.connectSourceQuestIds.clear();
        if (connected > 0) {
            state.canvasSelection.questIds().clear();
            state.canvasSelection.questIds().add(hit.questId());
            state.lastJumpQuest = hit.questId();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect completed sources={} target={}", attemptedSources, hit.questId());
            refresher.run();
            return true;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas connect ended without new edge sources={} target={}", attemptedSources, hit.questId());
        refresher.run();
        return true;
    }

    static boolean handleConnectMode(CanvasViewport canvasViewport, TabletUiState state, Player player, Runnable refresher, QuestCardLayout hit, int button) {
        if (hit == null) {
            return true;
        }
        if (state.connectSourceQuestId.isBlank() || state.connectSourceQuestId.equals(hit.questId())) {
            state.connectSourceQuestId = hit.questId();
            state.connectSourceQuestIds.clear();
            state.connectSourceQuestIds.add(hit.questId());
            state.canvasSelection.questIds().clear();
            state.canvasSelection.questIds().add(hit.questId());
            refresher.run();
            return true;
        }
        boolean addPrerequisite = !(canvasViewport.shiftDown() || canvasViewport.ctrlDown() || button == 1);
        if (state.connectSourceQuestIds.isEmpty()) {
            state.connectSourceQuestIds.add(state.connectSourceQuestId);
        }
        for (String sourceQuestId : List.copyOf(state.connectSourceQuestIds)) {
            if (!sourceQuestId.equals(hit.questId())) {
                if (addPrerequisite) {
                    CanvasConnectionAnimation.startIfNew(state, hit.questId(), sourceQuestId);
                }
                TabletUiFactory.runPrerequisiteAction(player, hit.questId(), sourceQuestId, addPrerequisite);
            }
        }
        state.canvasSelection.questIds().clear();
        state.canvasSelection.questIds().add(hit.questId());
        state.connectSourceQuestId = "";
        state.connectSourceQuestIds.clear();
        refresher.run();
        return true;
    }
}
