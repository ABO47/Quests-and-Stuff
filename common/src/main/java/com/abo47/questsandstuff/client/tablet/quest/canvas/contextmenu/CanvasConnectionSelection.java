package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
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
        if (state == null || group == null || group.isBlank() || state.selectedQuestIds.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> selected = new java.util.LinkedHashSet<>(state.selectedQuestIds);
        List<CanvasContextMenuController.EdgeRef> edges = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : ClientQuestCache.questEntries()) {
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
        return edges;
    }
}
