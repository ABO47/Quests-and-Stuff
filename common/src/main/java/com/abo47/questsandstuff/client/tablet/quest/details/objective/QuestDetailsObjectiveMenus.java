package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class QuestDetailsObjectiveMenus {
    private QuestDetailsObjectiveMenus() {
    }

    public static void renderTypePicker(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        renderCommandRewardEditor(modal, state, player, refresh, modalW, modalH);
        renderItemSourcePicker(modal, state, refresh, modalW, modalH);
        QuestObjectiveXpEditor.render(modal, state, player, refresh, modalW, modalH);
        if (!state.questDetailsTypePickerOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        boolean rewards = state.questDetailsTypePickerKind.startsWith("reward");
        boolean change = state.questDetailsTypePickerKind.endsWith("_change");
        List<QuestDetailsTypeChoice> choices = rewards ? QuestObjectiveTypeCatalog.rewardChoices() : QuestObjectiveTypeCatalog.taskChoices();
        List<ContextAction> typeActions = typePickerActions(choices, rewards, change, player, state, questId, quest);
        int rowCount = typeActions.size();
        int menuW = 130;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int mx = Math.max(4, Math.min(state.questDetailsTypePickerX, modalW - menuW - 4));
        int my = Math.max(4, Math.min(state.questDetailsTypePickerY, modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, typeActions, 0, rowCount, ModColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        if (!state.questDetailsContextOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        String kind = state.questDetailsContextKind == null ? "" : state.questDetailsContextKind;
        addCreateActions(actions, state, kind);
        if ("requirement".equals(kind) && !state.questDetailsContextId.isBlank()) {
            addRequirementActions(actions, state, player, questId, state.questDetailsContextId);
        }
        if ("reward".equals(kind) && !state.questDetailsContextId.isBlank()) {
            addRewardActions(actions, state, player, questId, state.questDetailsContextId);
        }
        if (actions.isEmpty()) {
            return;
        }
        int menuW = 140;
        int rowCount = ContextMenuPanel.rowActionCount(actions);
        int visibleRows = ContextMenuPanel.safeVisibleRows(rowCount, rowCount);
        int maxMenuH = Math.max(ContextMenuPanel.heightForRows(1), state.questDetailsH - 8);
        while (visibleRows > 1 && ContextMenuPanel.heightFor(actions, visibleRows) > maxMenuH) {
            visibleRows--;
        }
        state.questDetailsContextScrollMax = Math.max(0, rowCount - visibleRows);
        state.questDetailsContextScroll = ScrollController.clamp(state.questDetailsContextScroll, state.questDetailsContextScrollMax);
        int menuH = ContextMenuPanel.heightFor(actions, visibleRows);
        int x = Math.max(4, Math.min(state.questDetailsContextX, state.questDetailsW - menuW - 4));
        int y = Math.max(4, Math.min(state.questDetailsContextY, state.questDetailsH - menuH - 4));
        state.questDetailsContextX = x;
        state.questDetailsContextY = y;
        state.questDetailsContextW = menuW;
        state.questDetailsContextH = menuH;
        WidgetGroup menu = ContextMenuPanel.build(x, y, menuW, actions, state.questDetailsContextScroll, visibleRows, ModColors.BORDER_BASE, state, action -> {
            if (action.closeAfterClick()) {
                QuestDetailsTransientState.closeContext(state);
            }
            refresh.run();
        }, state.questDetailsW, state.questDetailsH, ScrollState.bind(
                () -> state.questDetailsContextScroll,
                value -> state.questDetailsContextScroll = ScrollController.clamp(value, state.questDetailsContextScrollMax),
                () -> state.contextMenuScrollDragging,
                dragging -> state.contextMenuScrollDragging = dragging
        ), refresh);
        modal.addWidget(menu);
    }

    private static void renderItemSourcePicker(WidgetGroup modal, TabletUiState state, Runnable refresh, int modalW, int modalH) {
        if (!state.questDetailsItemSourcePickerOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        String target = state.questDetailsItemSourcePickerTarget == null ? "" : state.questDetailsItemSourcePickerTarget;
        if (target.isBlank()) {
            QuestDetailsTransientState.closeItemSourcePicker(state);
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.PICK_ITEM), "icon", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsTransientState.closeItemSourcePicker(state);
            QuestDetailsWindow.openIconPicker(state, target);
        }));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.FROM_INVENTORY), "backpack", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsTransientState.closeItemSourcePicker(state);
            QuestDetailsWindow.openItemInventoryPicker(state, inventoryTarget(target));
        }));
        int rowCount = actions.size();
        int menuW = 132;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int mx = Math.max(4, Math.min(state.questDetailsItemSourcePickerX, modalW - menuW - 4));
        int my = Math.max(4, Math.min(state.questDetailsItemSourcePickerY, modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, actions, 0, rowCount, ModColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    private static void addCreateActions(List<ContextAction> actions, TabletUiState state, String kind) {
        if (kind.startsWith("requirement")) {
            actions.add(ContextActions.add(TabletVocabulary.text(QuestVocabulary.ADD_REQUIREMENT), () -> {
                state.contextDeleteConfirmKey = "";
                openTypePicker(state, "requirement");
            }));
        }
        if (kind.startsWith("reward")) {
            actions.add(ContextActions.add(TabletVocabulary.text(QuestVocabulary.ADD_REWARD), () -> {
                state.contextDeleteConfirmKey = "";
                openTypePicker(state, "reward");
            }));
        }
    }

    private static void addRequirementActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, String contextId) {
        actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CHANGE_REQUIREMENT), "rename", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            openTypePicker(state, "requirement_change", contextId);
        }));
        actions.add(ContextActions.promotedRename(TabletVocabulary.text(QuestVocabulary.RENAME_REQUIREMENT), () -> {
            state.contextDeleteConfirmKey = "";
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, true);
        }));
        CompoundTag requirementTag = ClientQuestCache.quest(questId)
                .getCompound("tasks")
                .getCompound(contextId);
        JsonObject requirementJson = parseObjectiveJson(requirementTag.getString("json"));
        if (QuestObjectiveXpEditor.isXp(requirementJson)) {
            actions.add(editSubmenu(List.of(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_XP), () -> {
                state.contextDeleteConfirmKey = "";
                QuestDetailsTransientState.openXpPicker(state, questId, contextId, true);
            }))));
        }
        addMoveActions(actions, () -> {
            state.contextDeleteConfirmKey = "";
            EditorCommandClient.moveQuestTask(player, questId, contextId, -1);
        }, () -> {
            state.contextDeleteConfirmKey = "";
            EditorCommandClient.moveQuestTask(player, questId, contextId, 1);
        });
        actions.add(visualsSubmenu(state, questId, contextId, true));
        String deleteKey = "quest_details_requirement:" + questId + ":" + contextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            EditorCommandClient.removeQuestTask(player, questId, contextId);
        }));
    }

    private static void addRewardActions(List<ContextAction> actions, TabletUiState state, Player player, String questId, String contextId) {
        CompoundTag rewardTag = ClientQuestCache.quest(questId)
                .getCompound("rewards")
                .getCompound(contextId);
        JsonObject rewardJson = parseObjectiveJson(rewardTag.getString("json"));
        boolean selectable = QuestObjectiveSelectableRewards.isSelectable(rewardJson);
        List<ContextAction> editActions = new ArrayList<>();
        if ("command".equals(QuestObjectiveJsons.typePath(rewardJson.has("type") ? rewardJson.get("type").getAsString() : ""))) {
            editActions.add(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_COMMAND_REWARD), () -> {
                state.contextDeleteConfirmKey = "";
                QuestObjectiveEditActions.openExistingCommandRewardEditor(state, questId, contextId);
            }));
        }
        if (QuestObjectiveXpEditor.isXp(rewardJson)) {
            editActions.add(ContextActions.rename(TabletVocabulary.text(QuestVocabulary.EDIT_XP), () -> {
                state.contextDeleteConfirmKey = "";
                QuestDetailsTransientState.openXpPicker(state, questId, contextId, false);
            }));
        }
        if (!selectable) {
            editActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.MAKE_SELECTABLE_REWARD), "selectable", ModColors.INTERACTIVE, () -> {
                state.contextDeleteConfirmKey = "";
                QuestObjectiveSelectableRewards.makeSelectable(player, questId, contextId);
            }));
        }
        actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CHANGE_REWARD), "rename", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            openTypePicker(state, "reward_change", contextId);
        }));
        actions.add(ContextActions.promotedRename(TabletVocabulary.text(QuestVocabulary.RENAME_REWARD), () -> {
            state.contextDeleteConfirmKey = "";
            QuestObjectiveEditActions.openObjectiveRenameEditor(state, questId, contextId, false);
        }));
        if (!editActions.isEmpty()) {
            actions.add(editSubmenu(editActions));
        }
        addMoveActions(actions, () -> {
            state.contextDeleteConfirmKey = "";
            EditorCommandClient.moveQuestReward(player, questId, contextId, -1);
        }, () -> {
            state.contextDeleteConfirmKey = "";
            EditorCommandClient.moveQuestReward(player, questId, contextId, 1);
        });
        actions.add(visualsSubmenu(state, questId, contextId, false));
        String deleteKey = "quest_details_reward:" + questId + ":" + contextId;
        actions.add(ContextActions.delete(state, deleteKey, TabletVocabulary.text(TabletVocabulary.COMMON_DELETE), () -> {
            EditorCommandClient.removeQuestReward(player, questId, contextId);
        }));
    }

    private static void addEntityIconActions(List<ContextAction> actions, TabletUiState state, String questId, String objectiveId, boolean task) {
        String icon = QuestObjectiveEditActions.objectiveIcon(questId, objectiveId, task);
        EntityIconControls.addEntityVariantAndMotionActions(
                actions,
                state,
                icon,
                task ? ModalTargets.objectiveTask(questId, objectiveId) : ModalTargets.objectiveReward(questId, objectiveId),
                () -> QuestDetailsTransientState.closeContext(state),
                () -> EntityMotionEditor.openObjectiveIcon(state, questId, objectiveId, task, state.questDetailsContextX, state.questDetailsContextY),
                () -> {
                }
        );
    }

    private static List<ContextAction> typePickerActions(
            List<QuestDetailsTypeChoice> choices,
            boolean rewards,
            boolean change,
            Player player,
            TabletUiState state,
            String questId,
            CompoundTag quest
    ) {
        if (rewards) {
            List<ContextAction> actions = new ArrayList<>();
            for (QuestDetailsTypeChoice choice : choices) {
                actions.add(typeChoiceAction(choice, true, change, player, state, questId, quest));
            }
            return actions;
        }

        List<ContextAction> actions = new ArrayList<>();
        addTypeGroup(actions, QuestVocabulary.CONTEXT_ITEM_TYPES, "icon", choices, List.of("item", "item_use", "item_interact", "recipe"), rewards, change, player, state, questId, quest);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_ENTITY_TYPES, "entity", choices, List.of("kill_entity", "entity_interact"), rewards, change, player, state, questId, quest);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_WORLD_TYPES, "biome", choices, List.of("block_interact", "structure", "biome", "location"), rewards, change, player, state, questId, quest);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_PROGRESS_TYPES, "stat", choices, List.of("advancement", "stat", "xp", "check"), rewards, change, player, state, questId, quest);
        return actions;
    }

    private static void addTypeGroup(
            List<ContextAction> actions,
            String labelKey,
            String icon,
            List<QuestDetailsTypeChoice> choices,
            List<String> types,
            boolean rewards,
            boolean change,
            Player player,
            TabletUiState state,
            String questId,
            CompoundTag quest
    ) {
        List<ContextAction> children = new ArrayList<>();
        for (String type : types) {
            QuestDetailsTypeChoice choice = typeChoice(choices, type);
            if (choice != null) {
                children.add(typeChoiceAction(choice, rewards, change, player, state, questId, quest));
            }
        }
        if (!children.isEmpty()) {
            actions.add(ContextActions.submenu(TabletVocabulary.text(labelKey), icon, ModColors.INTERACTIVE, children));
        }
    }

    private static QuestDetailsTypeChoice typeChoice(List<QuestDetailsTypeChoice> choices, String type) {
        for (QuestDetailsTypeChoice choice : choices) {
            if (choice.type().equals(type)) {
                return choice;
            }
        }
        return null;
    }

    private static ContextAction typeChoiceAction(
            QuestDetailsTypeChoice choice,
            boolean rewards,
            boolean change,
            Player player,
            TabletUiState state,
            String questId,
            CompoundTag quest
    ) {
        return ContextActions.action(choice.label(), choice.icon(), ModColors.INTERACTIVE, () -> {
            String targetId = state.questDetailsTypePickerTargetId;
            QuestDetailsTransientState.closeTypePicker(state);
            QuestDetailsTransientState.closeContext(state);
            if (rewards) {
                if (change && !targetId.isBlank()) {
                    QuestObjectiveEditActions.beginRewardChange(player, state, questId, targetId, choice.type());
                } else {
                    QuestObjectiveEditActions.beginRewardAdd(player, state, questId, quest, choice.type());
                }
            } else {
                if (change && !targetId.isBlank()) {
                    QuestObjectiveEditActions.beginTaskChange(player, state, questId, targetId, choice.type());
                } else {
                    QuestObjectiveEditActions.beginTaskAdd(player, state, questId, quest, choice.type());
                }
            }
        });
    }

    private static ContextAction editSubmenu(List<ContextAction> editActions) {
        return ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_EDIT), "rename", ModColors.INTERACTIVE, editActions);
    }

    private static void addMoveActions(List<ContextAction> actions, Runnable moveUp, Runnable moveDown) {
        actions.add(ContextActions.moveUp(moveUp));
        actions.add(ContextActions.moveDown(moveDown));
    }

    private static ContextAction visualsSubmenu(TabletUiState state, String questId, String objectiveId, boolean task) {
        List<ContextAction> visualActions = new ArrayList<>();
        visualActions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_ICON), "icon", ModColors.INTERACTIVE, () -> {
            state.contextDeleteConfirmKey = "";
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, objectiveId) : ModalTargets.rewardIcon(questId, objectiveId));
        }));
        addEntityIconActions(visualActions, state, questId, objectiveId, task);
        return ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_VISUALS), "style", ModColors.INTERACTIVE, visualActions);
    }

    private static void renderCommandRewardEditor(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH) {
        if (!state.questDetailsCommandRewardEditorOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        int w = 232;
        int h = 72;
        int pad = 8;
        int buttonW = 82;
        int buttonH = 16;
        int buttonY = 48;
        int x = Math.max(4, Math.min(state.questDetailsContextX, modalW - w - 4));
        int y = Math.max(4, Math.min(state.questDetailsContextY, modalH - h - 4));
        WidgetGroup popup = panel(x, y, w, h, withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT);
        popup.addWidget(label(pad, 6, TabletVocabulary.text(QuestVocabulary.ENTER_COMMAND), ModColors.TEXT_PRIMARY));
        TextFieldWidget commandField = StyledTextFields.commitField(
                pad,
                24,
                w - pad * 2,
                16,
                () -> state.questDetailsCommandRewardCommand,
                value -> state.questDetailsCommandRewardCommand = value == null ? "" : value,
                () -> {
                    commitCommandReward(player, state);
                    refresh.run();
                },
                () -> {
                    closeCommandRewardEditor(state);
                    refresh.run();
                },
                () -> {
                    if (state.questDetailsCommandRewardEditorOpen) {
                        commitCommandReward(player, state);
                        refresh.run();
                    }
                }
        );
        commandField.setClientSideWidget();
        commandField.setMaxStringLength(256);
        StyledTextFields.applyStandardStyle(commandField, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        popup.addWidget(commandField);
        ActionButtons.iconAction(popup, pad, buttonY, buttonW, buttonH, "add", TabletVocabulary.text(TabletVocabulary.COMMON_SAVE), ModColors.SUCCESS, null, click -> {
            commitCommandReward(player, state);
            refresh.run();
        });
        ActionButtons.iconAction(popup, w - pad - buttonW, buttonY, buttonW, buttonH, "close", TabletVocabulary.text(TabletVocabulary.COMMON_CANCEL), ModColors.ERROR, null, click -> {
            closeCommandRewardEditor(state);
            refresh.run();
        });
        commandField.setFocus(true);
        modal.addWidget(popup);
    }

    private static void openTypePicker(TabletUiState state, String kind) {
        openTypePicker(state, kind, "");
    }

    private static void openTypePicker(TabletUiState state, String kind, String targetId) {
        QuestDetailsTransientState.openTypePicker(state, kind, targetId);
    }

    private static void commitCommandReward(Player player, TabletUiState state) {
        JsonObject json = new JsonObject();
        json.addProperty("id", state.questDetailsCommandRewardId);
        json.addProperty("type", "questsandstuff:command");
        json.addProperty("command", state.questDetailsCommandRewardCommand == null ? "" : state.questDetailsCommandRewardCommand.trim());
        EditorCommandClient.putQuestRewardJson(player, state.questDetailsCommandRewardQuestId, json.toString());
        closeCommandRewardEditor(state);
    }

    private static void closeCommandRewardEditor(TabletUiState state) {
        QuestDetailsTransientState.closeCommandRewardEditor(state);
    }

    private static JsonObject parseObjectiveJson(String value) {
        try {
            return JsonParser.parseString(value == null || value.isBlank() ? "{}" : value).getAsJsonObject();
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    private static String inventoryTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(4)) {
            return target;
        }
        if (parsed.isTaskItem()) {
            return ModalTargets.taskInventoryItem(parsed.questId(), parsed.entryId(), parsed.type());
        }
        if (parsed.isRewardItem()) {
            return ModalTargets.rewardInventoryItem(parsed.questId(), parsed.entryId(), parsed.type());
        }
        return target;
    }
}
