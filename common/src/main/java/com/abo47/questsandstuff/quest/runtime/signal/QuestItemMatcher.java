package com.abo47.questsandstuff.quest.runtime.signal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public final class QuestItemMatcher {
    private QuestItemMatcher() {
    }

    public static boolean matchesNbt(ItemStack stack, String requiredSnbt) {
        if (requiredSnbt == null || requiredSnbt.isBlank()) {
            return true;
        }
        CompoundTag required = parse(requiredSnbt);
        if (required == null || required.isEmpty()) {
            return true;
        }
        CompoundTag actual = stack.getTag();
        return actual != null && containsAll(actual, required);
    }

    public static void applyNbt(ItemStack stack, String snbt) {
        if (stack == null || snbt == null || snbt.isBlank()) {
            return;
        }
        CompoundTag parsed = parse(snbt);
        if (parsed != null) {
            stack.setTag(parsed.copy());
        }
    }

    private static CompoundTag parse(String snbt) {
        try {
            return TagParser.parseTag(snbt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean containsAll(CompoundTag actual, CompoundTag required) {
        for (String key : required.getAllKeys()) {
            if (!actual.contains(key)) {
                return false;
            }
            Tag requiredValue = required.get(key);
            Tag actualValue = actual.get(key);
            if (requiredValue instanceof CompoundTag requiredCompound && actualValue instanceof CompoundTag actualCompound) {
                if (!containsAll(actualCompound, requiredCompound)) {
                    return false;
                }
                continue;
            }
            if (requiredValue == null || !requiredValue.equals(actualValue)) {
                return false;
            }
        }
        return true;
    }
}
