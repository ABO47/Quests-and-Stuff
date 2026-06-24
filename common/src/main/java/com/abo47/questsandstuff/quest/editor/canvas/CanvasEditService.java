package com.abo47.questsandstuff.quest.editor.canvas;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;


import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withGroups;

public final class CanvasEditService {
    private final EditorSessionService owner;

    public CanvasEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void putCanvasExclusiveChoice(ServerPlayer player, String groupName, CanvasExclusiveChoice ec) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || ec == null || ec.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasExclusiveChoice(group, ec);
        owner.postMutation(player);
    }

    public void putCanvasExclusiveChoices(ServerPlayer player, String groupName, List<CanvasExclusiveChoice> ecs) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || ecs == null || ecs.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec == null || ec.id().isBlank()) {
                continue;
            }
            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }
            owner.definitionStore().putCanvasExclusiveChoice(group, ec);
        }
        if (changed) {
            owner.postMutation(player);
        }
    }

    public void removeCanvasExclusiveChoice(ServerPlayer player, String groupName, String ecId) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || ecId == null || ecId.isBlank()) {
            return;
        }
        CanvasExclusiveChoice removed = owner.definitionStore().canvasExclusiveChoices(group).stream()
                .filter(ec -> ec.id().equals(ecId))
                .findFirst()
                .orElse(null);
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasExclusiveChoice(group, ecId)) {
            if (removed != null && !removed.connectionQuestIds().isEmpty()) {
                owner.runtimeEngine().clearExclusiveChoiceDisabled(new HashSet<>(removed.connectionQuestIds()));
            }
            owner.postMutation(player);
        }
    }

    public void ecConnectionHidden(ServerPlayer player, String groupName, String sourceId, String targetId, boolean hidden) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || sourceId.isBlank() || targetId.isBlank()) {
            return;
        }
        CanvasExclusiveChoice ec = owner.definitionStore().canvasExclusiveChoices(group).stream()
                .filter(e -> e.id().equals(sourceId))
                .findFirst()
                .orElse(null);
        if (ec == null) {
            ec = owner.definitionStore().canvasExclusiveChoices(group).stream()
                    .filter(e -> e.id().equals(targetId))
                    .findFirst()
                    .orElse(null);
            if (ec == null) return;
            owner.captureUndo(owner.session(player));
            owner.definitionStore().putCanvasExclusiveChoice(group, ec.withHiddenConnection(sourceId, hidden));
        } else {
            owner.captureUndo(owner.session(player));
            owner.definitionStore().putCanvasExclusiveChoice(group, ec.withHiddenConnection(targetId, hidden));
        }
        owner.postMutation(player);
    }

    public void putCanvasImage(ServerPlayer player, String groupName, CanvasImageLayer image) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasImage(group, image);
        owner.postMutation(player);
    }

    public void removeCanvasImage(ServerPlayer player, String groupName, String imageId) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || imageId == null || imageId.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasImage(group, imageId)) {
            owner.postMutation(player);
        }
    }

    public void putCanvasText(ServerPlayer player, String groupName, CanvasTextLayer text) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasText(group, text);
        owner.postMutation(player);
    }

    public void removeCanvasText(ServerPlayer player, String groupName, String textId) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || textId == null || textId.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasText(group, textId)) {
            owner.postMutation(player);
        }
    }

    public void setCanvasLayerOrder(ServerPlayer player, String groupName, List<String> layerOrder) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setCanvasLayerOrder(group, layerOrder);
        owner.postMutation(player);
    }

    public void moveQuestsInGroup(ServerPlayer player, String groupName, Map<String, int[]> positions) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || positions == null || positions.isEmpty()) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (Map.Entry<String, int[]> entry : positions.entrySet()) {
            String questId = entry.getKey();
            int[] coords = entry.getValue();
            if (questId == null || questId.isBlank() || coords == null || coords.length < 2) {
                continue;
            }
            QuestDefinition source = owner.definitionStore().quests().get(questId);
            if (source == null) {
                continue;
            }
            ChapterDefinition existingView = source.display().groups().get(group);
            if (existingView == null) {
                continue;
            }
            int targetX = coords[0];
            int targetY = coords[1];
            if (existingView.x() == targetX && existingView.y() == targetY) {
                continue;
            }

            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }

            Map<String, ChapterDefinition> groups = new HashMap<>(source.display().groups());
            groups.put(group, new ChapterDefinition(existingView.visible(), targetX, targetY, existingView.scale()));
            owner.definitionStore().upsert(withGroups(source, groups));
        }

        if (changed) {
            owner.postMutation(player);
        }
    }

    public void scaleQuestsInGroup(ServerPlayer player, String groupName, Map<String, Float> scales) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || scales == null || scales.isEmpty()) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (Map.Entry<String, Float> entry : scales.entrySet()) {
            String questId = entry.getKey();
            Float scaleValue = entry.getValue();
            if (questId == null || questId.isBlank() || scaleValue == null) {
                continue;
            }
            QuestDefinition source = owner.definitionStore().quests().get(questId);
            if (source == null) {
                continue;
            }
            ChapterDefinition existingView = source.display().groups().get(group);
            if (existingView == null) {
                continue;
            }
            float targetScale = scaleValue;
            if (Float.isNaN(targetScale) || Float.isInfinite(targetScale)) {
                continue;
            }
            targetScale = Math.max(0.5f, targetScale);
            if (existingView.scale() == targetScale) {
                continue;
            }

            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }

            Map<String, ChapterDefinition> groups = new HashMap<>(source.display().groups());
            groups.put(group, new ChapterDefinition(existingView.visible(), existingView.x(), existingView.y(), targetScale));
            owner.definitionStore().upsert(withGroups(source, groups));
        }

        if (changed) {
            owner.postMutation(player);
        }
    }
}
