package com.abo47.questsandstuff.network.chunkclaim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.TeamManager;

public record C2SChunkClaimActionPacket(Action action, ResourceLocation dimension, int x, int z) {
    public enum Action {
        CLAIM,
        UNCLAIM,
        TOGGLE_FORCE,
        REQUEST
    }

    public static C2SChunkClaimActionPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        ResourceLocation dimension = buf.readResourceLocation();
        int x = buf.readInt();
        int z = buf.readInt();
        return new C2SChunkClaimActionPacket(action, dimension, x, z);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeResourceLocation(dimension);
        buf.writeInt(x);
        buf.writeInt(z);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null) {
            return;
        }
        context.enqueueWork(() -> {
            ServerLevel level = player.serverLevel();
            TeamManager manager = new TeamManager(level, QuestServiceRegistry.engine(player.server));
            ChunkClaimPacketHelper.applyAction(player, manager, action, dimension, x, z);
        });
    }
}
