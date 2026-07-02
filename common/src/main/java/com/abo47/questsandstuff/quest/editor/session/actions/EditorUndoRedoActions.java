package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorSession;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.sync.SyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EditorUndoRedoActions {
    private static final int MAX_HISTORY = 24;

    public interface EditorHistoryEntry {
        void apply(QuestDefinitionStore definitionStore);
    }

    private final EditorSessionService service;
    private final QuestDefinitionStore definitionStore;
    private final RuntimeEngine runtimeEngine;
    private final SyncService syncService;

    public EditorUndoRedoActions(
            EditorSessionService service,
            QuestDefinitionStore definitionStore,
            RuntimeEngine runtimeEngine,
            SyncService syncService
    ) {
        this.service = service;
        this.definitionStore = definitionStore;
        this.runtimeEngine = runtimeEngine;
        this.syncService = syncService;
    }

    public void undo(ServerPlayer player) {
        EditorSession session = service.session(player);
        if (session.undo.isEmpty()) {
            return;
        }
        session.redo.push(new FullSnapshotEntry(definitionStore.editorSnapshot()));
        session.undo.pop().apply(definitionStore);
        postMutation(player);
    }

    public void redo(ServerPlayer player) {
        EditorSession session = service.session(player);
        if (session.redo.isEmpty()) {
            return;
        }
        session.undo.push(new FullSnapshotEntry(definitionStore.editorSnapshot()));
        session.redo.pop().apply(definitionStore);
        postMutation(player);
    }

    public void saveAll(ServerPlayer player) {
        definitionStore.saveAll();
        runtimeEngine.rebuildIndex();
        List<ServerPlayer> players = player.server.getPlayerList().getPlayers();
        runtimeEngine.preparePlayersForFullSync(players);
        syncService.syncFull(players);
    }

    public void captureUndo(EditorSession session) {
        session.undo.push(new FullSnapshotEntry(definitionStore.editorSnapshot()));
        while (session.undo.size() > MAX_HISTORY) {
            session.undo.removeLast();
        }
        session.redo.clear();
    }

    public void capturePasteUndo(
            EditorSession session,
            Collection<String> questIds,
            Collection<String> imageIds,
            Collection<String> textIds,
            String group
    ) {
        session.undo.push(new PasteUndoEntry(questIds, imageIds, textIds, group));
        while (session.undo.size() > MAX_HISTORY) {
            session.undo.removeLast();
        }
        session.redo.clear();
    }

    public void postMutation(ServerPlayer player) {
        runtimeEngine.rebuildIndex();
        List<ServerPlayer> players = player.server.getPlayerList().getPlayers();
        runtimeEngine.preparePlayersForFullSync(players);
        syncService.syncFull(players);
    }

    public void postMutationDelta(ServerPlayer player, Set<String> changedQuestIds, Set<String> changedChapters) {
        runtimeEngine.refreshIndex(changedQuestIds);
        List<ServerPlayer> players = player.server.getPlayerList().getPlayers();
        Set<String> syncedQuestIds = runtimeEngine.preparePlayersForDeltaSync(players, changedQuestIds);
        syncService.syncDeltaWithMetadata(players, syncedQuestIds, changedChapters);
    }

    private record FullSnapshotEntry(QuestDefinitionStore.EditorSnapshot snapshot) implements EditorHistoryEntry {
        @Override
        public void apply(QuestDefinitionStore definitionStore) {
            definitionStore.replaceAll(snapshot);
        }
    }

    private record PasteUndoEntry(
            Set<String> questIds,
            Set<String> imageIds,
            Set<String> textIds,
            String group
    ) implements EditorHistoryEntry {
        PasteUndoEntry(Collection<String> questIds, Collection<String> imageIds, Collection<String> textIds, String group) {
            this(copyOf(questIds), copyOf(imageIds), copyOf(textIds), group == null ? "" : group.trim());
        }

        @Override
        public void apply(QuestDefinitionStore definitionStore) {
            for (String questId : questIds) {
                definitionStore.remove(questId);
            }
            if (group.isBlank()) {
                return;
            }
            for (String imageId : imageIds) {
                definitionStore.removeCanvasImage(group, imageId);
            }
            for (String textId : textIds) {
                definitionStore.removeCanvasText(group, textId);
            }
        }

        private static Set<String> copyOf(Collection<String> values) {
            if (values == null || values.isEmpty()) {
                return Set.of();
            }
            Set<String> copy = new HashSet<>();
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    copy.add(value);
                }
            }
            return Set.copyOf(copy);
        }
    }
}
