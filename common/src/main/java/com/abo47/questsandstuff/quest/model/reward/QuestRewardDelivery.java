package com.abo47.questsandstuff.quest.model.reward;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

final class QuestRewardDelivery {
    private QuestRewardDelivery() {
    }

    static void giveItem(ServerPlayer player, ItemStack stack) {
        if (player == null || stack.isEmpty()) {
            return;
        }
        ItemStack copy = stack.copy();
        if (player.addItem(copy)) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.2F,
                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
            return;
        }
        ItemEntity dropped = player.drop(copy, false);
        if (dropped != null) {
            dropped.setNoPickUpDelay();
            dropped.setTarget(player.getUUID());
        }
    }
}
