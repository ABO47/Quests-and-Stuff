package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.tablet.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestObjectiveCardRenderer {
    private QuestObjectiveCardRenderer() {
    }

    static void renderTaskCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, List<QuestDetailsObjectiveEntry> entries, int listY, int listBottom) {
        parent.addWidget(cardPanel(state, player, refresh, questId, entries, "requirements", entry.id(), x, y, w, entry.tag().getFloat("progress"), listY, listBottom));
        String icon = QuestObjectiveDisplayText.taskIcon(entry.json());
        addObjectiveIcon(parent, icon, x + 8, y + 8);
        addIconChangeHit(parent, state, refresh, questId, entry.id(), true, x + 8, y + 8);
        boolean renaming = QuestObjectiveInlineFields.isRenamingObjective(state, questId, entry.id(), true);
        QuestObjectiveInlineFields.renderObjectiveTitle(parent, state, player, refresh, questId, entry, true, x + 30, y + 8, renaming ? x + w - 8 : x + w - 38);
        if (!renaming) {
            QuestObjectiveInlineFields.renderAmountField(parent, state, player, refresh, questId, entry, x + w - 34, y + 9, 30, true);
        }
    }

    static void renderRewardCard(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, List<QuestDetailsObjectiveEntry> entries, int listY, int listBottom) {
        parent.addWidget(cardPanel(state, player, refresh, questId, entries, "rewards", entry.id(), x, y, w, 0.0f, listY, listBottom));
        String icon = QuestObjectiveDisplayText.rewardIcon(entry.json());
        addObjectiveIcon(parent, icon, x + 8, y + 8);
        addIconChangeHit(parent, state, refresh, questId, entry.id(), false, x + 8, y + 8);
        boolean renaming = QuestObjectiveInlineFields.isRenamingObjective(state, questId, entry.id(), false);
        if (!renaming && QuestObjectiveLootTableRewardEditor.isLootTable(entry.json())) {
            QuestObjectiveLootTableRewardEditor.render(parent, state, player, refresh, questId, entry, x + 30, y + 8, x + w - 38);
        } else {
            QuestObjectiveInlineFields.renderObjectiveTitle(parent, state, player, refresh, questId, entry, false, x + 30, y + 8, renaming ? x + w - 8 : x + w - 38);
        }
        if (!renaming) {
            QuestObjectiveInlineFields.renderAmountField(parent, state, player, refresh, questId, entry, x + w - 34, y + 9, 30, false);
        }
    }

    private static void addObjectiveIcon(WidgetGroup parent, String icon, int x, int y) {
        parent.addWidget(new DisplayIconWidget(x, y, QuestDetailsObjectivesPanel.ICON, QuestDetailsObjectivesPanel.ICON, icon));
    }

    private static void addIconChangeHit(WidgetGroup parent, TabletUiState state, Runnable refresh, String questId, String id, boolean task, int x, int y) {
        if (!state.canEdit || !state.questDetailsEditMode) {
            return;
        }
        EntityIconControls.addChangeIconHit(parent, state, refresh, x, y, QuestDetailsObjectivesPanel.ICON, () -> {
            QuestDetailsWindow.openIconPicker(state, task ? ModalTargets.taskIcon(questId, id) : ModalTargets.rewardIcon(questId, id));
        });
    }

    private static WidgetGroup cardPanel(TabletUiState state, Player player, Runnable refresh, String questId, List<QuestDetailsObjectiveEntry> entries, String kind, String id, int x, int y, int w, float progress, int listY, int listBottom) {
        WidgetGroup card = new WidgetGroup(x, y, w, QuestDetailsObjectivesPanel.CARD_H) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0 && isMouseOverElement(mouseX, mouseY) && state.canEdit && state.questDetailsEditMode) {
                    QuestObjectiveListInteractions.selectAndBeginDrag(state, kind, id, mouseX, mouseY);
                    refresh.run();
                    return true;
                }
                if (button == 1 && isMouseOverElement(mouseX, mouseY) && state.canEdit && state.questDetailsEditMode) {
                    int lx = QuestDetailsMouse.localCoord(mouseX, getPositionX(), w);
                    int ly = QuestDetailsMouse.localCoord(mouseY, getPositionY(), QuestDetailsObjectivesPanel.CARD_H);
                    QuestObjectiveListInteractions.select(state, kind, id);
                    QuestDetailsMouse.openContextAtPointer(state, kind, id, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
                    refresh.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                int localY = listY + y + QuestDetailsMouse.localCoord(mouseY, getPositionY(), QuestDetailsObjectivesPanel.CARD_H);
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
        boolean selected = kind.startsWith(state.questDetailsSelectedObjectiveKind) && id.equals(state.questDetailsSelectedObjectiveId);
        card.setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, selected ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE));
        int fillW = Math.round((w - 2) * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            WidgetGroup fill = new WidgetGroup(1, 1, Math.max(1, fillW), QuestDetailsObjectivesPanel.CARD_H - 2);
            fill.setBackground(Surfaces.fill(withAlpha(ModColors.SUCCESS, 80)));
            card.addWidget(fill);
        }
        return card;
    }
}
