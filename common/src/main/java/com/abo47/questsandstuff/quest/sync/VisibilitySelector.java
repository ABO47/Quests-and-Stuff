package com.abo47.questsandstuff.quest.sync;

import com.abo47.questsandstuff.quest.editor.QuestEditorPermissions;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.progress.PlayerQuestState;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

final class VisibilitySelector {
    private final QuestDefinitionStore definitionStore;
    private BiPredicate<PlayerQuestState, QuestDefinition> visibilityFilter = (state, definition) -> true;
    private Predicate<ServerPlayer> editorVisibilityPredicate = QuestEditorPermissions::canEdit;

    VisibilitySelector(QuestDefinitionStore definitionStore) {
        this.definitionStore = definitionStore;
    }

    void setVisibilityFilter(BiPredicate<PlayerQuestState, QuestDefinition> visibilityFilter) {
        this.visibilityFilter = visibilityFilter == null ? (state, definition) -> true : visibilityFilter;
    }

    void setEditorVisibilityPredicate(Predicate<ServerPlayer> editorVisibilityPredicate) {
        this.editorVisibilityPredicate = editorVisibilityPredicate == null ? QuestEditorPermissions::canEdit : editorVisibilityPredicate;
    }

    boolean canSeeEditorGraph(ServerPlayer player) {
        return editorVisibilityPredicate.test(player);
    }

    Set<String> visibleQuestIds(PlayerQuestState playerState, boolean editorGraphVisible) {
        if (editorGraphVisible) {
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

    Set<String> syncedQuestIds(PlayerQuestState playerState, boolean editorGraphVisible) {
        if (editorGraphVisible) {
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

    DeltaVisibility deltaVisibility(PlayerQuestState playerState, boolean editorGraphVisible, Set<String> changedQuestIds) {
        Set<String> existingChanged = new HashSet<>();
        Set<String> descriptionChanged = new HashSet<>();
        Set<String> removed = new HashSet<>();
        for (String questId : changedQuestIds == null ? Set.<String>of() : changedQuestIds) {
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
        return new DeltaVisibility(existingChanged, descriptionChanged, removed);
    }

    boolean shouldSyncLockedPreview(PlayerQuestState playerState, QuestDefinition definition) {
        return definition.settings().hiddenMode() == QuestVisibilityMode.LOCKED
                && !playerState.quest(definition.id()).unlocked()
                && !playerState.quest(definition.id()).completed();
    }

    record DeltaVisibility(Set<String> changedQuestIds, Set<String> descriptionQuestIds, Set<String> removedQuestIds) {
    }
}
