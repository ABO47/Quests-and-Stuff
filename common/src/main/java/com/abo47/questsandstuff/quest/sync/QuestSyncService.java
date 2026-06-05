package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.sync.S2CDescriptionSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDeltaSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CDisplayCacheSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CEditorMutationPacket;
import com.abo47.questsandstuff.network.sync.S2CFullSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CPinnedSyncPacket;
import com.abo47.questsandstuff.network.sync.S2CQuestEventPacket;
import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class QuestSyncService {
    private static final int MAX_QUESTS_PER_CHUNK = 128;
    private static final int MAX_DESCRIPTIONS_PER_CHUNK = 64;

    private final QuestDefinitionStore definitionStore;
    private final QuestProgressSavedData progressData;
    private final QuestPerformanceTracker performanceTracker;
    private final QuestSyncPayloadBuilder payloads;
    private final AtomicLong sequenceCounter = new AtomicLong(1L);
    private BiPredicate<PlayerQuestState, QuestDefinition> visibilityFilter = (state, definition) -> true;
    private Predicate<ServerPlayer> editorVisibilityPredicate = QuestSyncService::hasEditorVisibility;

    public QuestSyncService(QuestDefinitionStore definitionStore, QuestProgressSavedData progressData, QuestPerformanceTracker performanceTracker) {
        this.definitionStore = definitionStore;
        this.progressData = progressData;
        this.performanceTracker = performanceTracker;
        this.payloads = new QuestSyncPayloadBuilder(definitionStore);
    }

    public void setVisibilityFilter(BiPredicate<PlayerQuestState, QuestDefinition> visibilityFilter) {
        this.visibilityFilter = visibilityFilter == null ? (state, definition) -> true : visibilityFilter;
    }

    public void setEditorVisibilityPredicate(Predicate<ServerPlayer> editorVisibilityPredicate) {
        this.editorVisibilityPredicate = editorVisibilityPredicate == null ? QuestSyncService::hasEditorVisibility : editorVisibilityPredicate;
    }

    public void syncFull(ServerPlayer player) {
        PlayerQuestState playerState = progressData.state(player.getUUID());
        Set<String> visibleQuestIds = visibleQuestIds(player, playerState);
        Set<String> syncedQuestIds = syncedQuestIds(player, playerState);
        List<Set<String>> chunks = partition(syncedQuestIds);
        boolean editorGraphVisible = canSeeEditorGraph(player);
        long sequence = sequenceCounter.getAndIncrement();
        long bytes = 0L;

        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag payload = new CompoundTag();
            payload.putInt("schema", QuestDefinition.CURRENT_SCHEMA);
            payload.put("groups", payloads.groupsTag(syncedQuestIds, editorGraphVisible));
            payload.put("group_props", payloads.groupPropsTag(syncedQuestIds, editorGraphVisible));
            payload.put("quests", payloads.questPayload(playerState, chunks.get(i)));
            bytes += payload.toString().length();
            QuestNetwork.sendToPlayer(new S2CFullSyncPacket(sequence, i, chunks.size(), payload), player);
        }

        syncDescriptions(player, visibleQuestIds);
        syncDisplayCaches(player);
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
        boolean editorGraphVisible = canSeeEditorGraph(player);
        Set<String> existingChanged = new HashSet<>();
        Set<String> descriptionChanged = new HashSet<>();
        Set<String> removed = new HashSet<>();
        for (String questId : safeChangedQuests) {
            QuestDefinition definition = definitionStore.quest(questId);
            if (definition == null) {
                removed.add(questId);
                continue;
            }
            boolean visible = editorGraphVisible || visibilityFilter.test(playerState, definition);
            if (visible) {
                descriptionChanged.add(questId);
            }
            if (visible || shouldSyncLockedPreview(playerState, definition)) {
                existingChanged.add(questId);
            } else {
                removed.add(questId);
            }
        }

        List<Set<String>> changedChunks = partition(existingChanged);
        int chunkCount = Math.max(1, changedChunks.size());
        long sequence = sequenceCounter.getAndIncrement();
        long bytes = 0L;

        for (int i = 0; i < chunkCount; i++) {
            CompoundTag payload = new CompoundTag();

            Set<String> changedIds = i < changedChunks.size() ? changedChunks.get(i) : Set.of();
            if (i == 0 && includeMetadata) {
                payload.put("groups", payloads.groupsTag());
                payload.put("group_props", payloads.groupPropsTagForGroups(safeChangedGroups));
            }
            payload.put("changed", payloads.questPayload(playerState, changedIds));

            CompoundTag removedTag = new CompoundTag();
            if (i == 0) {
                for (String removedId : removed) {
                    removedTag.putBoolean(removedId, true);
                }
            }
            payload.put("removed", removedTag);

            bytes += payload.toString().length();
            QuestNetwork.sendToPlayer(new S2CDeltaSyncPacket(sequence, i, chunkCount, payload), player);
        }

        syncDescriptions(player, descriptionChanged);
        performanceTracker.recordDeltaSync(1, chunkCount, bytes);
    }

    public void syncPinned(ServerPlayer player) {
        PlayerQuestState state = progressData.state(player.getUUID());
        List<String> pinned = new ArrayList<>(state.pinnedQuests());
        pinned.sort(String::compareTo);
        QuestNetwork.sendToPlayer(new S2CPinnedSyncPacket(sequenceCounter.getAndIncrement(), pinned), player);
    }

    public void syncFull(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            syncFull(player);
        }
    }

    public void sendQuestEvent(ServerPlayer player, String eventType, String questId, String rewardId) {
        QuestNetwork.sendToPlayer(new S2CQuestEventPacket(
                sequenceCounter.getAndIncrement(),
                eventType,
                questId,
                rewardId
        ), player);
    }

    public void broadcastEditorMutation(List<ServerPlayer> players, String action, QuestDefinition definition) {
        if (definition == null) {
            return;
        }
        long sequence = sequenceCounter.getAndIncrement();
        for (ServerPlayer player : players) {
            if (!canSeeEditorGraph(player)) {
                continue;
            }
            CompoundTag questTag = "add".equals(action)
                    ? payloads.editorQuestPayload(definition, progressData.state(player.getUUID()))
                    : payloads.editorQuestPayload(definition);
            QuestNetwork.sendToPlayer(new S2CEditorMutationPacket(sequence, action, definition.id(), questTag), player);
        }
    }

    public void broadcastEditorMutation(List<ServerPlayer> players, String action, String questId, CompoundTag questTag) {
        long sequence = sequenceCounter.getAndIncrement();
        for (ServerPlayer player : players) {
            if (!canSeeEditorGraph(player)) {
                continue;
            }
            QuestNetwork.sendToPlayer(new S2CEditorMutationPacket(sequence, action, questId, questTag == null ? new CompoundTag() : questTag), player);
        }
    }

    private Set<String> visibleQuestIds(ServerPlayer player, PlayerQuestState playerState) {
        if (canSeeEditorGraph(player)) {
            return new HashSet<>(definitionStore.questIds());
        }
        Set<String> visible = new HashSet<>();
        for (QuestDefinition definition : definitionStore.questDefinitions()) {
            if (visibilityFilter.test(playerState, definition)) {
                visible.add(definition.id());
            }
        }
        return visible;
    }

    private Set<String> syncedQuestIds(ServerPlayer player, PlayerQuestState playerState) {
        if (canSeeEditorGraph(player)) {
            return new HashSet<>(definitionStore.questIds());
        }
        Set<String> synced = new HashSet<>();
        for (QuestDefinition definition : definitionStore.questDefinitions()) {
            if (visibilityFilter.test(playerState, definition) || shouldSyncLockedPreview(playerState, definition)) {
                synced.add(definition.id());
            }
        }
        return synced;
    }

    private boolean shouldSyncLockedPreview(PlayerQuestState playerState, QuestDefinition definition) {
        return definition.settings().hiddenMode() == QuestVisibilityMode.LOCKED
                && !playerState.quest(definition.id()).unlocked()
                && !playerState.quest(definition.id()).completed();
    }

    private boolean canSeeEditorGraph(ServerPlayer player) {
        return editorVisibilityPredicate.test(player);
    }

    private static boolean hasEditorVisibility(ServerPlayer player) {
        return QuestEditorPermissions.canEdit(player);
    }

    private void syncDescriptions(ServerPlayer player, Set<String> questIds) {
        if (questIds.isEmpty()) {
            return;
        }

        List<String> ids = new ArrayList<>(questIds);
        ids.sort(String::compareTo);
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += MAX_DESCRIPTIONS_PER_CHUNK) {
            int end = Math.min(ids.size(), i + MAX_DESCRIPTIONS_PER_CHUNK);
            chunks.add(ids.subList(i, end));
        }

        long sequence = sequenceCounter.getAndIncrement();
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag payload = new CompoundTag();
            CompoundTag descriptions = new CompoundTag();
            for (String questId : chunks.get(i)) {
                QuestDefinition definition = definitionStore.quest(questId);
                if (definition == null) {
                    continue;
                }
                ListTag lines = new ListTag();
                for (String line : definition.display().description()) {
                    lines.add(StringTag.valueOf(line));
                }
                descriptions.put(questId, lines);
            }
            payload.put("descriptions", descriptions);
            QuestNetwork.sendToPlayer(new S2CDescriptionSyncPacket(sequence, i, chunks.size(), payload), player);
        }
    }

    private void syncDisplayCaches(ServerPlayer player) {
        CompoundTag payload = new CompoundTag();
        CompoundTag advancements = new CompoundTag();
        for (var advancement : player.server.getAdvancements().getAllAdvancements()) {
            String id = advancement.getId().toString();
            String title = advancement.getDisplay() == null
                    ? id
                    : advancement.getDisplay().getTitle().getString();
            advancements.putString(id, title);
        }
        payload.put("advancements", advancements);

        CompoundTag lootTables = new CompoundTag();
        for (var key : player.server.getLootData().getKeys(LootDataType.TABLE)) {
            String id = key.toString();
            lootTables.putString(id, key.getPath());
        }
        payload.put("loot_tables", lootTables);

        CompoundTag biomes = new CompoundTag();
        var biomeRegistry = player.server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        for (var biomeKey : biomeRegistry.registryKeySet()) {
            String id = biomeKey.location().toString();
            biomes.putString(id, biomeKey.location().getPath());
        }
        payload.put("biomes", biomes);

        QuestNetwork.sendToPlayer(new S2CDisplayCacheSyncPacket(sequenceCounter.getAndIncrement(), payload), player);
    }

    private static List<Set<String>> partition(Set<String> questIds) {
        List<String> ids = new ArrayList<>(questIds);
        ids.sort(String::compareTo);

        if (ids.isEmpty()) {
            return List.of(Set.of());
        }

        List<Set<String>> chunks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += MAX_QUESTS_PER_CHUNK) {
            int end = Math.min(ids.size(), i + MAX_QUESTS_PER_CHUNK);
            chunks.add(new HashSet<>(ids.subList(i, end)));
        }
        return chunks;
    }

}
