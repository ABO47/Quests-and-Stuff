package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestTaskRenameActions {
    private QuestTaskRenameActions() {
    }

    static void openTaskRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        JsonObject json = TaskJsonFactory.readForEdit(questId, id, task, entries.getCompound(id).getString("json")).value();
        QuestDetailsTransientManager.openTaskRename(
                state,
                questId,
                id,
                task,
                QuestTaskDisplayText.displayName(json, TaskJsonFactory.asString(json, "type", ""))
        );
    }

    static void putTaskTitle(Player player, String questId, String id, String title, boolean task) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
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
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details task renamed quest={} id={} task={} title={}", questId, id, task, normalizedTitle);
    }
}
