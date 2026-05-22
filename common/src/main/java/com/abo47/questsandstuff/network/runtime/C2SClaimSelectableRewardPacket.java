package com.abo47.questsandstuff.network.runtime;

import com.abo47.questsandstuff.network.QuestPacketContext;

import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public record C2SClaimSelectableRewardPacket(String questId, String rewardId, List<String> selectedRewardIds) {
    public static C2SClaimSelectableRewardPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String rewardId = buf.readUtf();
        int size = buf.readVarInt();
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            selected.add(buf.readUtf());
        }
        return new C2SClaimSelectableRewardPacket(questId, rewardId, selected);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId);
        buf.writeUtf(rewardId);
        buf.writeVarInt(selectedRewardIds.size());
        for (String id : selectedRewardIds) {
            buf.writeUtf(id);
        }
    }

    public void handle(QuestPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServices.engine(player.server)
                    .claimSelectedRewardAndAvailableRewards(player, questId == null ? "" : questId, rewardId, selectedRewardIds));
        }
    }
}
