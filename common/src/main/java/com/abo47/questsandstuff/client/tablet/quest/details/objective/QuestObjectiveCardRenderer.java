package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.ItemStackIconCodec;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.modal.TabletModalPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class QuestObjectiveCardRenderer {
    private QuestObjectiveCardRenderer() {
    }

    static void renderTaskCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, List<QuestDetailsObjectiveEntry> entries, int listY, int listBottom) {
        parent.addWidget(cardPanel(state, player, refresh, questId, entries, "requirements", entry.id(), x, y, w, entry.tag().getFloat("progress"), listY, listBottom));
        String icon = QuestObjectiveDisplayText.taskIcon(entry.json());
        addObjectiveIcon(parent, entry.json(), icon, x + 8, y + 8);
        addIconHoverHit(parent, state, refresh, questId, entry.id(), true, entry.json(), icon, x + 8, y + 8);
        boolean renaming = QuestObjectiveInlineFields.isRenamingObjective(state, questId, entry.id(), true);
        QuestObjectiveInlineFields.renderObjectiveTitle(parent, state, player, refresh, questId, entry, true, x + 30, y + 8, renaming ? x + w - 8 : taskTitleRightX(entry, x, w));
        if (!renaming) {
            QuestObjectiveInlineFields.renderAmountField(parent, state, player, refresh, questId, entry, x + w - 34, y + 9, 30, true);
        }
    }

    static void renderRewardCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, List<QuestDetailsObjectiveEntry> entries, int listY, int listBottom, boolean rewardsClaimed) {
        boolean selectableWrapper = QuestObjectiveSelectableRewards.isSelectable(entry.json());
        boolean selectableChoice = QuestObjectiveSelectableRewards.isSelectableChoiceId(entry.id());
        boolean selectableReward = selectableWrapper || selectableChoice;
        boolean claimChoiceEntry = QuestObjectiveSelectableRewards.isClaimChoiceEntry(entry);
        JsonObject displayJson = selectableWrapper ? QuestObjectiveSelectableRewards.displayJson(entry.json()) : entry.json();
        QuestDetailsObjectiveEntry displayEntry = selectableWrapper ? new QuestDetailsObjectiveEntry(entry.id(), entry.tag(), displayJson) : entry;
        parent.addWidget(cardPanel(state, player, refresh, questId, entries, "rewards", entry.id(), x, y, w, 0.0f, listY, listBottom, selectableReward, rewardsClaimed, claimChoiceEntry));
        String icon = QuestObjectiveDisplayText.rewardIcon(displayJson);
        addObjectiveIcon(parent, displayJson, icon, x + 8, y + 8);
        addIconHoverHit(parent, state, refresh, questId, entry.id(), false, displayJson, icon, x + 8, y + 8);
        boolean renaming = QuestObjectiveInlineFields.isRenamingObjective(state, questId, entry.id(), false);
        int titleX = x + 30;
        if (!renaming && !selectableReward && QuestObjectiveLootTableRewardEditor.isLootTable(entry.json())) {
            QuestObjectiveLootTableRewardEditor.render(parent, state, player, refresh, questId, entry, titleX, y + 8, rewardTitleRightX(entry, x, w));
        } else {
            QuestObjectiveInlineFields.renderObjectiveTitle(parent, state, player, refresh, questId, displayEntry, false, titleX, y + 8, renaming ? x + w - 8 : rewardTitleRightX(displayEntry, x, w), rewardsClaimed ? ModColors.TEXT_MUTED : ModColors.TEXT_PRIMARY);
        }
        if (!renaming) {
            QuestObjectiveInlineFields.renderAmountField(parent, state, player, refresh, questId, displayEntry, x + w - 34, y + 9, 30, false);
        }
    }

    private static void addObjectiveIcon(WidgetGroup parent, JsonObject json, String icon, int x, int y) {
        ItemStack stack = QuestObjectiveItemStacks.iconStack(json);
        if (!stack.isEmpty()) {
            parent.addWidget(new DisplayIconWidget(x, y, QuestDetailsObjectivesPanel.ICON, QuestDetailsObjectivesPanel.ICON, stack));
            return;
        }
        parent.addWidget(new DisplayIconWidget(x, y, QuestDetailsObjectivesPanel.ICON, QuestDetailsObjectivesPanel.ICON, icon));
    }

    private static int taskTitleRightX(QuestDetailsObjectiveEntry entry, int x, int w) {
        if (!QuestObjectiveDisplayText.usesAmountField(entry.json(), true)) {
            return x + w - 8;
        }
        if (QuestObjectiveDisplayText.isManualTask(entry.json())) {
            return x + w - 58;
        }
        int count = Math.max(0, entry.tag().getInt("count"));
        int countTextW = Minecraft.getInstance().font.width(count + " /");
        return x + w - 34 - countTextW - 6;
    }

    private static int rewardTitleRightX(QuestDetailsObjectiveEntry entry, int x, int w) {
        return QuestObjectiveDisplayText.usesAmountField(entry.json(), false) ? x + w - 40 : x + w - 8;
    }

    private static void addIconHoverHit(WidgetGroup parent, TabletUiState state, Runnable refresh, String questId, String id, boolean task, JsonObject json, String icon, int x, int y) {
        if (!QuestDetailsEditState.canEdit(state)) {
            return;
        }
        var hit = flatHitButton(x, y, QuestDetailsObjectivesPanel.ICON, QuestDetailsObjectivesPanel.ICON, click -> {
            ContextMenuState.clearDeleteConfirm(state);
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, id) : ModalTargets.rewardIcon(questId, id));
            refresh.run();
        });
        hit.setHoverTexture(EntityIconControls.iconHoverTexture());
        hit.setHoverTooltips(iconTooltip(json, icon));
        parent.addWidget(hit);
    }

    private static WidgetGroup cardPanel(TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, String id, int x, int y, int w, float progress, int listY, int listBottom) {
        return cardPanel(state, player, refresh, questId, entries, kind, id, x, y, w, progress, listY, listBottom, false);
    }

    private static WidgetGroup cardPanel(TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, String id, int x, int y, int w, float progress, int listY, int listBottom, boolean selectableReward) {
        return cardPanel(state, player, refresh, questId, entries, kind, id, x, y, w, progress, listY, listBottom, selectableReward, false, selectableReward);
    }

    private static WidgetGroup cardPanel(TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, String id, int x, int y, int w, float progress, int listY, int listBottom, boolean selectableReward, boolean claimedReward, boolean claimChoiceEntry) {
        WidgetGroup card = new WidgetGroup(x, y, w, QuestDetailsObjectivesPanel.CARD_H) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean editMode = QuestDetailsEditState.canEdit(state);
                if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
                    boolean claimChoice = claimChoiceEntry && !claimedReward;
                    if (claimChoice) {
                        QuestObjectiveSelectableRewards.selectChoice(state, id);
                    }
                    if (editMode) {
                        QuestObjectiveListInteractions.selectAndBeginDrag(state, kind, id, mouseX, mouseY);
                        refresh.run();
                        return true;
                    }
                    if (claimChoice) {
                        refresh.run();
                        return true;
                    }
                }
                if (button == 1 && isMouseOverElement(mouseX, mouseY) && editMode) {
                    int lx = QuestDetailsMouse.localX(state, mouseX, getPositionX(), w);
                    int ly = QuestDetailsMouse.localY(state, mouseY, getPositionY(), QuestDetailsObjectivesPanel.CARD_H);
                    QuestObjectiveListInteractions.select(state, kind, id);
                    QuestDetailsMouse.openContextAtPointer(state, kind, id, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
                    refresh.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                int localY = listY + y + QuestDetailsMouse.localY(state, mouseY, getPositionY(), QuestDetailsObjectivesPanel.CARD_H);
                if (QuestObjectiveListInteractions.handleDrag(player, state, refresh, questId, entries, kind, listY, listBottom, localY, mouseX, mouseY, button)) {
                    return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                if (QuestObjectiveListInteractions.handleRelease(player, state, refresh, questId, entries, kind)) {
                    return true;
                }
                return super.mouseReleased(mouseX, mouseY, button);
            }
        };
        boolean editSelected = kind.startsWith(state.questDetails.questDetailsSelectedObjectiveKind) && id.equals(state.questDetails.questDetailsSelectedObjectiveId);
        boolean claimSelected = claimChoiceEntry && QuestObjectiveSelectableRewards.isSelectedChoice(state, id);
        boolean selected = editSelected || claimSelected;
        int accent = selectableReward ? (claimSelected ? ModColors.SUCCESS : ModColors.WARNING) : ModColors.INTERACTIVE;
        card.setBackground(Surfaces.card(selected || selectableReward, accent, claimedReward));
        int fillW = Math.round((w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            WidgetGroup fill = new WidgetGroup(1, 1, Math.max(1, fillW), QuestDetailsObjectivesPanel.CARD_H - 2);
            fill.setBackground(Surfaces.fill(withAlpha(ModColors.SUCCESS, 80)));
            card.addWidget(fill);
        }
        return card;
    }

    private static Component[] iconTooltip(JsonObject json, String icon) {
        ItemStack stack = QuestObjectiveItemStacks.iconStack(json);
        if (stack.isEmpty()) {
            return TabletModalPanel.iconTooltip(icon);
        }
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        lines.add(Component.literal(itemId).withStyle(ChatFormatting.DARK_GRAY));
        String summary = ItemStackIconCodec.nbtSummary(stack);
        if (!summary.isBlank()) {
            lines.add(Component.literal("NBT: " + summary).withStyle(ChatFormatting.GOLD));
        }
        return lines.toArray(Component[]::new);
    }
}
