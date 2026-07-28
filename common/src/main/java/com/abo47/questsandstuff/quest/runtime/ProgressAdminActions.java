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
    private final RuntimeContext ctx;

    ProgressAdminActions(RuntimeContext ctx) {
        this.ctx = ctx;
    }

    void completeQuest(ServerPlayer player, String questId) {
        QuestDefinition definition = ctx.definitionStore().quests().get(questId);
        if (definition == null) {
            return;
        }

        PlayerQuestState state = ctx.progressData().state(player.getUUID());
        ctx.engine().ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());

        QuestProgressState questState = state.quest(questId);
        for (Map.Entry<String, QuestTaskDefinition> task : definition.tasks().entrySet()) {
            questState.setTaskProgress(task.getKey(), task.getValue(), CompletionRules.completeProgress(task.getValue()));
        }
        questState.setCompleted(true, player.server.getTickCount());
        ctx.engine().applyExclusiveChoiceDisable(player, player.getUUID(), state, questId);
        ctx.syncService().sendQuestEvent(player, "quest_completed", questId, "");
        RuntimeSyncs.syncChangedToAll(player, ctx.progressData(), ctx.syncService(), Set.of(questId));
    }

    void resetQuest(ServerPlayer player, String questId) {
        PlayerQuestState state = ctx.progressData().state(player.getUUID());
        Set<String> siblings = ctx.engine().exclusiveChoiceSiblings(questId);
        state.quests().remove(questId);
        if (!siblings.isEmpty()) {
            ctx.engine().clearExclusiveChoiceDisabledForPlayer(player.getUUID(), siblings);
        }
        ctx.engine().ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());
        ctx.progressData().setDirty();
        ctx.syncService().syncFull(player);
    }

    void resetAll(ServerPlayer player) {
        PlayerQuestState state = ctx.progressData().state(player.getUUID());
        state.quests().clear();
        ctx.engine().ensureUnlocks(player, player.getUUID(), state, new HashSet<>(), player.server.getTickCount());
        ctx.progressData().setDirty();
        ctx.syncService().syncFull(player);
    }

    void togglePin(ServerPlayer player, String questId) {
        PlayerQuestState state = ctx.progressData().state(player.getUUID());
        if (!state.pinnedQuests().add(questId)) {
            state.pinnedQuests().remove(questId);
        }
        ctx.progressData().setDirty();
        ctx.syncService().syncPinned(player);
    }
}
