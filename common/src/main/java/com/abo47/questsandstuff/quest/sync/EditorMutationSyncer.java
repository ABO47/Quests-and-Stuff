package com.abo47.questsandstuff.quest.sync;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;

final class EditorMutationSyncer {
    private final SyncPayloadBuilder payloads;
    private final QuestProgressSavedData progressData;
    private final VisibilitySelector visibilitySelector;

    EditorMutationSyncer(
            SyncPayloadBuilder payloads,
            QuestProgressSavedData progressData,
            VisibilitySelector visibilitySelector
    ) {
        this.payloads = payloads;
        this.progressData = progressData;
        this.visibilitySelector = visibilitySelector;
    }

    void broadcast(long sequence, List<ServerPlayer> players, String action, QuestDefinition definition) {
        if (definition == null) {
            return;
        }
        for (ServerPlayer player : players) {
            if (!visibilitySelector.canSeeEditorGraph(player)) {
                continue;
            }
            CompoundTag questTag = SyncKeys.EditorAction.ADD.equals(action)
                    ? payloads.editorQuestPayload(definition, progressData.state(player.getUUID()))
                    : payloads.editorQuestPayload(definition);
            ModNetwork.sendToPlayer(new S2CEditorMutationPacket(sequence, action, definition.id(), questTag), player);
        }
    }

    void broadcast(long sequence, List<ServerPlayer> players, String action, String questId, CompoundTag questTag) {
        for (ServerPlayer player : players) {
            if (!visibilitySelector.canSeeEditorGraph(player)) {
                continue;
            }
            ModNetwork.sendToPlayer(new S2CEditorMutationPacket(sequence, action, questId, questTag == null ? new CompoundTag() : questTag), player);
        }
    }
}
