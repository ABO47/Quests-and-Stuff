package com.abo47.questsandstuff.network.team;

import net.minecraft.network.FriendlyByteBuf;

import com.abo47.questsandstuff.network.ModPacketContext;

public record C2STeamCreatePacket() {
    public static C2STeamCreatePacket decode(FriendlyByteBuf buf) {
        return new C2STeamCreatePacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(ModPacketContext context) {
        TeamPacketHelper.onServer(context, (player, manager) -> {
            var team = manager.createTeam(player);
            TeamPacketHelper.send(player, team);
        });
    }
}
