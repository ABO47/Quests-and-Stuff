package com.abo47.questsandstuff.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.abo47.questsandstuff.platform.Services;

public final class TabletItem extends Item {
    public TabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            openClient(player);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && context.getLevel().isClientSide) {
            openClient(player);
        }
        return InteractionResult.CONSUME;
    }

    private static void openClient(Player player) {
        Services.platform().openTabletUi(player);
    }
}
