package com.abo47.questsandstuff.client.tablet.quest.prerequisite;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice.DEFAULT_HEIGHT;
import static com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice.DEFAULT_WIDTH;

record PrerequisiteConnectionModel(
        String questId,
        CompoundTag questTag,
        String targetTitle,
        List<PrerequisiteConnectionRow> allRows,
        List<PrerequisiteConnectionRow> modeRows,
        List<PrerequisiteConnectionRow> rows,
        boolean isExclusiveChoice,
        int ecX,
        int ecY,
        int ecW,
        int ecH
) {
    static PrerequisiteConnectionModel build(String questId, CompoundTag questTag, String group, String query, boolean externalMode) {
        String safeQuestId = safe(questId);
        CompoundTag safeQuestTag = questTag == null ? new CompoundTag() : questTag.copy();
        String title = questTitle(safeQuestId, safeQuestTag);
        List<PrerequisiteConnectionRow> all = connectionRows(safeQuestId, safeQuestTag, title);
        List<PrerequisiteConnectionRow> mode = rowsForMode(all, group, externalMode);
        List<PrerequisiteConnectionRow> visible = filteredRows(mode, query);
        return new PrerequisiteConnectionModel(
                safeQuestId,
                safeQuestTag,
                title,
                List.copyOf(all),
                List.copyOf(mode),
                List.copyOf(visible),
                false,
                0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT
        );
    }

    static PrerequisiteConnectionModel buildForEc(CanvasExclusiveChoice ec, String group, String query) {
        if (ec == null) {
            return new PrerequisiteConnectionModel("", new CompoundTag(), "", List.of(), List.of(), List.of(), false, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        String safeId = safe(ec.id());
        CompoundTag tag = new CompoundTag();
        tag.putString("quest_background", ec.background());
        List<PrerequisiteConnectionRow> all = new ArrayList<>();
        for (String questId : ec.prerequisiteQuestIds()) {
            CompoundTag questTag = ClientQuestStateFacade.quest(questId);
            String title = questTitle(questId, questTag);
            all.add(new PrerequisiteConnectionRow(questId, safeId, title, safeId, title, questTag == null ? "" : questTag.getString("icon"), PrerequisiteConnectionKind.INCOMING, true));
        }
        for (String questId : ec.connectionQuestIds()) {
            CompoundTag questTag = ClientQuestStateFacade.quest(questId);
            String title = questTitle(questId, questTag);
            all.add(new PrerequisiteConnectionRow(safeId, questId, safeId, title, title, questTag == null ? "" : questTag.getString("icon"), PrerequisiteConnectionKind.OUTGOING, true));
        }
        List<PrerequisiteConnectionRow> visible = filteredRows(all, query);
        return new PrerequisiteConnectionModel(safeId, tag, safeId, List.copyOf(all), List.copyOf(all), List.copyOf(visible), true, ec.x(), ec.y(), ec.w(), ec.h());
    }

    PrerequisiteConnectionRow selectedRow(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        for (PrerequisiteConnectionRow row : allRows) {
            if (row.key().equals(key)) {
                return row;
            }
        }
        return null;
    }

    Set<String> highlightedQuests(Set<String> highlightedConnections) {
        if (highlightedConnections == null || highlightedConnections.isEmpty()) {
            return Set.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (highlightedConnections.contains(row.key())) {
                ids.add(row.sourceId());
                ids.add(row.targetId());
            }
        }
        return ids;
    }

    static Set<String> highlightedConnections(String hoveredKey, String selectedKey) {
        String key = hoveredKey != null && !hoveredKey.isBlank() ? hoveredKey : safe(selectedKey);
        return key.isBlank() ? Set.of() : Set.of(key);
    }

    private static List<PrerequisiteConnectionRow> connectionRows(String questId, CompoundTag questTag, String targetTitle) {
        if (questId.isBlank() || questTag == null || questTag.isEmpty()) {
            return List.of();
        }
        Map<String, PrerequisiteConnectionRow> rows = new LinkedHashMap<>();
        addIncomingRows(rows, questId, questTag, targetTitle);
        addOutgoingRows(rows, questId, targetTitle);
        addEcConnectionRows(rows, questId, targetTitle);
        return List.copyOf(rows.values());
    }

    private static void addEcConnectionRows(Map<String, PrerequisiteConnectionRow> rows, String questId, String targetTitle) {
        for (List<CanvasExclusiveChoice> ecs : ClientQuestStateFacade.canvasExclusiveChoicesByGroup().values()) {
            for (CanvasExclusiveChoice ec : ecs) {
                String ecId = safe(ec.id());
                if (ecId.isBlank()) continue;
                if (ec.connectionQuestIds().contains(questId)) {
                    String key = QuestConnectionMetadata.edgeKey(ecId, questId);
                    rows.putIfAbsent(key, new PrerequisiteConnectionRow(ecId, questId, ecId, targetTitle, ecId, "", PrerequisiteConnectionKind.INCOMING, true));
                }
                if (ec.prerequisiteQuestIds().contains(questId)) {
                    String key = QuestConnectionMetadata.edgeKey(questId, ecId);
                    rows.putIfAbsent(key, new PrerequisiteConnectionRow(questId, ecId, targetTitle, ecId, ecId, "", PrerequisiteConnectionKind.OUTGOING, true));
                }
            }
        }
    }

    private static void addIncomingRows(Map<String, PrerequisiteConnectionRow> rows, String questId, CompoundTag questTag, String targetTitle) {
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            String sourceId = safe(prerequisites.getString(i));
            if (sourceId.isBlank()) {
                continue;
            }
            CompoundTag sourceTag = ClientQuestStateFacade.quest(sourceId);
            PrerequisiteConnectionRow row = new PrerequisiteConnectionRow(
                    sourceId,
                    questId,
                    questTitle(sourceId, sourceTag),
                    targetTitle,
                    questTitle(sourceId, sourceTag),
                    sourceTag == null ? "" : sourceTag.getString("icon"),
                    PrerequisiteConnectionKind.INCOMING,
                    false
            );
            rows.putIfAbsent(row.key(), row);
        }
    }

    private static void addOutgoingRows(Map<String, PrerequisiteConnectionRow> rows, String questId, String sourceTitle) {
        for (Map.Entry<String, CompoundTag> entry : ClientQuestStateFacade.questEntries()) {
            String targetId = entry.getKey();
            if (questId.equals(targetId)) {
                continue;
            }
            CompoundTag targetTag = entry.getValue();
            if (!hasPrerequisite(targetTag, questId)) {
                continue;
            }
            PrerequisiteConnectionRow row = new PrerequisiteConnectionRow(
                    questId,
                    targetId,
                    sourceTitle,
                    questTitle(targetId, targetTag),
                    questTitle(targetId, targetTag),
                    targetTag == null ? "" : targetTag.getString("icon"),
                    PrerequisiteConnectionKind.OUTGOING,
                    false
            );
            rows.putIfAbsent(row.key(), row);
        }
    }

    static boolean hasPrerequisite(CompoundTag questTag, String prerequisiteId) {
        if (questTag == null || prerequisiteId == null || prerequisiteId.isBlank()) {
            return false;
        }
        ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
        for (int i = 0; i < prerequisites.size(); i++) {
            if (QuestConnectionMetadata.metadataKey(prerequisiteId).equals(QuestConnectionMetadata.metadataKey(prerequisites.getString(i)))) {
                return true;
            }
        }
        return false;
    }

    private static List<PrerequisiteConnectionRow> filteredRows(List<PrerequisiteConnectionRow> rows, String query) {
        if (SearchFilter.normalize(query).isBlank()) {
            return rows;
        }
        List<PrerequisiteConnectionRow> filtered = new ArrayList<>();
        for (PrerequisiteConnectionRow row : rows) {
            String display = row.sourceTitle() + " " + row.targetTitle() + " " + row.otherTitle();
            if (SearchFilter.matches(query, row.sourceId() + " " + row.targetId(), display)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<PrerequisiteConnectionRow> rowsForMode(List<PrerequisiteConnectionRow> rows, String group, boolean externalMode) {
        List<PrerequisiteConnectionRow> filtered = new ArrayList<>();
        for (PrerequisiteConnectionRow row : rows) {
            if (row.exclusiveChoice()) {
                filtered.add(row);
                continue;
            }
            boolean local = isLocalConnection(row, group);
            if ((externalMode && !local) || (!externalMode && local)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    static boolean isLocalConnection(PrerequisiteConnectionRow row, String group) {
        if (group == null || group.isBlank()) {
            return true;
        }
        return questInGroup(row.sourceId(), group) && questInGroup(row.targetId(), group);
    }

    private static boolean questInGroup(String questId, String group) {
        CompoundTag questTag = ClientQuestStateFacade.quest(questId);
        if (questTag == null || questTag.isEmpty() || group == null || group.isBlank()) {
            return false;
        }
        return questTag.getCompound("groups").contains(group, Tag.TAG_COMPOUND);
    }

    static String questTitle(String questId, CompoundTag questTag) {
        String title = questTag == null ? "" : questTag.getString("title");
        if (title != null && !title.isBlank()) {
            return title;
        }
        return questId == null || questId.isBlank() ? TabletTranslationKeys.text(TabletTranslationKeys.COMMON_UNKNOWN) : questId;
    }

    private static String safe(String value) {
        return QuestConnectionMetadata.normalizeQuestId(value);
    }
}
