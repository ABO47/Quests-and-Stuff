package com.abo47.questsandstuff.network.quest.runtime;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;

public record C2SClaimSelectableRewardPacket(String questId, String rewardId, List<String> selectedRewardIds) {
    private static final int MAX_SELECTED_REWARDS = 64;

    public static C2SClaimSelectableRewardPacket decode(FriendlyByteBuf buf) {
        String questId = buf.readUtf();
        String rewardId = buf.readUtf();
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_SELECTED_REWARDS) {
            throw new IllegalArgumentException("Invalid selectable reward selection size: " + size);
        }
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            selected.add(buf.readUtf());
        }
        return new C2SClaimSelectableRewardPacket(questId, rewardId, selected);
    }

    public void encode(FriendlyByteBuf buf) {
        List<String> selected = selectedRewardIds == null ? List.of() : selectedRewardIds;
        if (selected.size() > MAX_SELECTED_REWARDS) {
            throw new IllegalArgumentException("Too many selectable reward choices: " + selected.size());
        }
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeUtf(rewardId == null ? "" : rewardId);
        buf.writeVarInt(selected.size());
        for (String id : selected) {
            buf.writeUtf(id == null ? "" : id);
        }
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (player != null) {
            context.enqueueWork(() -> QuestServiceRegistry.engine(player.server)
                    .claimSelectedRewardAndAvailableRewards(player, questId == null ? "" : questId, rewardId, selectedRewardIds));
        }
    }
}
