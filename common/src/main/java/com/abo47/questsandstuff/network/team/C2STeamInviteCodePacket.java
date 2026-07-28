package com.abo47.questsandstuff.network.team;

import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public record C2STeamInviteCodePacket() {
    public static C2STeamInviteCodePacket decode(FriendlyByteBuf buf) {
        return new C2STeamInviteCodePacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(ModPacketContext context) {
        TeamPacketHelper.onServer(context, (player, manager) -> {
            String code = manager.generateInviteCode(player);
            if (code.isBlank()) {
                return;
            }
            var team = manager.getTeam(player);
            TeamPacketHelper.send(player, team);
        });
    }
}
