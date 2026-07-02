package com.abo47.questsandstuff.client.tablet.quest.details.objective;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.ProgressAnimations;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.TabletTextTextures;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
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
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", ModColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", ModColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualTaskPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TabletVocabulary.component(QuestVocabulary.MARK_REQUIREMENT_DONE)}));
    }

    static void renderManualXpButton(WidgetGroup parent, Player player, Runnable refresh, String questId, QuestDetailsObjectiveEntry entry, int x, int y, int w, int count, int amount) {
        parent.addWidget(TabletTextTextures.literal(x - 46, y, 42, 16, count + " / " + amount, ModColors.TEXT_PRIMARY, TextTexture.TextType.RIGHT_HIDE));
        boolean done = count >= amount;
        int iconSize = 16;
        int iconX = x + Math.max(0, (w - iconSize) / 2);
        int iconY = y - 1;
        if (done) {
            parent.addWidget(IconOnlyButton.icon(iconX, iconY, iconSize, "send-horizontal", ModColors.TEXT_MUTED));
            return;
        }
        var hit = IconOnlyButton.create(iconX, iconY, iconSize, "send-horizontal", ModColors.SUCCESS, click -> {
            ModNetwork.sendToServer(new C2SManualXpSubmitPacket(questId, entry.id()));
            refresh.run();
        });
        parent.addWidget(hit.tooltips(new Component[]{TabletVocabulary.component(QuestVocabulary.SUBMIT_XP_REQUIREMENT)}));
    }

    static void renderProgress(WidgetGroup section, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        int barW = w;
        float progressValue = Math.max(0.0f, Math.min(1.0f, quest.getFloat("progress")));
        boolean locallyClaimed = state != null && questId.equals(state.questDetails.questDetailsClaimedOverrideQuestId);
        boolean claimed = quest.getBoolean("claimed") || locallyClaimed;
        boolean claimable = quest.getBoolean("completed") && !claimed;
        section.addWidget(TabletUiFactory.panel(x, y, barW, h, ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        int fillColor = claimed ? ModColors.TEXT_MUTED : (claimable ? ModColors.WARNING : ModColors.SUCCESS);
        String progressKey = ProgressAnimations.key("details", questId);
        ProgressTexture texture = new ProgressTexture(
                IGuiTexture.EMPTY,
                Surfaces.fill(withAlpha(fillColor, claimed ? 95 : 180))
        ).setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        ProgressWidget progressFill = new ProgressWidget(() -> ProgressAnimations.value(progressKey, progressValue), x + 1, y + 1, Math.max(1, barW - 2), Math.max(1, h - 2), texture);
        progressFill.setClientSideWidget();
        section.addWidget(progressFill);
        String label = claimable
                ? TabletVocabulary.text(QuestVocabulary.CLAIM)
                : (claimed ? TabletVocabulary.text(QuestVocabulary.CLAIMED) : Math.round(progressValue * 100.0f) + "%");
        section.addWidget(TabletTextTextures.literal(x, y, barW, h, label, claimed ? ModColors.TEXT_MUTED : ModColors.TEXT_PRIMARY, TextTexture.TextType.HIDE));
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
                    ClientQuestCache.setQuestClaimedLocal(questId, true);
                }
                refresh.run();
            });
            hit.setHoverTooltips(new Component[]{TabletVocabulary.component(QuestVocabulary.CLAIM_ALL_REWARDS)});
            hit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.SUCCESS, 45), ModColors.BORDER_ACCENT));
            hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.SUCCESS, 80)));
            section.addWidget(hit);
        }
    }
}
