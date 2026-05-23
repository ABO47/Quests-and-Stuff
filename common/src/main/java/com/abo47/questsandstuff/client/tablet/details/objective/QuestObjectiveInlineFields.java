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
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;

final class QuestObjectiveInlineFields {
    private static final String TITLE_ELLIPSIS = "..";

    private QuestObjectiveInlineFields() {
    }

    static void renderObjectiveTitle(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, boolean task, int x, int y, int rightX) {
        renderObjectiveTitle(parent, state, player, refresh, questId, entry, task, x, y, rightX, ModColors.TEXT_PRIMARY);
    }

    static void renderObjectiveTitle(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, boolean task, int x, int y, int rightX, int color) {
        boolean renaming = isRenamingObjective(state, questId, entry.id(), task);
        int doneW = renaming ? 12 : 0;
        int fieldW = Math.max(18, rightX - x - doneW - (renaming ? 3 : 0));
        if (!renaming) {
            parent.addWidget(label(x, y + 3, fitText(QuestObjectiveDisplayText.displayName(entry.json(), entry.type()), fieldW), color));
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
        if (task && QuestObjectiveDisplayText.isManualXpTask(entry.json()) && (!state.canEdit || !state.questDetailsEditMode)) {
            QuestObjectiveActionWidgets.renderManualXpButton(parent, player, refresh, questId, entry, x, y, w, count, amount);
            return;
        }
        if (!state.canEdit || !state.questDetailsEditMode) {
            int chipX = x - (task ? 24 : 0);
            int chipW = w + (task ? 24 : 0);
            renderAmountChip(parent, chipX, y, chipW, task ? count + " / " + amount : "x" + amount);
            return;
        }
        if (task) {
            String progress = count + " /";
            int progressW = Minecraft.getInstance().font.width(progress);
            parent.addWidget(label(x - progressW - 6, y + 3, progress, ModColors.TEXT_MUTED));
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
        } else if (QuestObjectiveSelectableRewards.commitDisplayAmount(player, questId, entry.id(), parsed)) {
            return;
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

    private static void renderAmountChip(WidgetGroup parent, int x, int y, int maxW, String text) {
        int width = Math.max(24, Math.min(maxW, Minecraft.getInstance().font.width(text) + 10));
        int chipX = x + maxW - width;
        WidgetGroup chip = new WidgetGroup(chipX, y, width, 14);
        chip.setBackground(Surfaces.bordered(TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 190), ModColors.subtleBorder()));
        parent.addWidget(chip);
        String fitted = fitText(text, Math.max(12, width - 6));
        int textW = Minecraft.getInstance().font.width(fitted);
        parent.addWidget(label(chipX + Math.max(3, (width - textW) / 2), y + 3, fitted, ModColors.TEXT_PRIMARY));
    }

    static String fitText(String value, int width) {
        String text = value == null ? "" : value;
        int available = Math.max(12, width);
        if (Minecraft.getInstance().font.width(text) <= available) {
            return text;
        }
        return Minecraft.getInstance().font.plainSubstrByWidth(text, Math.max(1, available - Minecraft.getInstance().font.width(TITLE_ELLIPSIS))) + TITLE_ELLIPSIS;
    }
}
