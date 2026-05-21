package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

public interface QuestRewardDefinition {
    Codec<QuestRewardDefinition> CODEC = QuestRewards.CODEC;

    String id();

    ResourceLocation type();

    default int amount() {
        return 1;
    }

    default int safeAmount() {
        return Math.max(1, amount());
    }

    default boolean selectable() {
        return false;
    }

    default boolean canBeMassClaimed() {
        return true;
    }

    void grant(ServerPlayer player);

    default boolean isSelectableClaimValid(List<String> selectedRewardIds) {
        return selectable() && (selectedRewardIds == null || selectedRewardIds.isEmpty());
    }

    default void grantSelected(ServerPlayer player, List<String> selectedRewardIds) {
        if (isSelectableClaimValid(selectedRewardIds)) {
            grant(player);
        }
    }

    default QuestRewardDefinition copyForQuest(Map<String, String> copiedQuestIds) {
        return this;
    }
}
