package com.abo47.questsandstuff.network.team;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.model.team.TeamData;
import com.abo47.questsandstuff.quest.team.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record C2STeamCreatePacket() {
    public static C2STeamCreatePacket decode(FriendlyByteBuf buf) {
        return new C2STeamCreatePacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null) {
            return;
        }
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            TeamManager manager = new TeamManager(level, QuestServices.engine(player.server));
            TeamData team = manager.createTeam(player);
            S2CTeamSyncPacket.send(player, team);
        });
    }
}
