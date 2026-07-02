package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.WindowChrome;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamInviteCodePacket;
import com.abo47.questsandstuff.network.team.C2STeamJoinPacket;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

final class TeamsInviteCodeModal {
    private static final int PAD = 8;
    private static final int BTN_SIZE = 18;
    private static final int GAP = 3;
    private static final int CODE_Y = 32;
    private static final int CODE_H = 44;

    private TeamsInviteCodeModal() {
    }

    static void rebuild(WidgetGroup modal, TabletUiState state, Runnable refresh, int layerW, int layerH) {
        if (!state.teams.inviteCodeModalOpen) {
            return;
        }

        if (layerW <= 0) {
            layerW = state.root.tabletRootWidth > 0 ? state.root.tabletRootWidth : ROOT_W;
        }
        if (layerH <= 0) {
            layerH = state.root.tabletRootHeight > 0 ? state.root.tabletRootHeight : ROOT_H;
        }

        modal.addWidget(flatHitButton(0, 0, layerW, layerH, click -> {
            state.teams.inviteCodeModalOpen = false;
            refresh.run();
        }));

        int panelW = Math.min(300, Math.max(200, layerW - 32));
        int panelH = 120;
        int px = Math.max(8, (layerW - panelW) / 2);
        int py = Math.max(24, (layerH - panelH) / 2);

        WidgetGroup panel = Surfaces.panel(px, py, panelW, panelH,
                withAlpha(ModColors.elevatedSurface(), 245), ModColors.BORDER_ACCENT);

        panel.addWidget(label(PAD, 6,
                I18n.get(state.teams.inviteCodeImportMode
                        ? "ui.questsandstuff.teams.enter_invite_code"
                        : "ui.questsandstuff.teams.share_invite"),
                ModColors.TEXT_PRIMARY));

        int closeX = panelW - PAD - BTN_SIZE;
        panel.addWidget(WindowChrome.closeIconButton(closeX, 4, BTN_SIZE, BTN_SIZE, click -> {
            state.teams.inviteCodeModalOpen = false;
            refresh.run();
        }));

        int x = closeX - GAP - BTN_SIZE;
        if (state.teams.inviteCodeImportMode) {
            panel.addWidget(WindowChrome.iconButton(x, 4, BTN_SIZE, BTN_SIZE,
                    "manual_check", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS),
                    click -> {
                        String code = state.teams.inviteCodeDraft == null ? "" : state.teams.inviteCodeDraft.trim().toUpperCase();
                        if (code.isBlank()) {
                            state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.teams.invite_code_empty");
                            state.teams.inviteCodeMessageSuccess = false;
                            refresh.run();
                            return;
                        }
                        state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.teams.joining");
                        state.teams.inviteCodeMessageSuccess = true;
                        ModNetwork.sendToServer(new C2STeamJoinPacket(code));
                        refresh.run();
                    }));
            x -= GAP + BTN_SIZE;
            panel.addWidget(WindowChrome.iconButton(x, 4, BTN_SIZE, BTN_SIZE,
                    "paste", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT),
                    click -> {
                        state.teams.inviteCodeDraft = Minecraft.getInstance().keyboardHandler.getClipboard();
                        state.teams.inviteCodeMessage = "";
                        refresh.run();
                    }));
        } else {
            ClientTeamCache.ClientTeamInfo teamInfo = ClientTeamCache.INSTANCE.getTeam();
            if (state.teams.inviteCodeDraft.isBlank()) {
                if (teamInfo != null && !teamInfo.inviteCode().isBlank()) {
                    state.teams.inviteCodeDraft = teamInfo.inviteCode();
                    state.teams.inviteCodeMessage = "";
                }
            } else if (teamInfo != null && !teamInfo.inviteCode().isBlank()
                    && !teamInfo.inviteCode().equals(state.teams.inviteCodeDraft)) {
                state.teams.inviteCodeDraft = teamInfo.inviteCode();
                state.teams.inviteCodeMessage = "";
            }
            int renewX = x - GAP - BTN_SIZE;
            panel.addWidget(WindowChrome.iconButton(renewX, 4, BTN_SIZE, BTN_SIZE,
                    "reset_quest", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_WARNING),
                    click -> {
                        state.teams.inviteCodeDraft = "";
                        state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.teams.generating_code");
                        state.teams.inviteCodeMessageSuccess = true;
                        ModNetwork.sendToServer(new C2STeamInviteCodePacket());
                        refresh.run();
                    }));
            panel.addWidget(WindowChrome.iconButton(x, 4, BTN_SIZE, BTN_SIZE,
                    "copy", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_DEFAULT),
                    click -> {
                        Minecraft.getInstance().keyboardHandler.setClipboard(
                                state.teams.inviteCodeDraft == null ? "" : state.teams.inviteCodeDraft);
                        state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.blueprints.code_copied");
                        refresh.run();
                    }));
        }

        com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget editor =
                new com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget(
                        PAD, CODE_Y, panelW - PAD * 2, CODE_H);
        editor.codeEditor.setLanguageDefinitionUnformatted();
        editor.setBackground(Surfaces.bordered(ModColors.recessedSurface(), ModColors.BORDER_BASE));
        String draft = state.teams.inviteCodeDraft == null ? "" : state.teams.inviteCodeDraft;
        editor.setLines(draft.isBlank() ? List.of("") : List.of(draft.split("\\R", -1)));
        editor.setFocus(true);
        panel.addWidget(editor);

        if (state.teams.inviteCodeMessage != null && !state.teams.inviteCodeMessage.isBlank()) {
            panel.addWidget(label(PAD, CODE_Y + CODE_H + 8,
                    state.teams.inviteCodeMessage,
                    state.teams.inviteCodeMessageSuccess ? ModColors.SUCCESS : ModColors.WARNING));
        }

        modal.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel,
                () -> state.teams.inviteCodeAnimationStartMs, () -> true, () -> null)
                : panel);
    }
}
