package com.abo47.questsandstuff.quest.model.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record SelectableQuestRewardDefinition(
        String id,
        ResourceLocation type,
        int amount,
        Map<String, QuestRewardDefinition> rewards
) implements QuestRewardDefinition {
    public static Codec<SelectableQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(SelectableQuestRewardDefinition::id),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(SelectableQuestRewardDefinition::amount),
                QuestRewards.MAP_CODEC.fieldOf("rewards").forGetter(SelectableQuestRewardDefinition::rewards)
        ).apply(instance, (id, amount, rewards) -> new SelectableQuestRewardDefinition(id, type, amount, rewards)));
    }

    public SelectableQuestRewardDefinition {
        rewards = rewards == null ? Map.of() : Map.copyOf(rewards);
    }

    @Override
    public boolean selectable() {
        return true;
    }

    @Override
    public boolean canBeMassClaimed() {
        return false;
    }

    @Override
    public void grant(ServerPlayer player) {
    }

    @Override
    public boolean isSelectableClaimValid(List<String> selectedRewardIds) {
        if (selectedRewardIds == null || selectedRewardIds.isEmpty() || selectedRewardIds.size() != safeAmount()) {
            return false;
        }
        Set<String> unique = new HashSet<>(selectedRewardIds);
        if (unique.size() != selectedRewardIds.size()) {
            return false;
        }
        for (String selected : selectedRewardIds) {
            QuestRewardDefinition reward = rewards.get(selected);
            if (reward == null || !reward.canBeMassClaimed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSelectableClaimValid(ServerPlayer player, List<String> selectedRewardIds) {
        if (!isSelectableClaimValid(selectedRewardIds)) {
            return false;
        }
        for (String selected : selectedRewardIds) {
            QuestRewardDefinition reward = rewards.get(selected);
            if (reward == null || !reward.canClaim(player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void grantSelected(ServerPlayer player, List<String> selectedRewardIds) {
        if (!isSelectableClaimValid(player, selectedRewardIds)) {
            return;
        }
        for (String selected : selectedRewardIds) {
            QuestRewardDefinition reward = rewards.get(selected);
            if (reward != null) {
                reward.grant(player);
            }
        }
    }

    @Override
    public QuestRewardDefinition copyForQuest(Map<String, String> copiedQuestIds) {
        Map<String, QuestRewardDefinition> copied = new LinkedHashMap<>();
        for (Map.Entry<String, QuestRewardDefinition> entry : rewards.entrySet()) {
            copied.put(entry.getKey(), entry.getValue().copyForQuest(copiedQuestIds));
        }
        return new SelectableQuestRewardDefinition(id, type, amount, copied);
    }
}
