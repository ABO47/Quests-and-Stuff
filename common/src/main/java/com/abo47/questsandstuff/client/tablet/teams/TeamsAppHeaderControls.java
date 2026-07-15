package com.abo47.questsandstuff.client.tablet.teams;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.refreshActiveTablet;

final class TeamsAppHeaderControls {
    private static final int TOOL_SIZE = HEADER_H;
    private static final int HEADER_GAP = 4;
    private static final int HEADER_INSET = GRID_9;

    private final TextFieldWidget searchField;
    private final ButtonWidget leaveBtn;
    private final ButtonWidget joinBtn;
    private final ButtonWidget inviteBtn;

    private TeamsAppHeaderControls(
            TextFieldWidget searchField,
            ButtonWidget leaveBtn,
            ButtonWidget joinBtn,
            ButtonWidget inviteBtn
    ) {
        this.searchField = searchField;
        this.leaveBtn = leaveBtn;
        this.joinBtn = joinBtn;
        this.inviteBtn = inviteBtn;
    }

    static TeamsAppHeaderControls create(TabletUiState state, Runnable refresh, int headerY, int bodyW) {
        int searchStartW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * 3);

        TextFieldWidget searchField = StyledTextFields.search(
                HEADER_INSET, headerY, searchStartW, TOOL_SIZE,
                () -> state.teams.search, Integer.MAX_VALUE,
                value -> {
                    state.teams.search = SearchFilter.normalizeUserInput(value);
                    refreshActiveTablet();
                },
                focused -> {}
        );

        int btnAreaStartX = HEADER_INSET + searchStartW;
        int leaveBtnX = btnAreaStartX + HEADER_GAP;
        int joinBtnX = leaveBtnX + TOOL_SIZE + HEADER_GAP;
        int inviteBtnX = joinBtnX + TOOL_SIZE + HEADER_GAP;

        int leaveRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_WARNING);
        int joinRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS);
        int shareRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE);

        ButtonWidget leaveBtn = headerIconButton(leaveBtnX, headerY, "door-open", leaveRoleColor,
                click -> {
                    ClientTeamCache.ClientTeamInfo teamInfo = ClientTeamCache.INSTANCE.getTeam();
                    if (teamInfo != null && teamInfo.isOwner(Minecraft.getInstance().player.getUUID())) {
                        return;
                    }
                    state.teams.confirmTitleKey = "ui.questsandstuff.teams.confirm_leave_title";
                    state.teams.confirmMessageKey = "ui.questsandstuff.teams.confirm_leave_message";
                    state.teams.confirmAction = "LEAVE";
                    state.teams.confirmTargetUuid = "";
                    state.teams.confirmAnimationStartMs = System.currentTimeMillis();
                    state.teams.confirmModalOpen = true;
                    refresh.run();
                });
        leaveBtn.setHoverTooltips(Component.translatable("ui.questsandstuff.teams.leave"));

        ButtonWidget joinBtn = headerIconButton(joinBtnX, headerY, "mail-open", joinRoleColor,
                click -> {
                    state.teams.inviteCodeImportMode = true;
                    state.teams.inviteCodeDraft = "";
                    state.teams.inviteCodeMessage = "";
                    state.teams.inviteCodeAnimationStartMs = System.currentTimeMillis();
                    state.teams.inviteCodeModalOpen = true;
                    refresh.run();
                });
        joinBtn.setHoverTooltips(Component.translatable("ui.questsandstuff.teams.join_team"));

        ButtonWidget inviteBtn = headerIconButton(inviteBtnX, headerY, "mail", shareRoleColor,
                click -> {
                    state.teams.inviteCodeImportMode = false;
                    state.teams.inviteCodeAnimationStartMs = System.currentTimeMillis();
                    state.teams.inviteCodeDraft = "";
                    state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.teams.generating_code");
                    state.teams.inviteCodeMessageSuccess = true;
                    state.teams.inviteCodeModalOpen = true;
                    refresh.run();
                    com.abo47.questsandstuff.network.ModNetwork.sendToServer(
                            new com.abo47.questsandstuff.network.team.C2STeamInviteCodePacket());
                });
        inviteBtn.setHoverTooltips(Component.translatable("ui.questsandstuff.teams.share_invite"));

        return new TeamsAppHeaderControls(searchField, leaveBtn, joinBtn, inviteBtn);
    }

    TextFieldWidget searchField() {
        return searchField;
    }

    ButtonWidget leaveBtn() {
        return leaveBtn;
    }

    ButtonWidget joinBtn() {
        return joinBtn;
    }

    ButtonWidget inviteBtn() {
        return inviteBtn;
    }

    void layout(int headerY, int bodyW) {
        int searchW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * 3);
        searchField.setSize(searchW, TOOL_SIZE);

        int baX = HEADER_INSET + searchW;
        leaveBtn.setSelfPosition(baX + HEADER_GAP, headerY);
        joinBtn.setSelfPosition(baX + HEADER_GAP + TOOL_SIZE + HEADER_GAP, headerY);
        inviteBtn.setSelfPosition(baX + HEADER_GAP + TOOL_SIZE * 2 + HEADER_GAP * 2, headerY);
    }

    void addTo(WidgetGroup mainPanel) {
        mainPanel.addWidget(searchField);
        mainPanel.addWidget(leaveBtn);
        mainPanel.addWidget(joinBtn);
        mainPanel.addWidget(inviteBtn);
    }

    private static ButtonWidget headerIconButton(int x, int y, String icon, int color,
                                                  java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TabletIconTextButton.Visuals visuals = new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, color),
                TabletIconTextButton.State.of(withAlpha(color, 66), TabletColors.BORDER_ACCENT, color),
                TabletIconTextButton.State.of(withAlpha(color, 90), color, TabletColors.TEXT_PRIMARY)
        );
        return TabletIconTextButton.icon(x, y, TOOL_SIZE, TOOL_SIZE, icon, visuals, callback);
    }
}
