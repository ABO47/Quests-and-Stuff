package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

final class QuestObjectiveTypePickerMenu {
    private QuestObjectiveTypePickerMenu() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        QuestDetailsPickerSession picker = state.questDetailsPickerSession;
        if (!picker.typePicker() || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        boolean rewards = picker.kind().startsWith("reward");
        boolean change = picker.kind().endsWith("_change");
        List<QuestDetailsTypeChoice> choices = rewards ? QuestObjectiveTypeCatalog.rewardChoices() : QuestObjectiveTypeCatalog.taskChoices();
        List<ContextAction> typeActions = actions(choices, rewards, change, player, state, questId, quest, picker.targetId());
        int rowCount = typeActions.size();
        int menuW = 130;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int mx = Math.max(4, Math.min(picker.x(), modalW - menuW - 4));
        int my = Math.max(4, Math.min(picker.y(), modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, typeActions, 0, rowCount, ModColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    private static List<ContextAction> actions(
            List<QuestDetailsTypeChoice> choices,
            boolean rewards,
            boolean change,
            Player player,
            TabletUiState state,
            String questId,
            CompoundTag quest,
            String targetId
    ) {
        if (rewards) {
            List<ContextAction> actions = new ArrayList<>();
            for (QuestDetailsTypeChoice choice : choices) {
                actions.add(typeChoiceAction(choice, true, change, player, state, questId, quest, targetId));
            }
            return actions;
        }

        List<ContextAction> actions = new ArrayList<>();
        addTypeGroup(actions, QuestVocabulary.CONTEXT_ITEM_TYPES, "icon", choices, List.of("item", "item_use", "item_interact", "recipe"), rewards, change, player, state, questId, quest, targetId);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_ENTITY_TYPES, "entity", choices, List.of("kill_entity", "entity_interact"), rewards, change, player, state, questId, quest, targetId);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_WORLD_TYPES, "biome", choices, List.of("block_interact", "structure", "biome", "location"), rewards, change, player, state, questId, quest, targetId);
        addTypeGroup(actions, QuestVocabulary.CONTEXT_PROGRESS_TYPES, "stat", choices, List.of("advancement", "stat", "xp", "check"), rewards, change, player, state, questId, quest, targetId);
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
            CompoundTag quest,
            String targetId
    ) {
        List<ContextAction> children = new ArrayList<>();
        for (String type : types) {
            QuestDetailsTypeChoice choice = typeChoice(choices, type);
            if (choice != null) {
                children.add(typeChoiceAction(choice, rewards, change, player, state, questId, quest, targetId));
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
            CompoundTag quest,
            String targetId
    ) {
        return ContextActions.action(choice.label(), choice.icon(), ModColors.INTERACTIVE, () -> {
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
}
