package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveXpEditor {
    private static final String XP_TYPE = QuestObjectiveJsons.MOD + "xp";

    private QuestObjectiveXpEditor() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH) {
        if (!state.questDetailsXpPickerOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = state.questDetailsXpPickerTask
                ? taskActions(player, state)
                : rewardActions(player, state);
        int rowCount = actions.size();
        int menuW = state.questDetailsXpPickerTask ? 156 : 132;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int x = Math.max(4, Math.min(state.questDetailsXpPickerX, modalW - menuW - 4));
        int y = Math.max(4, Math.min(state.questDetailsXpPickerY, modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, 0, rowCount, ModColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    static boolean isXp(JsonObject json) {
        return "xp".equals(QuestObjectiveJsons.typePath(QuestObjectiveJsons.asString(json, "type", "")));
    }

    private static List<ContextAction> taskActions(Player player, TabletUiState state) {
        List<ContextAction> actions = new ArrayList<>();
        addTaskAction(actions, player, state, QuestVocabulary.XP_POINTS_AUTOMATIC, "points", "automatic");
        addTaskAction(actions, player, state, QuestVocabulary.XP_POINTS_MANUAL, "points", "manual");
        addTaskAction(actions, player, state, QuestVocabulary.XP_POINTS_CONSUME, "points", "consume");
        addTaskAction(actions, player, state, QuestVocabulary.XP_LEVELS_AUTOMATIC, "level", "automatic");
        addTaskAction(actions, player, state, QuestVocabulary.XP_LEVELS_MANUAL, "level", "manual");
        addTaskAction(actions, player, state, QuestVocabulary.XP_LEVELS_CONSUME, "level", "consume");
        return actions;
    }

    private static List<ContextAction> rewardActions(Player player, TabletUiState state) {
        List<ContextAction> actions = new ArrayList<>();
        addRewardAction(actions, player, state, QuestVocabulary.XP_POINTS, "points");
        addRewardAction(actions, player, state, QuestVocabulary.XP_LEVELS, "level");
        return actions;
    }

    private static void addTaskAction(List<ContextAction> actions, Player player, TabletUiState state, String labelKey, String mode, String collection) {
        actions.add(ContextActions.action(QuestVocabulary.text(labelKey), "xp", ModColors.INTERACTIVE, () -> {
            commitTask(player, state, mode, collection);
        }));
    }

    private static void addRewardAction(List<ContextAction> actions, Player player, TabletUiState state, String labelKey, String mode) {
        actions.add(ContextActions.action(QuestVocabulary.text(labelKey), "xp", ModColors.INTERACTIVE, () -> {
            commitReward(player, state, mode);
        }));
    }

    private static void commitTask(Player player, TabletUiState state, String mode, String collection) {
        String questId = state.questDetailsXpPickerQuestId;
        String id = state.questDetailsXpPickerEntryId;
        JsonObject existing = existingJson(questId, id, true);
        JsonObject json = xpBase(existing, id);
        json.addProperty("mode", mode);
        json.addProperty("collection", collection);
        EditorCommandClient.putQuestTaskJson(player, questId, json.toString());
        QuestDetailsTransientState.closeXpPicker(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp requirement saved quest={} task={} mode={} collection={}", questId, id, mode, collection);
    }

    private static void commitReward(Player player, TabletUiState state, String mode) {
        String questId = state.questDetailsXpPickerQuestId;
        String id = state.questDetailsXpPickerEntryId;
        JsonObject existing = existingJson(questId, id, false);
        JsonObject json = xpBase(existing, id);
        json.addProperty("mode", mode);
        EditorCommandClient.putQuestRewardJson(player, questId, json.toString());
        QuestDetailsTransientState.closeXpPicker(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp reward saved quest={} reward={} mode={}", questId, id, mode);
    }

    private static JsonObject xpBase(JsonObject existing, String id) {
        JsonObject json = QuestObjectiveJsons.base(id, XP_TYPE);
        json.addProperty("amount", QuestObjectiveDisplayText.amount(existing));
        copyIfPresent(existing, json, "title");
        String icon = QuestObjectiveJsons.asString(existing, "icon", "");
        json.addProperty("icon", icon.isBlank() || "xp".equals(icon) ? QuestObjectiveJsons.XP_CARD_ICON : icon);
        return json;
    }

    private static JsonObject existingJson(String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        return QuestObjectiveJsons.read(entries.getCompound(id).getString("json"));
    }

    private static void copyIfPresent(JsonObject source, JsonObject target, String key) {
        String value = QuestObjectiveJsons.asString(source, key, "");
        if (!value.isBlank()) {
            target.addProperty(key, value);
        }
    }
}
