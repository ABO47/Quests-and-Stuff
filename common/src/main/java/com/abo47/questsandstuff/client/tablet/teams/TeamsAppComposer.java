package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.controls.ActionButtons;
import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWidget;
import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.shell.TabletShellBootstrap;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.WindowChrome;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.team.C2STeamActionPacket;
import com.abo47.questsandstuff.network.team.C2STeamCreatePacket;
import com.abo47.questsandstuff.network.team.C2STeamInviteCodePacket;
import com.abo47.questsandstuff.network.team.C2STeamJoinPacket;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.BODY_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_PAD_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ROOT_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.applyRootSize;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.refreshActiveTablet;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.setActiveTabletRefresh;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootHeight;
import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.rootWidth;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TeamsAppComposer {
    private static final int HOME_BTN_SIZE = 10;
    private static final int TOOL_SIZE = HEADER_H;
    private static final int HEADER_GAP = 4;
    private static final int CARD_H = 32;
    private static final int CARD_GAP = 8;
    private static final int CONTENT_INSET = 6;
    private static final int GUTTER = 6;
    private static final int HEADER_LIST_GAP = 5;

    private TeamsAppComposer() {
    }

    public static WidgetGroup create(Player player) {
        return create(player, ROOT_W, ROOT_H, false);
    }

    public static WidgetGroup create(Player player, int requestedRootW, int requestedRootH, boolean fullScreenMode) {
        TabletUiState state = TabletShellBootstrap.prepare(player);
        applyRootSize(state, requestedRootW, requestedRootH, fullScreenMode);

        int initialRootW = rootWidth(state);
        int initialRootH = rootHeight(state);

        TabletRootWidget root = new TabletRootWidget(0, 0, initialRootW, initialRootH, state);
        root.setBackground(fullScreenMode
                ? Surfaces.transparent()
                : Surfaces.transparentBorder(ModColors.BORDER_BASE));

        WidgetGroup rootFill = new WidgetGroup(0, 0, initialRootW, initialRootH);
        rootFill.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));

        int bodyH = chapterHeight(state);
        int bodyW = initialRootW - ROOT_PAD_X * 2;
        WidgetGroup mainPanel = SplitPanelLayout.leftPanel(BODY_X, BODY_Y, bodyW, bodyH);

        int homeBtnX = initialRootW - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
        int homeBtnY = ROOT_PAD_Y + ((initialRootH - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
        ButtonWidget homeBtn = new ButtonWidget(homeBtnX, homeBtnY, HOME_BTN_SIZE, HOME_BTN_SIZE,
                Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.subtleBorder()),
                cd -> TabletClientHooks.openTabletUiFromItem(player));
        homeBtn.setClientSideWidget();
        homeBtn.setHoverTexture(Surfaces.bordered(ModColors.elevatedSurface(), ModColors.focusBorder()));
        homeBtn.setClickedTexture(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_ACCENT));
        root.addWidget(homeBtn);
        root.setHomeButton(homeBtn);

        final int contentInset = CONTENT_INSET;
        final int headerY = contentInset;

        int searchStartW = Math.max(40, bodyW - contentInset * 2 - (TOOL_SIZE + HEADER_GAP) * 3);

        TextFieldWidget searchField = StyledTextFields.search(
                contentInset, headerY, searchStartW, TOOL_SIZE,
                () -> state.teams.search, Integer.MAX_VALUE,
                value -> {
                    state.teams.search = SearchFilter.normalizeUserInput(value);
                    refreshActiveTablet();
                },
                focused -> {}
        );

        Runnable[] refresh = new Runnable[1];

        int btnAreaStartX = contentInset + searchStartW;
        int leaveBtnX = btnAreaStartX + HEADER_GAP;
        int joinBtnX = leaveBtnX + TOOL_SIZE + HEADER_GAP;
        int inviteBtnX = joinBtnX + TOOL_SIZE + HEADER_GAP;

        int leaveRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_WARNING);
        int joinRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_SUCCESS);
        int shareRoleColor = UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE);

        ButtonWidget leaveBtn = headerIconButton(leaveBtnX, headerY, "door-open", leaveRoleColor,
                click -> {
                    ClientTeamCache.ClientTeamInfo teamInfo = ClientTeamCache.INSTANCE.getTeam();
                    if (teamInfo != null && teamInfo.isOwner(player.getUUID())) {
                        return;
                    }
                    state.teams.confirmTitleKey = "ui.questsandstuff.teams.confirm_leave_title";
                    state.teams.confirmMessageKey = "ui.questsandstuff.teams.confirm_leave_message";
                    state.teams.confirmAction = "LEAVE";
                    state.teams.confirmTargetUuid = "";
                    state.teams.confirmAnimationStartMs = System.currentTimeMillis();
                    state.teams.confirmModalOpen = true;
                    refresh[0].run();
                });
        leaveBtn.setHoverTooltips(Component.translatable("ui.questsandstuff.teams.leave"));

        ButtonWidget joinBtn = headerIconButton(joinBtnX, headerY, "mail-open", joinRoleColor,
                click -> {
                    state.teams.inviteCodeImportMode = true;
                    state.teams.inviteCodeDraft = "";
                    state.teams.inviteCodeMessage = "";
                    state.teams.inviteCodeAnimationStartMs = System.currentTimeMillis();
                    state.teams.inviteCodeModalOpen = true;
                    refresh[0].run();
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
                    refresh[0].run();
                    ModNetwork.sendToServer(new C2STeamInviteCodePacket());
                });
        inviteBtn.setHoverTooltips(Component.translatable("ui.questsandstuff.teams.share_invite"));

        int listY = headerY + HEADER_H + HEADER_LIST_GAP;
        int listH = Math.max(1, bodyH - listY - GUTTER);
        WidgetGroup memberListPanel = new WidgetGroup(contentInset, listY, bodyW - contentInset * 2, listH);
        memberListPanel.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));

        WidgetGroup modalLayer = new WidgetGroup(0, 0, initialRootW, initialRootH);

        if (ClientTeamCache.INSTANCE.getTeam() == null) {
            ModNetwork.sendToServer(new C2STeamCreatePacket());
        }

        refresh[0] = () -> {
            int crw = rootWidth(state);
            int crh = rootHeight(state);
            root.setSize(crw, crh);
            int cbw = crw - ROOT_PAD_X * 2;
            int cbh = chapterHeight(state);
            mainPanel.setSize(cbw, cbh);

            int hbx = crw - ROOT_PAD_X + (ROOT_PAD_X - HOME_BTN_SIZE) / 2;
            int hby = ROOT_PAD_Y + ((crh - 2 * ROOT_PAD_Y) - HOME_BTN_SIZE) / 2;
            homeBtn.setSelfPosition(hbx, hby);

            int searchW = Math.max(40, cbw - contentInset * 2 - (TOOL_SIZE + HEADER_GAP) * 3);
            searchField.setSize(searchW, TOOL_SIZE);

            int baX = contentInset + searchW;
            leaveBtn.setSelfPosition(baX + HEADER_GAP, headerY);
            joinBtn.setSelfPosition(baX + HEADER_GAP + TOOL_SIZE + HEADER_GAP, headerY);
            inviteBtn.setSelfPosition(baX + HEADER_GAP + TOOL_SIZE * 2 + HEADER_GAP * 2, headerY);

            int clY = headerY + HEADER_H + HEADER_LIST_GAP;
            int clH = Math.max(1, cbh - clY - GUTTER);
            memberListPanel.setSize(cbw - contentInset * 2, clH);

            ClientTeamCache.JoinResult joinResult = ClientTeamCache.INSTANCE.takePendingJoinResult();
            if (joinResult != null) {
                state.teams.inviteCodeMessage = joinResult.message();
                state.teams.inviteCodeMessageSuccess = joinResult.success();
                if (joinResult.success()) {
                    state.teams.inviteCodeModalOpen = false;
                }
            }

            modalLayer.clearAllWidgets();
            rebuildMemberList(memberListPanel, state, refresh[0]);
            rebuildInviteCodeModal(modalLayer, state, refresh[0], crw, crh);
            rebuildConfirmModal(modalLayer, state, refresh[0], crw, crh);
        };

        setActiveTabletState(state);
        setActiveTabletRefresh(refresh[0]);
        root.setRefresher(refresh[0]);
        root.setModalLayer(modalLayer);

        root.addWidgets(rootFill, mainPanel);
        mainPanel.addWidget(searchField);
        mainPanel.addWidget(leaveBtn);
        mainPanel.addWidget(joinBtn);
        mainPanel.addWidget(inviteBtn);
        mainPanel.addWidget(memberListPanel);
        root.addWidget(modalLayer);

        refresh[0].run();
        return root;
    }

    private static void rebuildMemberList(WidgetGroup panel, TabletUiState state, Runnable refresh) {
        panel.clearAllWidgets();

        ClientTeamCache.ClientTeamInfo teamInfo = ClientTeamCache.INSTANCE.getTeam();
        if (teamInfo == null || teamInfo.members().isEmpty()) {
            return;
        }

        UUID localUuid = Minecraft.getInstance().player.getUUID();
        boolean isOwner = teamInfo.isOwner(localUuid);

        List<ClientTeamCache.ClientMember> members = new ArrayList<>(teamInfo.members());
        String query = SearchFilter.normalize(state.teams.search);
        if (!query.isBlank()) {
            members = members.stream()
                    .filter(m -> SearchFilter.matches(query, m.name()))
                    .collect(Collectors.toList());
        }

        int panelW = panel.getSize().width;
        int cardW = Math.max(40, panelW - CONTENT_INSET * 2);
        int y = CONTENT_INSET;

        for (ClientTeamCache.ClientMember member : members) {
            WidgetGroup card = createMemberCard(member, isOwner, state, refresh, cardW);
            card.setSelfPosition(CONTENT_INSET, y);
            panel.addWidget(card);
            y += CARD_H + CARD_GAP;
        }
    }

    private static ButtonWidget headerIconButton(int x, int y, String icon, int color,
                                                  java.util.function.Consumer<ClickData> callback) {
        TabletIconTextButton.Visuals visuals = new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE, color),
                TabletIconTextButton.State.of(withAlpha(color, 66), ModColors.BORDER_ACCENT, color),
                TabletIconTextButton.State.of(withAlpha(color, 90), color, ModColors.TEXT_PRIMARY)
        );
        return TabletIconTextButton.icon(x, y, TOOL_SIZE, TOOL_SIZE, icon, visuals, callback);
    }

    private static WidgetGroup createMemberCard(ClientTeamCache.ClientMember member, boolean isOwner,
                                                 TabletUiState state, Runnable refresh, int cardW) {
        boolean isOwnerMember = member.role() == ClientTeamCache.ClientMember.Role.OWNER;

        WidgetGroup card = new WidgetGroup(0, 0, cardW, CARD_H);
        card.setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE));

        PlayerFaceTexture faceTexture = new PlayerFaceTexture(member.uuid(), member.name());

        ButtonWidget headBtn = new ButtonWidget(1, 1, CARD_H - 2, CARD_H - 2,
                Surfaces.group(faceTexture), cd -> {});
        headBtn.setClientSideWidget();

        int textX = CARD_H + 4;

        int nameY = (CARD_H - 9 - 2 - 9) / 2;
        int roleY = nameY + 9 + 2;
        LabelWidget nameLabel = label(textX, nameY, member.name(), ModColors.TEXT_PRIMARY);
        LabelWidget roleLabel = label(textX, roleY,
                isOwnerMember
                        ? I18n.get("ui.questsandstuff.teams.owner")
                        : I18n.get("ui.questsandstuff.teams.member"),
                isOwnerMember ? ModColors.WARNING : ModColors.TEXT_MUTED);

        card.addWidget(headBtn);
        card.addWidget(nameLabel);
        card.addWidget(roleLabel);

        if (isOwner && !isOwnerMember) {
            UUID localUuid = Minecraft.getInstance().player.getUUID();
            if (!member.uuid().equals(localUuid)) {
                int btnSize = 12;
                int btnY = (CARD_H - btnSize) / 2;
                int kickX = cardW - btnSize - 4;
                int transferX = kickX - btnSize - 2;

                IconOnlyButton transferBtn = IconOnlyButton.create(transferX, btnY, btnSize,
                        "crown", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_INTERACTIVE),
                        click -> {
                            state.teams.confirmTitleKey = "ui.questsandstuff.teams.confirm_transfer_title";
                            state.teams.confirmMessageKey = "ui.questsandstuff.teams.confirm_transfer_message";
                            state.teams.confirmAction = "TRANSFER";
                            state.teams.confirmTargetUuid = member.uuid().toString();
                            state.teams.confirmAnimationStartMs = System.currentTimeMillis();
                            state.teams.confirmModalOpen = true;
                            refresh.run();
                        });
                transferBtn.tooltips(new Component[]{Component.translatable("ui.questsandstuff.teams.transfer_ownership")});
                card.addWidget(transferBtn);

                IconOnlyButton kickBtn = IconOnlyButton.create(kickX, btnY, btnSize,
                        "user-round-x", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_ERROR),
                        click -> {
                            state.teams.confirmTitleKey = "ui.questsandstuff.teams.confirm_kick_title";
                            state.teams.confirmMessageKey = "ui.questsandstuff.teams.confirm_kick_message";
                            state.teams.confirmAction = "KICK";
                            state.teams.confirmTargetUuid = member.uuid().toString();
                            state.teams.confirmAnimationStartMs = System.currentTimeMillis();
                            state.teams.confirmModalOpen = true;
                            refresh.run();
                        });
                kickBtn.tooltips(new Component[]{Component.translatable("ui.questsandstuff.teams.kick")});
                card.addWidget(kickBtn);
            }
        }

        return card;
    }

    private static void rebuildConfirmModal(WidgetGroup layer, TabletUiState state, Runnable refresh,
                                             int layerW, int layerH) {
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
        int pad = 8;
        int buttonW = 82;
        int buttonH = 16;
        int buttonY = 56;

        int px = Math.max(8, (layerW - w) / 2);
        int py = Math.max(24, (layerH - h) / 2);

        WidgetGroup panel = Surfaces.panel(px, py, w, h,
                withAlpha(ModColors.SURFACE_BASE, 246), ModColors.BORDER_ACCENT);

        panel.addWidget(label(pad, 10,
                I18n.get(state.teams.confirmTitleKey),
                ModColors.TEXT_PRIMARY));

        panel.addWidget(label(pad, 28,
                I18n.get(state.teams.confirmMessageKey),
                ModColors.TEXT_MUTED));

        ActionButtons.iconAction(panel, pad, buttonY, buttonW, buttonH,
                "close", TabletVocabulary.text(TabletVocabulary.COMMON_CANCEL), ModColors.ERROR, null, click -> {
            state.teams.confirmModalOpen = false;
            refresh.run();
        });

        ActionButtons.iconAction(panel, w - pad - buttonW, buttonY, buttonW, buttonH,
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

    private static void rebuildInviteCodeModal(WidgetGroup modal, TabletUiState state, Runnable refresh,
                                               int layerW, int layerH) {
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

        int pad = 8;
        int btnSize = 18;
        int gap = 3;
        int codeY = 32;
        int codeH = 44;

        panel.addWidget(label(pad, 6,
                I18n.get(state.teams.inviteCodeImportMode
                        ? "ui.questsandstuff.teams.enter_invite_code"
                        : "ui.questsandstuff.teams.share_invite"),
                ModColors.TEXT_PRIMARY));

        int closeX = panelW - pad - btnSize;
        panel.addWidget(WindowChrome.closeIconButton(closeX, 4, btnSize, btnSize, click -> {
            state.teams.inviteCodeModalOpen = false;
            refresh.run();
        }));

        int x = closeX - gap - btnSize;
        if (state.teams.inviteCodeImportMode) {
            panel.addWidget(WindowChrome.iconButton(x, 4, btnSize, btnSize,
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
            x -= gap + btnSize;
            panel.addWidget(WindowChrome.iconButton(x, 4, btnSize, btnSize,
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
            int renewX = x - gap - btnSize;
            panel.addWidget(WindowChrome.iconButton(renewX, 4, btnSize, btnSize,
                    "reset_quest", UiThemeManager.colorForRole(UiThemeManager.ROLE_ICON_WARNING),
                    click -> {
                        state.teams.inviteCodeDraft = "";
                        state.teams.inviteCodeMessage = I18n.get("ui.questsandstuff.teams.generating_code");
                        state.teams.inviteCodeMessageSuccess = true;
                        ModNetwork.sendToServer(new C2STeamInviteCodePacket());
                        refresh.run();
                    }));
            panel.addWidget(WindowChrome.iconButton(x, 4, btnSize, btnSize,
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
                        pad, codeY, panelW - pad * 2, codeH);
        editor.codeEditor.setLanguageDefinitionUnformatted();
        editor.setBackground(Surfaces.bordered(ModColors.recessedSurface(), ModColors.BORDER_BASE));
        String draft = state.teams.inviteCodeDraft == null ? "" : state.teams.inviteCodeDraft;
        editor.setLines(draft.isBlank() ? List.of("") : List.of(draft.split("\\R", -1)));
        editor.setFocus(true);
        panel.addWidget(editor);

        if (state.teams.inviteCodeMessage != null && !state.teams.inviteCodeMessage.isBlank()) {
            panel.addWidget(label(pad, codeY + codeH + 8,
                    state.teams.inviteCodeMessage,
                    state.teams.inviteCodeMessageSuccess ? ModColors.SUCCESS : ModColors.WARNING));
        }

        modal.addWidget(QuestsAndStuffConfig.popupWindowAnimationsEnabled()
                ? SourceOriginRevealWidget.windowNoShadow(panel,
                () -> state.teams.inviteCodeAnimationStartMs, () -> true, () -> null)
                : panel);
    }
}
