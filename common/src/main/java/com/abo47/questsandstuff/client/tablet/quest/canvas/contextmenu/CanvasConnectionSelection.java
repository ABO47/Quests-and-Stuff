package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CanvasConnectionSelection {
    private CanvasConnectionSelection() {
    }

    public static List<CanvasContextMenuController.EdgeRef> selectedConnectedEdges(TabletUiState state, String group) {
        if (state == null || group == null || group.isBlank() || state.canvas.canvasSelection.questIds().isEmpty()) {
            return List.of();
        }
        java.util.Set<String> selected = new java.util.LinkedHashSet<>(state.canvas.canvasSelection.questIds());
        List<CanvasContextMenuController.EdgeRef> edges = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : ClientQuestStateFacade.questEntries()) {
            String questId = entry.getKey();
            CompoundTag quest = entry.getValue();
            if (quest == null || !quest.getCompound("groups").contains(group)) {
                continue;
            }
            ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                if (selected.contains(questId) || selected.contains(prerequisiteId)) {
                    edges.add(new CanvasContextMenuController.EdgeRef(prerequisiteId, questId));
                }
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            for (String connectedQuestId : ec.connectionQuestIds()) {
                if (selected.contains(connectedQuestId)) {
                    edges.add(new CanvasContextMenuController.EdgeRef(ec.id(), connectedQuestId));
                }
            }
            for (String prerequisiteQuestId : ec.prerequisiteQuestIds()) {
                if (selected.contains(prerequisiteQuestId)) {
                    edges.add(new CanvasContextMenuController.EdgeRef(prerequisiteQuestId, ec.id()));
                }
            }
        }
        return edges;
    }
}
