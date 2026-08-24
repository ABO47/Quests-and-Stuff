package com.abo47.questsandstuff.client.quest.lock;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.QuestsAndStuffConfig;

public final class LockedItemTooltips {
    public static final String TOOLTIP_KEY = "tooltip.questsandstuff.item_locked";

    private LockedItemTooltips() {
    }

    public static void append(List<Component> tooltip, ItemStack stack) {
        if (!QuestsAndStuffConfig.itemLockLockedTooltips()) {
            return;
        }
        if (stack == null || stack.isEmpty() || !ClientItemLocks.isLocked(stack)) {
            return;
        }
        tooltip.add(Component.translatable(TOOLTIP_KEY).withStyle(ChatFormatting.DARK_RED));
    }
}
