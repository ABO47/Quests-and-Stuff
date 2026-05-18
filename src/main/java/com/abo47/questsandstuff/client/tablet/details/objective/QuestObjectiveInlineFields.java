package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import static com.abo47.questsandstuff.client.tablet.controls.SearchFilter.crop;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

final class QuestObjectiveInlineFields {
    private QuestObjectiveInlineFields() {
    }

    static void renderObjectiveTitle(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, boolean task, int x, int y, int rightX) {
        boolean renaming = isRenamingObjective(state, questId, entry.id(), task);
        int doneW = renaming ? 12 : 0;
        int fieldW = Math.max(18, rightX - x - doneW - (renaming ? 3 : 0));
        if (!renaming) {
            int maxChars = Math.max(4, fieldW / 7);
            parent.addWidget(label(x, y + 3, crop(QuestObjectiveDisplayText.displayName(entry.json(), entry.type()), maxChars), ModColors.TEXT_PRIMARY));
            return;
        }
        InlineRenameField field = new InlineRenameField(
                x,
                y,
                fieldW,
                16,
                () -> state.questDetailsObjectiveRenameDraft,
                value -> state.questDetailsObjectiveRenameDraft = sanitizeObjectiveTitle(value),
                () -> {
                    commitObjectiveRename(player, state);
                    refresh.run();
                },
                () -> {
                    closeObjectiveRenameEditor(state);
                    refresh.run();
                },
                () -> {
                    if (state.questDetailsObjectiveRenameOpen) {
                        commitObjectiveRename(player, state);
                        refresh.run();
                    }
                },
                null
        );
        field.setClientSideWidget();
        field.setCurrentString(state.questDetailsObjectiveRenameDraft);
        field.setMaxStringLength(80);
        field.setBordered(false);
        field.setTextColor(ModColors.TEXT_PRIMARY);
        field.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.INTERACTIVE));
        parent.addWidget(field);
        int doneX = x + fieldW + 3;
        parent.addWidget(new ImageWidget(doneX, y + 2, TabletUiFactory.ACTION_ICON_SIZE, TabletUiFactory.ACTION_ICON_SIZE, UiIconAtlas.iconTexture("add.png")));
        parent.addWidget(flatHitButton(doneX, y, doneW, 16, click -> {
            commitObjectiveRename(player, state);
            refresh.run();
        }));
        field.setFocus(true);
    }

    static void renderAmountField(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, boolean task) {
        int count = task ? Math.max(0, entry.tag().getInt("count")) : 0;
        int amount = QuestObjectiveDisplayText.amount(entry.json());
        if (task && QuestObjectiveDisplayText.isManualTask(entry.json())) {
            QuestObjectiveActionWidgets.renderManualDoneButton(parent, player, refresh, entry, x - 16, y, w + 16, count > 0);
            return;
        }
        if (!QuestObjectiveDisplayText.usesAmountField(entry.json(), task)) {
            return;
        }
        if (!state.canEdit || !state.questDetailsEditMode) {
            parent.addWidget(label(x - (task ? 18 : 0), y + 3, task ? count + "/" + amount : Integer.toString(amount), ModColors.TEXT_PRIMARY));
            return;
        }
        if (task) {
            parent.addWidget(label(x - 20, y + 3, count + "/", ModColors.TEXT_MUTED));
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

    static boolean isRenamingObjective(TabletUiState state, String questId, String entryId, boolean task) {
        return state.questDetailsObjectiveRenameOpen
                && state.questDetailsObjectiveRenameTask == task
                && questId.equals(state.questDetailsObjectiveRenameQuestId)
                && entryId.equals(state.questDetailsObjectiveRenameId)
                && state.canEdit
                && state.questDetailsEditMode;
    }

    static boolean handleRenameKey(Player player, TabletUiState state, int keyCode) {
        if (!state.questDetailsObjectiveRenameOpen) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            commitObjectiveRename(player, state);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeObjectiveRenameEditor(state);
            return true;
        }
        return false;
    }

    static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestObjectiveListInteractions.openRenameEditor(state, questId, id, task);
    }

    static void closeObjectiveRenameEditor(TabletUiState state) {
        QuestDetailsTransientState.closeObjectiveRename(state);
    }

    private static void commitAmount(Player player, String questId, QuestDetailsObjectiveEntry entry, boolean task, int oldAmount, String rawValue) {
        int parsed = QuestObjectiveDisplayText.parsePositive(rawValue, oldAmount);
        if (parsed == oldAmount) {
            return;
        }
        JsonObject next = entry.json().deepCopy();
        next.addProperty("amount", parsed);
        if (task) {
            EditorCommandClient.putQuestTaskJson(player, questId, next.toString());
        } else {
            EditorCommandClient.putQuestRewardJson(player, questId, next.toString());
        }
    }

    private static void commitObjectiveRename(Player player, TabletUiState state) {
        QuestObjectiveEditActions.putObjectiveTitle(
                player,
                state.questDetailsObjectiveRenameQuestId,
                state.questDetailsObjectiveRenameId,
                state.questDetailsObjectiveRenameDraft,
                state.questDetailsObjectiveRenameTask
        );
        closeObjectiveRenameEditor(state);
    }

    private static String sanitizeObjectiveTitle(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }
}
