package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.runtime.C2SManualXpSubmitPacket;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestObjectiveActionWidgets {
    private QuestObjectiveActionWidgets() {
    }

    static void renderManualDoneButton(WidgetGroup parent, Player player, Runnable refresh, QuestDetailsObjectiveEntry entry, int x, int y, int w, boolean done) {
        String label = QuestVocabulary.text(QuestVocabulary.COMMON_OK);
        int fill = done ? TabletUiFactory.withAlpha(ModColors.SUCCESS, 80) : TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 120);
        int border = done ? ModColors.SUCCESS : ModColors.BORDER_BASE;
        parent.addWidget(TabletUiFactory.panel(x, y, w, 14, fill, border));
        parent.addWidget(TabletUiFactory.label(x + Math.max(2, (w - Minecraft.getInstance().font.width(label)) / 2), y + 3, label, done ? ModColors.TEXT_MUTED : ModColors.TEXT_PRIMARY));
        if (done) {
            return;
        }
        var hit = TabletUiFactory.flatHitButton(x, y, w, 14, click -> {
            String taskKey = QuestObjectiveDisplayText.manualTarget(entry.json(), entry.id());
            QuestNetwork.sendToServer(new C2SManualTaskPacket(taskKey));
            refresh.run();
        });
        hit.setHoverTooltips(new Component[]{QuestVocabulary.component(QuestVocabulary.MARK_REQUIREMENT_DONE)});
        hit.setHoverTexture(Surfaces.bordered(TabletUiFactory.withAlpha(ModColors.SUCCESS, 45), ModColors.BORDER_ACCENT));
        parent.addWidget(hit);
    }

    static void renderManualXpButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, int count, int amount) {
        parent.addWidget(TabletUiFactory.label(x - 42, y + 3, count + " / " + amount, ModColors.TEXT_PRIMARY));
        boolean done = count >= amount;
        int iconSize = 12;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        parent.addWidget(new ImageWidget(iconX, y + 1, iconSize, iconSize, UiIconAtlas.iconTexture("send-horizontal")));
        if (done) {
            return;
        }
        var hit = TabletUiFactory.flatHitButton(iconX, y + 1, iconSize, iconSize, click -> {
            QuestNetwork.sendToServer(new C2SManualXpSubmitPacket(questId, entry.id()));
            refresh.run();
        });
        hit.setHoverTooltips(new Component[]{QuestVocabulary.component(QuestVocabulary.SUBMIT_XP_REQUIREMENT)});
        hit.setHoverTexture(Surfaces.bordered(TabletUiFactory.withAlpha(ModColors.SUCCESS, 45), ModColors.BORDER_ACCENT));
        parent.addWidget(hit);
    }

    static void renderProgress(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        int barW = w;
        float progressValue = Math.max(0.0f, Math.min(1.0f, quest.getFloat("progress")));
        int progress = Math.max(0, Math.min(barW, Math.round(barW * progressValue)));
        boolean locallyClaimed = state != null && questId.equals(state.questDetailsClaimedOverrideQuestId);
        boolean claimed = quest.getBoolean("claimed") || locallyClaimed;
        boolean claimable = quest.getBoolean("completed") && !claimed;
        section.addWidget(TabletUiFactory.panel(x, y, barW, h, ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        if (progress > 0) {
            WidgetGroup fill = new WidgetGroup(x + 1, y + 1, Math.max(1, progress - 2), Math.max(1, h - 2));
            int fillColor = claimed ? ModColors.TEXT_MUTED : (claimable ? ModColors.WARNING : ModColors.SUCCESS);
            fill.setBackground(Surfaces.fill(TabletUiFactory.withAlpha(fillColor, claimed ? 95 : 180)));
            section.addWidget(fill);
        }
        String label = claimable
                ? QuestVocabulary.text(QuestVocabulary.CLAIM)
                : (claimed ? QuestVocabulary.text(QuestVocabulary.CLAIMED) : Math.round(progressValue * 100.0f) + "%");
        int labelW = Minecraft.getInstance().font.width(label);
        LabelWidget progressLabel = new LabelWidget(x + Math.max(2, (barW - labelW) / 2), y + 3, Component.literal(label));
        progressLabel.setColor(claimed ? ModColors.TEXT_MUTED : ModColors.TEXT_PRIMARY);
        section.addWidget(progressLabel);
        if (claimable) {
            var hit = TabletUiFactory.flatHitButton(x, y, barW, h, click -> {
                boolean hasSelectableReward = QuestObjectiveSelectableRewards.hasSelectableReward(quest);
                QuestDetailsWindow.claimAll(player, questId);
                boolean claimedSelectable = QuestObjectiveSelectableRewards.claimSelected(player, state, questId);
                boolean selectableSelectionsComplete = QuestObjectiveSelectableRewards.allSelectableRewardsSelected(quest, state);
                if (!hasSelectableReward || (claimedSelectable && selectableSelectionsComplete)) {
                    if (state != null) {
                        state.questDetailsClaimedOverrideQuestId = questId;
                    }
                    ClientQuestCache.setQuestClaimedLocal(questId, true);
                }
                refresh.run();
            });
            hit.setHoverTooltips(new Component[]{QuestVocabulary.component(QuestVocabulary.CLAIM_ALL_REWARDS)});
            hit.setHoverTexture(Surfaces.bordered(TabletUiFactory.withAlpha(ModColors.SUCCESS, 45), ModColors.BORDER_ACCENT));
            hit.setClickedTexture(Surfaces.fill(TabletUiFactory.withAlpha(ModColors.SUCCESS, 80)));
            section.addWidget(hit);
        }
    }
}
