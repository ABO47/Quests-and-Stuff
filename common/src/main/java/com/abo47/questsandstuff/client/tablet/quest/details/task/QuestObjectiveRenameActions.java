package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestObjectiveRenameActions {
    private QuestObjectiveRenameActions() {
    }

    static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        JsonObject json = TaskJsonFactory.readForEdit(questId, id, task, entries.getCompound(id).getString("json")).value();
        QuestDetailsTransientManager.openObjectiveRename(
                state,
                questId,
                id,
                task,
                QuestObjectiveDisplayText.displayName(json, TaskJsonFactory.asString(json, "type", ""))
        );
    }

    static void putObjectiveTitle(Player player, String questId, String id, String title, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        CompoundTag entry = entries.getCompound(id);
        JsonObject json = TaskJsonFactory.readForEdit(questId, id, task, entry.getString("json")).value();
        if (!json.has("id")) {
            json.addProperty("id", id);
        }
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isBlank()) {
            json.remove("title");
        } else {
            json.addProperty("title", normalizedTitle);
        }
        if (task) {
            EditorQuestCommandClient.putQuestTaskJson(player, questId, json.toString());
        } else {
            EditorQuestCommandClient.putQuestRewardJson(player, questId, json.toString());
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details objective renamed quest={} id={} task={} title={}", questId, id, task, normalizedTitle);
    }
}
