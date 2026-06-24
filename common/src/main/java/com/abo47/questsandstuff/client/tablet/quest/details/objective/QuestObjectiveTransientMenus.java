package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;

final class QuestObjectiveTransientMenus {
    private QuestObjectiveTransientMenus() {
    }

    static void render(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH) {
        renderCommandRewardEditor(modal, state, player, refresh, modalW, modalH);
        renderItemSourcePicker(modal, state, refresh, modalW, modalH);
        QuestObjectiveXpEditor.render(modal, state, player, refresh, modalW, modalH);
    }

    private static void renderItemSourcePicker(WidgetGroup modal, TabletUiState state, Runnable refresh, int modalW, int modalH) {
        QuestDetailsPickerSession picker = state.questDetails.questDetailsPickerSession;
        if (!picker.itemSourcePicker() || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        String target = picker.itemSourceTarget() == null ? "" : picker.itemSourceTarget();
        if (target.isBlank()) {
            QuestDetailsTransientState.closeItemSourcePicker(state);
            return;
        }
        List<ContextAction> actions = new ArrayList<>();
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.PICK_ITEM), "icon", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsTransientState.closeItemSourcePicker(state);
            QuestDetailsWindow.openIconPicker(state, target);
        }));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.FROM_INVENTORY), "backpack", ModColors.INTERACTIVE, () -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsTransientState.closeItemSourcePicker(state);
            QuestDetailsWindow.openItemInventoryPicker(state, inventoryTarget(target));
        }));
        int rowCount = actions.size();
        int menuW = 132;
        int menuH = ContextMenuPanel.heightForRows(rowCount);
        int mx = Math.max(4, Math.min(picker.x(), modalW - menuW - 4));
        int my = Math.max(4, Math.min(picker.y(), modalH - menuH - 4));
        WidgetGroup menu = ContextMenuPanel.build(mx, my, menuW, actions, 0, rowCount, ModColors.BORDER_ACCENT, state, action -> refresh.run(), modalW, modalH);
        modal.addWidget(menu);
    }

    private static void renderCommandRewardEditor(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int modalW, int modalH) {
        if (!state.questDetails.questDetailsCommandRewardEditorOpen || !QuestDetailsEditState.canEdit(state)) {
            return;
        }
        int w = 232;
        int h = 72;
        int pad = 8;
        int buttonW = 82;
        int buttonH = 16;
        int buttonY = 48;
        int x = Math.max(4, Math.min(state.questDetails.questDetailsContextX, modalW - w - 4));
        int y = Math.max(4, Math.min(state.questDetails.questDetailsContextY, modalH - h - 4));
        WidgetGroup popup = panel(x, y, w, h, withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT);
        popup.addWidget(label(pad, 6, TabletVocabulary.text(QuestVocabulary.ENTER_COMMAND), ModColors.TEXT_PRIMARY));
        TextFieldWidget commandField = StyledTextFields.commitField(
                pad,
                24,
                w - pad * 2,
                16,
                () -> state.questDetails.questDetailsCommandRewardCommand,
                value -> state.questDetails.questDetailsCommandRewardCommand = value == null ? "" : value,
                () -> {
                    commitCommandReward(player, state);
                    refresh.run();
                },
                () -> {
                    closeCommandRewardEditor(state);
                    refresh.run();
                },
                () -> {
                    if (state.questDetails.questDetailsCommandRewardEditorOpen) {
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

    private static void commitCommandReward(Player player, TabletUiState state) {
        JsonObject json = new JsonObject();
        json.addProperty("id", state.questDetails.questDetailsCommandRewardId);
        json.addProperty("type", "questsandstuff:command");
        json.addProperty("command", state.questDetails.questDetailsCommandRewardCommand == null ? "" : state.questDetails.questDetailsCommandRewardCommand.trim());
        EditorCommandClient.putQuestRewardJson(player, state.questDetails.questDetailsCommandRewardQuestId, json.toString());
        closeCommandRewardEditor(state);
    }

    private static void closeCommandRewardEditor(TabletUiState state) {
        QuestDetailsTransientState.closeCommandRewardEditor(state);
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
