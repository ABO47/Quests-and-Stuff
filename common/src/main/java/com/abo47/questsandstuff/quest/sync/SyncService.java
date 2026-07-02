package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.quest.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class SyncService {
    private final QuestProgressSavedData progressData;
    private final PerformanceTracker performanceTracker;
    private final SyncChunker chunker;
    private final VisibilitySelector visibilitySelector;
    private final DescriptionSyncer descriptionSyncer;
    private final DisplayCacheSyncer displayCacheSyncer;
    private final EditorMutationSyncer editorMutationSyncer;
    private final EventSyncer eventSyncer;
    private final AtomicLong sequenceCounter = new AtomicLong(1L);

    public SyncService(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, PerformanceTracker performanceTracker) {
        this.progressData = progressData;
        this.performanceTracker = performanceTracker;
        SyncPayloadBuilder payloads = new SyncPayloadBuilder(definitionStore);
        this.chunker = new SyncChunker(payloads);
        this.visibilitySelector = new VisibilitySelector(definitionStore);
        this.descriptionSyncer = new DescriptionSyncer(definitionStore);
        this.displayCacheSyncer = new DisplayCacheSyncer();
        this.editorMutationSyncer = new EditorMutationSyncer(payloads, progressData, visibilitySelector);
        this.eventSyncer = new EventSyncer();
    }

    public void setVisibilityFilter(BiPredicate<PlayerQuestState, QuestDefinition> visibilityFilter) {
        visibilitySelector.setVisibilityFilter(visibilityFilter);
    }

    public void setEditorVisibilityPredicate(Predicate<ServerPlayer> editorVisibilityPredicate) {
        visibilitySelector.setEditorVisibilityPredicate(editorVisibilityPredicate);
    }

    public void syncFull(ServerPlayer player) {
        PlayerQuestState playerState = progressData.state(player.getUUID());
        boolean editorGraphVisible = visibilitySelector.canSeeEditorGraph(player);
        Set<String> visibleQuestIds = visibilitySelector.visibleQuestIds(playerState, editorGraphVisible);
        Set<String> syncedQuestIds = visibilitySelector.syncedQuestIds(playerState, editorGraphVisible);
        List<SyncChunker.SyncChunk> chunks = chunker.fullChunks(playerState, syncedQuestIds, editorGraphVisible);
        long sequence = sequenceCounter.getAndIncrement();
        long bytes = 0L;

        for (SyncChunker.SyncChunk chunk : chunks) {
            CompoundTag payload = chunk.payload();
            bytes += payload.toString().length();
            ModNetwork.sendToPlayer(new S2CFullSyncPacket(sequence, chunk.chunkIndex(), chunk.chunkCount(), payload), player);
        }

        descriptionSyncer.sync(player, sequenceCounter::getAndIncrement, visibleQuestIds);
        displayCacheSyncer.sync(player, sequenceCounter.getAndIncrement());
        syncPinned(player);
        performanceTracker.recordFullSync(1, chunks.size(), bytes);
    }

    public void syncDelta(ServerPlayer player, Set<String> changedQuests) {
        syncDelta(player, changedQuests, false);
    }

    public void syncDeltaWithMetadata(List<ServerPlayer> players, Set<String> changedQuests, Set<String> changedGroups) {
        if (players == null || players.isEmpty()) {
            return;
        }
        for (ServerPlayer player : players) {
            syncDelta(player, changedQuests, changedGroups, true);
        }
    }

    private void syncDelta(ServerPlayer player, Set<String> changedQuests, boolean forceMetadata) {
        syncDelta(player, changedQuests, Set.of(), forceMetadata);
    }

    private void syncDelta(ServerPlayer player, Set<String> changedQuests, Set<String> changedGroups, boolean forceMetadata) {
        Set<String> safeChangedQuests = changedQuests == null ? Set.of() : changedQuests;
        Set<String> safeChangedGroups = changedGroups == null ? Set.of() : changedGroups;
        boolean includeMetadata = forceMetadata && !safeChangedGroups.isEmpty();
        if (safeChangedQuests.isEmpty() && !includeMetadata) {
            return;
        }

        PlayerQuestState playerState = progressData.state(player.getUUID());
        boolean editorGraphVisible = visibilitySelector.canSeeEditorGraph(player);
        VisibilitySelector.DeltaVisibility delta = visibilitySelector.deltaVisibility(playerState, editorGraphVisible, safeChangedQuests);
        List<SyncChunker.SyncChunk> chunks = chunker.deltaChunks(
                playerState,
                delta.changedQuestIds(),
                delta.removedQuestIds(),
                safeChangedGroups,
                includeMetadata
        );
        long sequence = sequenceCounter.getAndIncrement();
        long bytes = 0L;

        for (SyncChunker.SyncChunk chunk : chunks) {
            CompoundTag payload = chunk.payload();
            bytes += payload.toString().length();
            ModNetwork.sendToPlayer(new S2CDeltaSyncPacket(sequence, chunk.chunkIndex(), chunk.chunkCount(), payload), player);
        }

        descriptionSyncer.sync(player, sequenceCounter::getAndIncrement, delta.descriptionQuestIds());
        performanceTracker.recordDeltaSync(1, chunks.size(), bytes);
    }

    public void syncPinned(ServerPlayer player) {
        PlayerQuestState state = progressData.state(player.getUUID());
        List<String> pinned = new ArrayList<>(state.pinnedQuests());
        pinned.sort(String::compareTo);
        ModNetwork.sendToPlayer(new S2CPinnedSyncPacket(sequenceCounter.getAndIncrement(), pinned), player);
    }

    public void syncFull(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            syncFull(player);
        }
    }

    public void sendQuestEvent(ServerPlayer player, String eventType, String questId, String rewardId) {
        eventSyncer.send(player, sequenceCounter.getAndIncrement(), eventType, questId, rewardId);
    }

    public void broadcastEditorMutation(List<ServerPlayer> players, String action, QuestDefinition definition) {
        editorMutationSyncer.broadcast(sequenceCounter.getAndIncrement(), players, action, definition);
    }

    public void broadcastEditorMutation(List<ServerPlayer> players, String action, String questId, CompoundTag questTag) {
        editorMutationSyncer.broadcast(sequenceCounter.getAndIncrement(), players, action, questId, questTag);
    }

}
