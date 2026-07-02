package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamActionPacket;
import com.abo47.questsandstuff.network.team.C2STeamCreatePacket;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.UUID;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

final class TeamsConfirmModal {
    private static final int PAD = 8;
    private static final int BUTTON_W = 82;
    private static final int BUTTON_H = 16;
    private static final int BUTTON_Y = 56;

    private TeamsConfirmModal() {
    }

    static void rebuild(WidgetGroup layer, TabletUiState state, Runnable refresh, int layerW, int layerH) {
        if (!state.teams.confirmModalOpen) {
            return;
        }
        if (layerW <= 0) {
            layerW = state.root.tabletRootWidth > 0 ? state.root.tabletRootWidth : ROOT_W;
        }
        if (layerH <= 0) {
            layerH = state.root.tabletRootHeight > 0 ? state.root.tabletRootHeight : ROOT_H;
        }

        layer.addWidget(flatHitButton(0, 0, layerW, layerH, click -> {
            state.teams.confirmModalOpen = false;
            refresh.run();
        }));

        int w = 200;
        int h = 80;

        int px = Math.max(8, (layerW - w) / 2);
        int py = Math.max(24, (layerH - h) / 2);

        WidgetGroup panel = Surfaces.panel(px, py, w, h,
                withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT);

        panel.addWidget(label(PAD, 10,
                I18n.get(state.teams.confirmTitleKey),
                ModColors.TEXT_PRIMARY));

        panel.addWidget(label(PAD, 28,
                I18n.get(state.teams.confirmMessageKey),
                ModColors.TEXT_MUTED));

        ActionButtons.iconAction(panel, PAD, BUTTON_Y, BUTTON_W, BUTTON_H,
                "close", TabletVocabulary.text(TabletVocabulary.COMMON_CANCEL), ModColors.ERROR, null, click -> {
            state.teams.confirmModalOpen = false;
            refresh.run();
        });

        ActionButtons.iconAction(panel, w - PAD - BUTTON_W, BUTTON_Y, BUTTON_W, BUTTON_H,
                "manual_check", TabletVocabulary.text(TabletVocabulary.COMMON_CONFIRM), ModColors.SUCCESS, null, click -> {
            state.teams.confirmModalOpen = false;
            UUID localUuid = Minecraft.getInstance().player == null
                    ? UUID.randomUUID() : Minecraft.getInstance().player.getUUID();
            switch (state.teams.confirmAction) {
                case "LEAVE":
                    ClientTeamCache.INSTANCE.clear();
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.LEAVE, localUuid));
                    ModNetwork.sendToServer(new C2STeamCreatePacket());
                    break;
                case "KICK":
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.KICK,
                            UUID.fromString(state.teams.confirmTargetUuid)));
                    break;
                case "TRANSFER":
                    ModNetwork.sendToServer(new C2STeamActionPacket(
                            C2STeamActionPacket.Action.TRANSFER,
                            UUID.fromString(state.teams.confirmTargetUuid)));
                    break;
            }
            refresh.run();
        });

        layer.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel,
                () -> state.teams.confirmAnimationStartMs, () -> true, () -> null)
                : panel);
    }
}
