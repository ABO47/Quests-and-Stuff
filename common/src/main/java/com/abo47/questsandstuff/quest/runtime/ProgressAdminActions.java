package com.abo47.questsandstuff.quest.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.sync.SyncService;

final class ProgressAdminActions {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final SyncService syncService;
    private final RuntimeEngine engine;

    ProgressAdminActions(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, SyncService syncService, RuntimeEngine engine) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.syncService = syncService;
        this.engine = engine;
    }

    void completeQuest(ServerPlayer player, String questId) {
        QuestDefinition definition = definitionStore.quests().get(questId);
        if (definition == null) {
            return;
        }

        PlayerQuestState state = progressData.state(player.getUUID());
        engine.ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());

        QuestProgressState questState = state.quest(questId);
        for (Map.Entry<String, QuestTaskDefinition> task : definition.tasks().entrySet()) {
            questState.setTaskProgress(task.getKey(), task.getValue(), CompletionRules.completeProgress(task.getValue()));
        }
        questState.setCompleted(true, player.server.getTickCount());
        engine.applyExclusiveChoiceDisable(player, player.getUUID(), state, questId);
        syncService.sendQuestEvent(player, "quest_completed", questId, "");
        RuntimeSyncs.syncChangedToAll(player, progressData, syncService, Set.of(questId));
    }

    void resetQuest(ServerPlayer player, String questId) {
        PlayerQuestState state = progressData.state(player.getUUID());
        Set<String> siblings = engine.exclusiveChoiceSiblings(questId);
        state.quests().remove(questId);
        if (!siblings.isEmpty()) {
            engine.clearExclusiveChoiceDisabledForPlayer(player.getUUID(), siblings);
        }
        engine.ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());
        progressData.setDirty();
        syncService.syncFull(player);
    }

    void resetAll(ServerPlayer player) {
        PlayerQuestState state = progressData.state(player.getUUID());
        state.quests().clear();
        engine.ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());
        progressData.setDirty();
        syncService.syncFull(player);
    }

    void togglePin(ServerPlayer player, String questId) {
        PlayerQuestState state = progressData.state(player.getUUID());
        if (!state.pinnedQuests().add(questId)) {
            state.pinnedQuests().remove(questId);
        }
        progressData.setDirty();
        syncService.syncPinned(player);
    }
}
