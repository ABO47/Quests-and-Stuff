package com.abo47.questsandstuff.quest.editor.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionColors;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionModes;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withHiddenConnections;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withPrerequisites;

public final class PrerequisiteEditService {
    private final EditorSessionService owner;

    public PrerequisiteEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void setQuestPrerequisite(ServerPlayer player, String questId, String prerequisiteId, boolean enabled) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (quest.isBlank() || prerequisite.isBlank() || quest.equals(prerequisite)) {
            return;
        }

        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !owner.definitionStore().quests().containsKey(prerequisite)) {
            return;
        }

        Set<String> prerequisites = new HashSet<>(source.prerequisites());
        boolean changed = enabled ? prerequisites.add(prerequisite) : prerequisites.remove(prerequisite);
        if (!changed) {
            return;
        }

        owner.captureUndo(owner.session(player));
        QuestsAndStuffMod.debugLog("[QnS:Editor] prerequisite quest={} prerequisite={} enabled={}", quest, prerequisite, enabled);
        owner.definitionStore().upsert(withPrerequisites(source, prerequisites));
        owner.postMutation(player);
    }

    public void setConnectionMode(ServerPlayer player, String questId, String prerequisiteId, boolean gridMode) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (quest.isBlank() || prerequisite.isBlank() || quest.equals(prerequisite)) {
            return;
        }
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Map<String, String> modes = new HashMap<>(source.connectionModes());
        String previous = modes.get(prerequisite);
        if (gridMode) {
            modes.put(prerequisite, "grid");
        } else {
            modes.remove(prerequisite);
        }
        if ((gridMode && "grid".equals(previous)) || (!gridMode && previous == null)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().upsert(withConnectionModes(source, modes));
        owner.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] connection_mode quest={} prerequisite={} mode={}", quest, prerequisite, gridMode ? "grid" : "direct");
    }

    public void setConnectionHidden(ServerPlayer player, String questId, String prerequisiteId, boolean hidden) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (quest.isBlank() || prerequisite.isBlank() || quest.equals(prerequisite)) {
            return;
        }
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Set<String> hiddenConnections = new HashSet<>(source.hiddenConnections());
        boolean changed = hidden ? hiddenConnections.add(prerequisite) : hiddenConnections.remove(prerequisite);
        if (!changed) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().upsert(withHiddenConnections(source, hiddenConnections));
        owner.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] connection_hidden quest={} prerequisite={} hidden={}", quest, prerequisite, hidden);
    }

    public void setConnectionColor(ServerPlayer player, String questId, String prerequisiteId, int color) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (quest.isBlank() || prerequisite.isBlank() || quest.equals(prerequisite)) {
            return;
        }
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Map<String, Integer> colors = new HashMap<>(source.connectionColors());
        Integer previous = colors.put(prerequisite, color);
        if (previous != null && previous == color) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().upsert(withConnectionColors(source, colors));
        owner.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] connection_color quest={} prerequisite={} color={}", quest, prerequisite, color);
    }

    public void connectToNext(ServerPlayer player) {
        EditorSessionService.EditorSession session = owner.session(player);
        List<String> questIds = owner.questIdsInGroup(session.currentGroup);
        if (questIds.size() < 2) {
            return;
        }

        int idx = questIds.indexOf(session.currentQuest);
        if (idx < 0) {
            idx = 0;
        }
        String current = questIds.get(idx);
        String prerequisite = questIds.get((idx + 1) % questIds.size());
        if (current.equals(prerequisite)) {
            return;
        }

        QuestDefinition source = owner.definitionStore().quests().get(current);
        if (source == null) {
            return;
        }

        owner.captureUndo(session);
        Set<String> prerequisites = new HashSet<>(source.prerequisites());
        prerequisites.add(prerequisite);
        owner.definitionStore().upsert(withPrerequisites(source, prerequisites));
        owner.postMutation(player);
    }

    public void disconnectFromNext(ServerPlayer player) {
        EditorSessionService.EditorSession session = owner.session(player);
        List<String> questIds = owner.questIdsInGroup(session.currentGroup);
        if (questIds.size() < 2) {
            return;
        }

        int idx = questIds.indexOf(session.currentQuest);
        if (idx < 0) {
            idx = 0;
        }
        String current = questIds.get(idx);
        String prerequisite = questIds.get((idx + 1) % questIds.size());

        QuestDefinition source = owner.definitionStore().quests().get(current);
        if (source == null) {
            return;
        }

        owner.captureUndo(session);
        Set<String> prerequisites = new HashSet<>(source.prerequisites());
        prerequisites.remove(prerequisite);
        owner.definitionStore().upsert(withPrerequisites(source, prerequisites));
        owner.postMutation(player);
    }

}
