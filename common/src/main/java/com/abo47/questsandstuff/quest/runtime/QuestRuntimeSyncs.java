package com.abo47.questsandstuff.quest.runtime;

import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

final class QuestRuntimeSyncs {
    private QuestRuntimeSyncs() {
    }

    static void syncChangedToAll(ServerPlayer actor, QuestProgressSavedData progressData, QuestSyncService syncService, Set<String> changedQuestIds) {
        if (!canSync(actor, progressData, syncService, changedQuestIds)) {
            return;
        }
        progressData.setDirty();
        actor.server.getPlayerList().getPlayers().forEach(player -> syncService.syncDelta(player, changedQuestIds));
    }

    static void syncChangedToOnlineMembers(ServerLevel level, Collection<UUID> members, QuestProgressSavedData progressData, QuestSyncService syncService, Set<String> changedQuestIds) {
        if (level == null || members == null || members.isEmpty() || progressData == null || syncService == null || changedQuestIds == null || changedQuestIds.isEmpty()) {
            return;
        }
        progressData.setDirty();
        for (UUID member : members) {
            ServerPlayer online = level.getServer().getPlayerList().getPlayer(member);
            if (online != null) {
                syncService.syncDelta(online, changedQuestIds);
            }
        }
    }

    private static boolean canSync(ServerPlayer actor, QuestProgressSavedData progressData, QuestSyncService syncService, Set<String> changedQuestIds) {
        return actor != null
                && progressData != null
                && syncService != null
                && changedQuestIds != null
                && !changedQuestIds.isEmpty();
    }
}
