package com.abo47.questsandstuff.quest.editor.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionColors;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionModes;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionTextures;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withConnectionTextureSpacings;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withHiddenConnections;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withPrerequisites;

public final class PrerequisiteEditService {
    private final EditorSessionService owner;

    public PrerequisiteEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    private static boolean invalidQuestPair(String quest, String prerequisite) {
        return quest.isBlank() || prerequisite.isBlank() || quest.equals(prerequisite);
    }

    public void setQuestPrerequisite(ServerPlayer player, String questId, String prerequisiteId, boolean enabled) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (invalidQuestPair(quest, prerequisite)) return;

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
        if (invalidQuestPair(quest, prerequisite)) return;
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Map<String, String> modes = new HashMap<>(source.connectionModes());
        String previous = modes.get(prerequisite);
        if (gridMode) {
            modes.put(prerequisite, QuestConnectionMode.GRID.serializedName());
        } else {
            modes.remove(prerequisite);
        }
        if ((gridMode && QuestConnectionMode.fromSerializedName(previous) == QuestConnectionMode.GRID) || (!gridMode && previous == null)) {
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
        if (invalidQuestPair(quest, prerequisite)) return;
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
        if (invalidQuestPair(quest, prerequisite)) return;
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

    public void setConnectionTexture(ServerPlayer player, String questId, String prerequisiteId, String texture) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (invalidQuestPair(quest, prerequisite)) return;
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Map<String, String> textures = new HashMap<>(source.connectionTextures());
        String previous = textures.put(prerequisite, (texture == null || texture.isBlank()) ? "" : texture);
        if ((texture == null || texture.isBlank()) && (previous == null || previous.isBlank())) {
            return;
        }
        if (previous != null && previous.equals(texture)) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().upsert(withConnectionTextures(source, textures));
        owner.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] connection_texture quest={} prerequisite={} texture={} sourcePrereqs={}", quest, prerequisite, texture, source.prerequisites());
    }

    public void setConnectionTextures(ServerPlayer player, Map<String, Map<String, String>> questTextures) {
        if (questTextures == null || questTextures.isEmpty()) {
            QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures called with empty map");
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        int changedCount = 0;
        for (Map.Entry<String, Map<String, String>> questEntry : questTextures.entrySet()) {
            String quest = EditorSessionService.normalizeQuestId(questEntry.getKey());
            if (quest.isBlank()) continue;
            QuestDefinition source = owner.definitionStore().quests().get(quest);
            if (source == null) {
                QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures source=null quest={}", quest);
                continue;
            }
            Map<String, String> textures = new HashMap<>(source.connectionTextures());
            boolean questChanged = false;
            int prereqCount = 0;
            for (Map.Entry<String, String> prereqEntry : questEntry.getValue().entrySet()) {
                String prerequisite = EditorSessionService.normalizeQuestId(prereqEntry.getKey());
                if (prerequisite.isBlank() || quest.equals(prerequisite)) continue;
                if (!source.prerequisites().contains(prerequisite)) {
                    QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures prereq not in source quest={} prereq={} sourcePrereqs={}", quest, prerequisite, source.prerequisites());
                    continue;
                }
                String texture = prereqEntry.getValue();
                String previous = textures.put(prerequisite, (texture == null || texture.isBlank()) ? "" : texture);
                if ((texture == null || texture.isBlank()) && (previous == null || previous.isBlank())) continue;
                if (previous != null && previous.equals(texture)) continue;
                questChanged = true;
                prereqCount++;
            }
            if (!questChanged) continue;
            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }
            owner.definitionStore().upsert(withConnectionTextures(source, textures));
            changedCount++;
            QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures upsert quest={} prereqs={} textures={}", quest, prereqCount, source.connectionTextures());
        }
        if (changed) {
            owner.postMutation(player);
            QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures postMutation changedCount={}", changedCount);
        } else {
            QuestsAndStuffMod.debugLog("[QnS:Editor] setConnectionTextures nothing changed");
        }
    }

    public void setConnectionTextureSpacing(ServerPlayer player, String questId, String prerequisiteId, int spacing) {
        String quest = EditorSessionService.normalizeQuestId(questId);
        String prerequisite = EditorSessionService.normalizeQuestId(prerequisiteId);
        if (invalidQuestPair(quest, prerequisite)) return;
        QuestDefinition source = owner.definitionStore().quests().get(quest);
        if (source == null || !source.prerequisites().contains(prerequisite)) {
            return;
        }
        Map<String, Integer> spacings = new HashMap<>(source.connectionTextureSpacings());
        Integer previous = spacings.put(prerequisite, Math.max(0, spacing));
        if (previous != null && previous == spacing) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().upsert(withConnectionTextureSpacings(source, spacings));
        owner.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] connection_texture_spacing quest={} prerequisite={} spacing={}", quest, prerequisite, spacing);
    }

    public void connectToNext(ServerPlayer player) {
        EditorSessionService.EditorSession session = owner.session(player);
        List<String> questIds = owner.questIdsInChapter(session.currentChapter);
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
        List<String> questIds = owner.questIdsInChapter(session.currentChapter);
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
