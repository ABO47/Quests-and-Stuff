package com.abo47.questsandstuff.client.tablet.quest.details.task;

import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TabletTextTextures;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;

final class QuestTaskInlineFields {
    private QuestTaskInlineFields() {
    }

    static void renderTaskTitle(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsTaskEntry entry, boolean task, int x, int y, int rightX) {
        renderTaskTitle(parent, state, player, refresh, questId, entry, task, x, y, rightX, TabletColors.TEXT_PRIMARY);
    }

    static void renderTaskTitle(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsTaskEntry entry, boolean task, int x, int y, int rightX, int color) {
        boolean renaming = isRenamingTask(state, questId, entry.id(), task);
        int doneW = renaming ? 12 : 0;
        int fieldW = Math.max(18, rightX - x - doneW - (renaming ? 3 : 0));
        if (!renaming) {
            renderDisplayText(parent, x, y, fieldW, QuestTaskDisplayText.displayName(entry.json(), entry.type()), color, TextTexture.TextType.LEFT_HIDE);
            return;
        }
        InlineRenameField field = new InlineRenameField(
                x,
                y,
                fieldW,
                16,
                () -> state.questDetails.questDetailsTaskRenameDraft,
                value -> state.questDetails.questDetailsTaskRenameDraft = sanitizeTaskTitle(value),
                () -> {
                    commitTaskRename(player, state);
                    refresh.run();
                },
                () -> {
                    closeTaskRenameEditor(state);
                    refresh.run();
                },
                () -> {
                    if (state.questDetails.questDetailsTaskRenameOpen) {
                        commitTaskRename(player, state);
                        refresh.run();
                    }
                },
                null
        );
        field.setClientSideWidget();
        field.setCurrentString(state.questDetails.questDetailsTaskRenameDraft);
        field.setMaxStringLength(80);
        field.setBordered(false);
        field.setTextColor(TabletColors.TEXT_PRIMARY);
        field.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.INTERACTIVE));
        parent.addWidget(field);
        int doneX = x + fieldW + 3;
        parent.addWidget(new ImageWidget(doneX, y + 2, TabletUiFactory.ACTION_ICON_SIZE, TabletUiFactory.ACTION_ICON_SIZE, IconAtlas.iconTexture("add.png")));
        parent.addWidget(flatHitButton(doneX, y, doneW, 16, click -> {
            commitTaskRename(player, state);
            refresh.run();
        }));
        if (state.questDetails.questDetailsTaskRenameFocusPending) {
            state.questDetails.questDetailsTaskRenameFocusPending = false;
            field.requestFocusWhenReady();
        }
    }

    static void renderAmountField(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsTaskEntry entry, int x, int y, int w, boolean task) {
        int count = task ? Math.max(0, entry.tag().getInt("count")) : 0;
        int amount = QuestTaskDisplayText.amount(entry.json());
        if (task && QuestTaskDisplayText.isManualTask(entry.json())) {
            QuestTaskActionWidgets.renderManualDoneButton(parent, player, refresh, questId, entry, x - 16, y, w + 16, count > 0);
            return;
        }
        if (!QuestTaskDisplayText.usesAmountField(entry.json(), task)) {
            return;
        }
        if (task && QuestTaskDisplayText.isManualXpTask(entry.json()) && !QuestDetailsEditController.canEdit(state)) {
            QuestTaskActionWidgets.renderManualXpButton(parent, player, refresh, questId, entry, x, y, w, count, amount);
            return;
        }
        if (!QuestDetailsEditController.canEdit(state)) {
            int chipX = x - (task ? 24 : 0);
            int chipW = w + (task ? 24 : 0);
            renderAmountText(parent, chipX, y, chipW, task ? count + " / " + amount : "x" + amount);
            return;
        }
        if (task) {
            String progress = count + " /";
            renderDisplayText(parent, x - 42, y, 36, progress, TabletColors.TEXT_MUTED, TextTexture.TextType.RIGHT_HIDE);
        }
        final TextFieldWidget[] fieldRef = new TextFieldWidget[1];
        TextFieldWidget field = StyledTextFields.numberField(
                x,
                y,
                w,
                14,
                amount,
                1,
                99999,
                5,
                value -> {
                },
                () -> {
                    commitAmount(player, questId, entry, task, amount, fieldRef[0] == null ? "" : fieldRef[0].getRawCurrentString());
                    refresh.run();
                },
                () -> {
                },
                () -> {
                    commitAmount(player, questId, entry, task, amount, fieldRef[0] == null ? "" : fieldRef[0].getRawCurrentString());
                    refresh.run();
                }
        );
        fieldRef[0] = field;
        parent.addWidget(field);
    }

    static boolean isRenamingTask(TabletUiState state, String questId, String entryId, boolean task) {
        return state.questDetails.questDetailsTaskRenameOpen
                && state.questDetails.questDetailsTaskRenameTask == task
                && questId.equals(state.questDetails.questDetailsTaskRenameQuestId)
                && entryId.equals(state.questDetails.questDetailsTaskRenameId)
                && QuestDetailsEditController.canEdit(state);
    }

    static boolean handleRenameKey(Player player, TabletUiState state, int keyCode, boolean draftUnchanged) {
        if (!state.questDetails.questDetailsTaskRenameOpen || !QuestDetailsEditController.canEdit(state)) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            commitTaskRename(player, state);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeTaskRenameEditor(state);
            return true;
        }
        if (draftUnchanged && keyCode == GLFW.GLFW_KEY_BACKSPACE && !state.questDetails.questDetailsTaskRenameDraft.isEmpty()) {
            state.questDetails.questDetailsTaskRenameDraft = state.questDetails.questDetailsTaskRenameDraft.substring(0, state.questDetails.questDetailsTaskRenameDraft.length() - 1);
            return true;
        }
        return false;
    }

    static boolean handleRenameChar(TabletUiState state, char c, boolean draftUnchanged) {
        if (!draftUnchanged || !state.questDetails.questDetailsTaskRenameOpen || !QuestDetailsEditController.canEdit(state)) {
            return false;
        }
        if (Character.isISOControl(c)) {
            return false;
        }
        state.questDetails.questDetailsTaskRenameDraft = sanitizeTaskTitle(state.questDetails.questDetailsTaskRenameDraft + c);
        if (state.questDetails.questDetailsTaskRenameDraft.length() > 80) {
            state.questDetails.questDetailsTaskRenameDraft = state.questDetails.questDetailsTaskRenameDraft.substring(0, 80);
        }
        return true;
    }

    static void openTaskRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestTaskListInteractions.openRenameEditor(state, questId, id, task);
    }

    static void closeTaskRenameEditor(TabletUiState state) {
        QuestDetailsTransientManager.closeTaskRename(state);
    }

    private static void commitAmount(Player player, String questId, QuestDetailsTaskEntry entry, boolean task, int oldAmount, String rawValue) {
        int parsed = QuestTaskDisplayText.parsePositive(rawValue, oldAmount);
        if (parsed == oldAmount) {
            return;
        }
        JsonObject next = entry.json().deepCopy();
        next.addProperty("amount", parsed);
        if (task) {
            EditorQuestCommandClient.putQuestTaskJson(player, questId, next.toString());
        } else if (QuestTaskSelectableRewards.commitDisplayAmount(player, questId, entry.id(), parsed)) {
            return;
        } else {
            EditorQuestCommandClient.putQuestRewardJson(player, questId, next.toString());
        }
    }

    private static void commitTaskRename(Player player, TabletUiState state) {
        QuestTaskEditActions.putTaskTitle(
                player,
                state.questDetails.questDetailsTaskRenameQuestId,
                state.questDetails.questDetailsTaskRenameId,
                state.questDetails.questDetailsTaskRenameDraft,
                state.questDetails.questDetailsTaskRenameTask
        );
        closeTaskRenameEditor(state);
    }

    private static String sanitizeTaskTitle(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void renderAmountText(WidgetGroup parent, int x, int y, int maxW, String text) {
        renderDisplayText(parent, x, y, Math.max(12, maxW), text, TabletColors.TEXT_SECONDARY, TextTexture.TextType.RIGHT_HIDE);
    }

    static void renderDisplayText(WidgetGroup parent, int x, int y, int width, String text, int color, TextTexture.TextType type) {
        parent.addWidget(TabletTextTextures.literal(x, y, Math.max(1, width), 16, text, color, type));
    }
}
