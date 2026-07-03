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

import com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu.CanvasContextMenuController.ConnectionRef;

public final class CanvasConnectionSelection {
    private CanvasConnectionSelection() {
    }

    public static List<ConnectionRef> selectedConnectedEdges(TabletUiState state, String group) {
        if (state == null || group == null || group.isBlank() || state.canvas.canvasSelection.questIds().isEmpty()) {
            return List.of();
        }
        java.util.Set<String> selected = new java.util.LinkedHashSet<>(state.canvas.canvasSelection.questIds());
        List<ConnectionRef> connections = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : ClientQuestStateFacade.questEntries()) {
            String questId = entry.getKey();
            CompoundTag quest = entry.getValue();
            if (quest == null || !quest.getCompound("chapters").contains(group)) {
                continue;
            }
            ListTag prerequisites = quest.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                if (selected.contains(questId) || selected.contains(prerequisiteId)) {
                    connections.add(new ConnectionRef(prerequisiteId, questId));
                }
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(group, List.of())) {
            for (String connectedQuestId : ec.connectionQuestIds()) {
                if (selected.contains(connectedQuestId)) {
                    connections.add(new ConnectionRef(ec.id(), connectedQuestId));
                }
            }
            for (String prerequisiteQuestId : ec.prerequisiteQuestIds()) {
                if (selected.contains(prerequisiteQuestId)) {
                    connections.add(new ConnectionRef(prerequisiteQuestId, ec.id()));
                }
            }
        }
        return connections;
    }
}
