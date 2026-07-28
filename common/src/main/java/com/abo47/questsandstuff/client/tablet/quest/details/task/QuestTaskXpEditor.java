package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import com.google.gson.JsonObject;

final class QuestTaskXpEditor {
    private static final String XP_TYPE = TaskJsonFactory.MOD + "xp";

    private QuestTaskXpEditor() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH) {
        QuestDetailsPickerSession picker = state.questDetails.questDetailsPickerSession;
        if (!picker.xpPicker() || !QuestDetailsEditController.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = picker.xpTask()
                ? taskActions(player, state)
                : rewardActions(player, state);
        int rowCount = actions.size();
        int menuW = picker.xpTask() ? 156 : 132;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int x = Math.max(4, Math.min(picker.x(), modalW - menuW - 4));
        int y = Math.max(4, Math.min(picker.y(), modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, 0, rowCount, TabletColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    static boolean isXp(JsonObject json) {
        return "xp".equals(TaskJsonFactory.typePath(TaskJsonFactory.asString(json, "type", "")));
    }

    private static List<ContextAction> taskActions(Player player, TabletUiState state) {
        ContextMenuSections sections = new ContextMenuSections();
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_POINTS_AUTOMATIC, "points", "automatic");
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_POINTS_MANUAL, "points", "manual");
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_POINTS_CONSUME, "points", "consume");
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_LEVELS_AUTOMATIC, "level", "automatic");
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_LEVELS_MANUAL, "level", "manual");
        addTaskAction(sections, player, state, QuestTranslationKeys.XP_LEVELS_CONSUME, "level", "consume");
        return sections.build();
    }

    private static List<ContextAction> rewardActions(Player player, TabletUiState state) {
        ContextMenuSections sections = new ContextMenuSections();
        addRewardAction(sections, player, state, QuestTranslationKeys.XP_POINTS, "points");
        addRewardAction(sections, player, state, QuestTranslationKeys.XP_LEVELS, "level");
        return sections.build();
    }

    private static void addTaskAction(ContextMenuSections sections, Player player, TabletUiState state, String labelKey, String mode, String collection) {
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TabletTranslationKeys.text(labelKey), "xp", TabletColors.INTERACTIVE, () -> {
            commitTask(player, state, mode, collection);
        }));
    }

    private static void addRewardAction(ContextMenuSections sections, Player player, TabletUiState state, String labelKey, String mode) {
        sections.add(ContextMenuSection.PRIMARY, ContextActionFactory.action(TabletTranslationKeys.text(labelKey), "xp", TabletColors.INTERACTIVE, () -> {
            commitReward(player, state, mode);
        }));
    }

    private static void commitTask(Player player, TabletUiState state, String mode, String collection) {
        QuestDetailsPickerSession picker = state.questDetails.questDetailsPickerSession;
        String questId = picker.xpQuestId();
        String id = picker.xpEntryId();
        JsonObject existing = existingJson(questId, id, true);
        JsonObject json = xpBase(existing, id);
        json.addProperty("mode", mode);
        json.addProperty("collection", collection);
        EditorQuestCommandClient.putQuestTaskJson(player, questId, json.toString());
        QuestDetailsTransientManager.closeXpPicker(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp task saved quest={} task={} mode={} collection={}", questId, id, mode, collection);
    }

    private static void commitReward(Player player, TabletUiState state, String mode) {
        QuestDetailsPickerSession picker = state.questDetails.questDetailsPickerSession;
        String questId = picker.xpQuestId();
        String id = picker.xpEntryId();
        JsonObject existing = existingJson(questId, id, false);
        JsonObject json = xpBase(existing, id);
        json.addProperty("mode", mode);
        EditorQuestCommandClient.putQuestRewardJson(player, questId, json.toString());
        QuestDetailsTransientManager.closeXpPicker(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details xp reward saved quest={} reward={} mode={}", questId, id, mode);
    }

    private static JsonObject xpBase(JsonObject existing, String id) {
        JsonObject json = TaskJsonFactory.base(id, XP_TYPE);
        json.addProperty("amount", QuestTaskDisplayText.amount(existing));
        copyIfPresent(existing, json, "title");
        String icon = TaskJsonFactory.asString(existing, "icon", "");
        json.addProperty("icon", icon.isBlank() || "xp".equals(icon) ? TaskJsonFactory.XP_CARD_ICON : icon);
        return json;
    }

    private static JsonObject existingJson(String questId, String id, boolean task) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        CompoundTag entries = quest.getCompound(task ? "tasks" : "rewards");
        return TaskJsonFactory.readForEdit(questId, id, task, entries.getCompound(id).getString("json")).value();
    }

    private static void copyIfPresent(JsonObject source, JsonObject target, String key) {
        String value = TaskJsonFactory.asString(source, key, "");
        if (!value.isBlank()) {
            target.addProperty(key, value);
        }
    }
}
