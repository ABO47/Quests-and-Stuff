package com.abo47.questsandstuff.client.tablet.teams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.IconOnlyButton;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinFillOverride;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinOverrideKey;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.render.PlayerFaceTexture;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;

final class TeamsMemberCardRenderer {
    private static final int CARD_H = ROW_H_32;
    private static final int CARD_GAP = 8;
    private static final int CONTENT_INSET = GRID_6;

    private TeamsMemberCardRenderer() {
    }

    static void rebuildMemberList(WidgetGroup panel, TabletUiState state, Runnable refresh) {
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

    private static WidgetGroup createMemberCard(ClientTeamCache.ClientMember member, boolean isOwner,
                                                 TabletUiState state, Runnable refresh, int cardW) {
        boolean isOwnerMember = member.role() == ClientTeamCache.ClientMember.Role.OWNER;

        WidgetGroup card = new WidgetGroup(0, 0, cardW, CARD_H);
        card.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE));
        applyCardSkin(card, state, cardW);

        PlayerFaceTexture faceTexture = new PlayerFaceTexture(member.uuid(), member.name());

        ButtonWidget headBtn = new ButtonWidget(1, 1, CARD_H - 2, CARD_H - 2,
                SurfaceFactory.group(faceTexture), cd -> {});
        headBtn.setClientSideWidget();

        int textX = CARD_H + 4;

        int nameY = (CARD_H - 9 - 2 - 9) / 2;
        int roleY = nameY + 9 + 2;
        LabelWidget nameLabel = label(textX, nameY, member.name(), TabletColors.TEXT_PRIMARY);
        LabelWidget roleLabel = label(textX, roleY,
                isOwnerMember
                        ? I18n.get("ui.questsandstuff.teams.owner")
                        : I18n.get("ui.questsandstuff.teams.member"),
                isOwnerMember ? TabletColors.WARNING : TabletColors.TEXT_MUTED);

        card.addWidget(headBtn);
        card.addWidget(nameLabel);
        card.addWidget(roleLabel);

        if (isOwner && !isOwnerMember) {
            UUID localUuid = Minecraft.getInstance().player.getUUID();
            if (!member.uuid().equals(localUuid)) {
                int btnSize = ICON_12;
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

    private static void applyCardSkin(WidgetGroup card, TabletUiState state, int cardW) {
        String rawOverride = SkinOverrideKey.resolveOverride(state, "teams_member_cards");
        if (rawOverride == null) return;
        SkinFillOverride parsed = SkinFillOverride.parse(rawOverride);
        if (parsed == null) return;
        IGuiTexture tex = parsed.createTexture();
        if (tex != null) {
            card.setBackground(IGuiTexture.EMPTY);
            card.addWidget(new ImageWidget(-1, -1, cardW + 2, CARD_H + 2, tex));
        }
    }
}
