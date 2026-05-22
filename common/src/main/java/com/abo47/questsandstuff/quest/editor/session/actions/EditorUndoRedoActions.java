package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorSession;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.QuestRuntimeEngine;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class EditorUndoRedoActions {
    private static final int MAX_HISTORY = 24;

    private final EditorSessionService service;
    private final QuestDefinitionStore definitionStore;
    private final QuestRuntimeEngine runtimeEngine;
    private final QuestSyncService syncService;

    public EditorUndoRedoActions(
            EditorSessionService service,
            QuestDefinitionStore definitionStore,
            QuestRuntimeEngine runtimeEngine,
            QuestSyncService syncService
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
        session.redo.push(definitionStore.snapshot());
        definitionStore.replaceAll(session.undo.pop());
        postMutation(player);
    }

    public void redo(ServerPlayer player) {
        EditorSession session = service.session(player);
        if (session.redo.isEmpty()) {
            return;
        }
        session.undo.push(definitionStore.snapshot());
        definitionStore.replaceAll(session.redo.pop());
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
        session.undo.push(definitionStore.snapshot());
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
}
