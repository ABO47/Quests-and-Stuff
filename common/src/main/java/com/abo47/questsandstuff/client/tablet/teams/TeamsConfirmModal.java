package com.abo47.questsandstuff.client.tablet.teams;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamActionPacket;
import com.abo47.questsandstuff.network.team.C2STeamCreatePacket;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

final class TeamsConfirmModal {
    private static final int PAD = 8;
    private static final int BUTTON_W = 82;
    private static final int BUTTON_H = BUTTON_16;
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

        int w = 200;
        int h = 80;

        int px = Math.max(8, (layerW - w) / 2);
        int py = Math.max(24, (layerH - h) / 2);

        WidgetGroup panel = SurfaceFactory.panel(px, py, w, h,
                withAlpha(TabletColors.SURFACE_BASE, 246), TabletColors.BORDER_ACCENT);

        panel.addWidget(label(PAD, 10,
                I18n.get(state.teams.confirmTitleKey),
                TabletColors.TEXT_PRIMARY));

        panel.addWidget(label(PAD, 28,
                I18n.get(state.teams.confirmMessageKey),
                TabletColors.TEXT_MUTED));

        ActionButtons.iconAction(panel, PAD, BUTTON_Y, BUTTON_W, BUTTON_H,
                "close", TabletTranslationKeys.text(TabletTranslationKeys.COMMON_CANCEL), TabletColors.ERROR, null, click -> {
            state.teams.confirmModalOpen = false;
            refresh.run();
        });

        ActionButtons.iconAction(panel, w - PAD - BUTTON_W, BUTTON_Y, BUTTON_W, BUTTON_H,
                "manual_check", TabletTranslationKeys.text(TabletTranslationKeys.COMMON_CONFIRM), TabletColors.SUCCESS, null, click -> {
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
