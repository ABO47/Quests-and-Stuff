package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.runtime.progress.QuestProgressState;
import com.abo47.questsandstuff.quest.runtime.reward.QuestRewardApplier;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class QuestProgressAdminActions {
    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final QuestSyncService syncService;
    private final QuestRuntimeEngine engine;

    QuestProgressAdminActions(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, QuestSyncService syncService, QuestRuntimeEngine engine) {
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
            questState.setTaskProgress(task.getKey(), task.getValue(), QuestCompletionRules.completeProgress(task.getValue()));
        }
        questState.setCompleted(true, player.server.getTickCount());
        syncService.sendQuestEvent(player, "quest_completed", questId, "");
        if (definition.settings().autoClaimRewards()) {
            QuestRewardApplier.autoClaimNonSelectableRewards(player, definition, questState, player.server.getTickCount(), syncService);
        }
        progressData.setDirty();
        player.server.getPlayerList().getPlayers().forEach(target -> syncService.syncDelta(target, Set.of(questId)));
    }

    void resetQuest(ServerPlayer player, String questId) {
        PlayerQuestState state = progressData.state(player.getUUID());
        state.quests().remove(questId);
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
