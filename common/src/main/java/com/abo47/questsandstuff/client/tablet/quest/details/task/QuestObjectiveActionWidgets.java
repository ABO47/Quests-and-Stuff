package com.abo47.questsandstuff.client.tablet.quest.details.task;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.animation.ProgressAnimations;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.TabletTextTextures;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualTaskPacket;
import com.abo47.questsandstuff.network.quest.runtime.C2SManualXpSubmitPacket;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestObjectiveActionWidgets {
    private QuestObjectiveActionWidgets() {
    }

    static void renderManualDoneButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, boolean done) {
        int iconSize = 16;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        int iconY = y - 1;
        if (done) {
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", TabletColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", TabletColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualTaskPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TabletTranslationKeys.component(QuestTranslationKeys.MARK_REQUIREMENT_DONE)}));
    }

    static void renderManualXpButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, int count, int amount) {
        parent.addWidget(TabletTextTextures.literal(x - 46, y, 42, 16, count + " / " + amount, TabletColors.TEXT_PRIMARY, TextTexture.TextType.RIGHT_HIDE));
        boolean done = count >= amount;
        int iconSize = 16;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        int iconY = y - 1;
        if (done) {
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", TabletColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", TabletColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualXpSubmitPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TabletTranslationKeys.component(QuestTranslationKeys.SUBMIT_XP_REQUIREMENT)}));
    }

    static void renderProgress(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        int barW = w;
        float progressValue = Math.max(0.0f, Math.min(1.0f, quest.getFloat("progress")));
        boolean locallyClaimed = state != null && questId.equals(state.questDetails.questDetailsClaimedOverrideQuestId);
        boolean claimed = quest.getBoolean("claimed") || locallyClaimed;
        boolean claimable = quest.getBoolean("completed") && !claimed;
        section.addWidget(TabletUiFactory.panel(x, y, barW, h, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
        int fillColor = claimed ? TabletColors.TEXT_MUTED : (claimable ? TabletColors.WARNING : TabletColors.SUCCESS);
        String progressKey = ProgressAnimations.key("details", questId);
        ProgressTexture texture = new ProgressTexture(
                IGuiTexture.EMPTY,
                SurfaceFactory.fill(withAlpha(fillColor, claimed ? 95 : 180))
        ).setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        ProgressWidget progressFill = new ProgressWidget(() -> ProgressAnimations.value(progressKey, progressValue), x + 1, y + 1, Math.max(1, barW - 2), Math.max(1, h - 2), texture);
        progressFill.setClientSideWidget();
        section.addWidget(progressFill);
        String label = claimable
                ? TabletTranslationKeys.text(QuestTranslationKeys.CLAIM)
                : (claimed ? TabletTranslationKeys.text(QuestTranslationKeys.CLAIMED) : Math.round(progressValue * 100.0f) + "%");
        section.addWidget(TabletTextTextures.literal(x, y, barW, h, label, claimed ? TabletColors.TEXT_MUTED : TabletColors.TEXT_PRIMARY, TextTexture.TextType.HIDE));
        if (claimable) {
            var hit = TabletUiFactory.flatHitButton(x, y, barW, h, click -> {
                boolean hasSelectableReward = QuestObjectiveSelectableRewards.hasSelectableReward(quest);
                boolean canClaimNow = true;
                if (hasSelectableReward) {
                    canClaimNow = QuestObjectiveSelectableRewards.allSelectableRewardsSelected(quest, state)
                            && QuestObjectiveSelectableRewards.claimSelected(player, state, questId);
                } else {
                    QuestDetailsWindow.claimAll(player, questId);
                }
                if (canClaimNow) {
                    if (state != null) {
                        state.questDetails.questDetailsClaimedOverrideQuestId = questId;
                    }
                    ClientQuestStateFacade.setQuestClaimedLocal(questId, true);
                }
                refresh.run();
            });
            hit.setHoverTooltips(new Component[]{TabletTranslationKeys.component(QuestTranslationKeys.CLAIM_ALL_REWARDS)});
            hit.setHoverTexture(SurfaceFactory.bordered(withAlpha(TabletColors.SUCCESS, 45), TabletColors.BORDER_ACCENT));
            hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.SUCCESS, 80)));
            section.addWidget(hit);
        }
    }
}
