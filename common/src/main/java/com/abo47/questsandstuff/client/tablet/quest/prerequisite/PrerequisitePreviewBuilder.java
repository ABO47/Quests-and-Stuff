package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.ClientQuestDefinitionSnapshots;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.GroupDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class PrerequisitePreviewBuilder {
    private static final int COMPACT_X_OFFSET = 128;
    private static final int COMPACT_Y_STEP = 70;

    private PrerequisitePreviewBuilder() {
    }

    static CanvasBlueprint build(String group, PrerequisiteConnectionModel model, boolean externalMode) {
        if (model == null) {
            return CanvasBlueprint.empty();
        }
        QuestDefinition focus = ClientQuestDefinitionSnapshots.fromClientTag(model.questId(), model.questTag());
        if (focus == null) {
            return CanvasBlueprint.empty();
        }

        boolean ecMode = model.isExclusiveChoice();
        Map<String, QuestDefinition> definitions = definitionsForPreview(model.questId(), focus, model.rows());
        Map<String, QuestPlacement> placements = placementsForPreview(group, model.questId(), definitions, model.rows(), externalMode);
        Map<String, Set<String>> prerequisitesByTarget = prerequisitesByTarget(model.rows());
        List<CanvasBlueprint.QuestEntry> entries = new ArrayList<>();
        List<CanvasBlueprint.ExclusiveChoiceEntry> ecEntries = new ArrayList<>();
        List<String> order = new ArrayList<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            String id = entry.getKey();
            if (model.questId().equals(id)) {
                continue;
            }
            QuestPlacement placement = placements.get(id);
            if (placement == null) {
                continue;
            }
            entries.add(new CanvasBlueprint.QuestEntry(id, placement.group(), placement.x(), placement.y(), placement.scale(), withPrerequisites(entry.getValue(), prerequisitesByTarget.getOrDefault(id, Set.of()))));
            order.add(CanvasLayerOrdering.questKey(id));
        }
        if (ecMode) {
            CanvasExclusiveChoice focusEc = findEcById(model.questId(), group);
            if (focusEc != null) {
                ecEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        model.questId(), group, model.ecX(), model.ecY(),
                        model.ecW(), model.ecH(), focusEc.rotation(),
                        focusEc.background(), focusEc.connectionQuestIds(),
                        prerequisitesByTarget.getOrDefault(model.questId(), Set.of()),
                        focusEc.connectionColors(), focusEc.connectionModes(),
                        focusEc.connectionTextures(), focusEc.connectionTextureSpacings(),
                        focusEc.hiddenConnections()));
            } else {
                ecEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        model.questId(), group, model.ecX(), model.ecY(),
                        model.ecW(), model.ecH(), 0,
                        focus.display().questBackground(), List.of(),
                        prerequisitesByTarget.getOrDefault(model.questId(), Set.of()),
                        Map.of(), Map.of(), Map.of(), Map.of(), Set.of()));
            }
            order.add(CanvasLayerOrdering.exclusiveChoiceKey(model.questId()));
        } else {
            Set<String> ecIds = ecIdsFromRows(model.rows(), model.questId());
            for (String ecId : ecIds) {
                CanvasExclusiveChoice ec = findEcById(ecId, group);
                if (ec == null) {
                    continue;
                }
                QuestPlacement ecPlacement = placements.get(ecId);
                int ecX = ecPlacement != null ? ecPlacement.x() : ec.x();
                int ecY = ecPlacement != null ? ecPlacement.y() : ec.y();
                ecEntries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        ecId, group, ecX, ecY, ec.w(), ec.h(), ec.rotation(),
                        ec.background(), ec.connectionQuestIds(),
                        prerequisitesByTarget.getOrDefault(ecId, Set.of()),
                        ec.connectionColors(), ec.connectionModes(),
                        ec.connectionTextures(), ec.connectionTextureSpacings(),
                        ec.hiddenConnections()));
                order.add(CanvasLayerOrdering.exclusiveChoiceKey(ecId));
            }
            QuestPlacement focusPlacement = placements.get(model.questId());
            if (focusPlacement != null) {
                entries.add(new CanvasBlueprint.QuestEntry(model.questId(), focusPlacement.group(), focusPlacement.x(), focusPlacement.y(), focusPlacement.scale(), withPrerequisites(focus, prerequisitesByTarget.getOrDefault(model.questId(), Set.of()))));
                order.add(CanvasLayerOrdering.questKey(model.questId()));
            }
        }
        Origin origin = origin(entries, ecEntries);
        return new CanvasBlueprint(CanvasBlueprint.CURRENT_SCHEMA, focus.display().title(), origin.x(), origin.y(), entries, List.of(), List.of(), order, ecEntries);
    }

    private static Map<String, QuestDefinition> definitionsForPreview(String questId, QuestDefinition focus, List<PrerequisiteConnectionRow> rows) {
        Map<String, QuestDefinition> definitions = new LinkedHashMap<>();
        definitions.put(questId, focus);
        for (PrerequisiteConnectionRow row : rows) {
            addDefinition(definitions, row.sourceId());
            addDefinition(definitions, row.targetId());
        }
        return definitions;
    }

    private static void addDefinition(Map<String, QuestDefinition> definitions, String questId) {
        if (definitions.containsKey(questId)) {
            return;
        }
        QuestDefinition definition = ClientQuestDefinitionSnapshots.fromClientTag(questId, ClientQuestStateFacade.quest(questId));
        if (definition != null) {
            definitions.put(questId, definition);
        }
    }

    private static Map<String, QuestPlacement> placementsForPreview(String group, String focusId, Map<String, QuestDefinition> definitions, List<PrerequisiteConnectionRow> rows, boolean externalMode) {
        if (externalMode) {
            return compactPlacements(group, focusId, definitions, rows);
        }
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        for (Map.Entry<String, QuestDefinition> entry : definitions.entrySet()) {
            placements.put(entry.getKey(), actualPlacement(entry.getValue(), group));
        }
        return placements;
    }

    private static Map<String, QuestPlacement> compactPlacements(String group, String focusId, Map<String, QuestDefinition> definitions, List<PrerequisiteConnectionRow> rows) {
        Map<String, QuestPlacement> placements = new LinkedHashMap<>();
        placements.put(focusId, new QuestPlacement(group, 0, 0, 1.0f));
        List<String> incoming = uniqueOtherIds(rows, focusId, PrerequisiteConnectionKind.INCOMING);
        List<String> outgoing = uniqueOtherIds(rows, focusId, PrerequisiteConnectionKind.OUTGOING);
        addCompactColumn(placements, group, incoming, -COMPACT_X_OFFSET);
        addCompactColumn(placements, group, outgoing, COMPACT_X_OFFSET);
        for (String id : definitions.keySet()) {
            placements.putIfAbsent(id, new QuestPlacement(group, 0, (placements.size() + 1) * COMPACT_Y_STEP, 1.0f));
        }
        return placements;
    }

    private static List<String> uniqueOtherIds(List<PrerequisiteConnectionRow> rows, String focusId, PrerequisiteConnectionKind kind) {
        Set<String> values = new LinkedHashSet<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.kind() != kind) {
                continue;
            }
            values.add(focusId.equals(row.sourceId()) ? row.targetId() : row.sourceId());
        }
        return List.copyOf(values);
    }

    private static void addCompactColumn(Map<String, QuestPlacement> placements, String group, List<String> ids, int x) {
        int count = ids.size();
        for (int i = 0; i < ids.size(); i++) {
            int y = Math.round((i - (count - 1) / 2.0f) * COMPACT_Y_STEP);
            placements.put(ids.get(i), new QuestPlacement(group, x, y, 1.0f));
        }
    }

    private static QuestPlacement actualPlacement(QuestDefinition definition, String preferredGroup) {
        GroupDef preferred = definition.display().groups().get(preferredGroup);
        if (preferred != null) {
            return new QuestPlacement(preferredGroup, preferred.x(), preferred.y(), preferred.scale());
        }
        for (Map.Entry<String, GroupDef> entry : definition.display().groups().entrySet()) {
            GroupDef view = entry.getValue();
            return new QuestPlacement(entry.getKey(), view.x(), view.y(), view.scale());
        }
        return new QuestPlacement(preferredGroup, 0, 0, 1.0f);
    }

    private static Map<String, Set<String>> prerequisitesByTarget(List<PrerequisiteConnectionRow> rows) {
        Map<String, Set<String>> prerequisites = new LinkedHashMap<>();
        for (PrerequisiteConnectionRow row : rows) {
            prerequisites.computeIfAbsent(row.targetId(), ignored -> new LinkedHashSet<>()).add(row.sourceId());
        }
        return prerequisites;
    }

    private static QuestDefinition withPrerequisites(QuestDefinition definition, Set<String> prerequisites) {
        return new QuestDefinition(
                definition.schema(),
                definition.id(),
                definition.display(),
                definition.settings(),
                prerequisites,
                definition.connectionColors(),
                definition.connectionModes(),
                definition.hiddenConnections(),
                definition.connectionTextures(),
                definition.connectionTextureSpacings(),
                definition.tasksOrder(),
                definition.rewardsOrder(),
                definition.tasks(),
                definition.rewards()
        );
    }

    private static Origin origin(List<CanvasBlueprint.QuestEntry> entries, List<CanvasBlueprint.ExclusiveChoiceEntry> ecEntries) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (CanvasBlueprint.QuestEntry entry : entries) {
            minX = Math.min(minX, entry.sourceX());
            minY = Math.min(minY, entry.sourceY());
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : ecEntries) {
            minX = Math.min(minX, entry.sourceX());
            minY = Math.min(minY, entry.sourceY());
        }
        return minX == Integer.MAX_VALUE ? new Origin(0, 0) : new Origin(minX, minY);
    }

    private static Set<String> ecIdsFromRows(List<PrerequisiteConnectionRow> rows, String focusId) {
        Set<String> ecIds = new LinkedHashSet<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.exclusiveChoice()) {
                if (!row.sourceId().equals(focusId)) {
                    ecIds.add(row.sourceId());
                }
                if (!row.targetId().equals(focusId)) {
                    ecIds.add(row.targetId());
                }
            }
        }
        return ecIds;
    }

    private static CanvasExclusiveChoice findEcById(String id, String group) {
        if (group == null || group.isBlank()) {
            return null;
        }
        List<CanvasExclusiveChoice> ecs = ClientQuestStateFacade.canvasExclusiveChoicesByGroup().get(group);
        if (ecs == null) {
            return null;
        }
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec.id().equals(id)) {
                return ec;
            }
        }
        return null;
    }

    private record QuestPlacement(String group, int x, int y, float scale) {
    }

    private record Origin(int x, int y) {
    }
}
