package com.abo47.questsandstuff.client.tablet.icons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemStackIconCodec {
    public static final String PREFIX = "item_stack|";

    private ItemStackIconCodec() {
    }

    public static String iconFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return "";
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return id.toString();
        }
        return PREFIX + id + tag;
    }

    public static boolean isStackIcon(String icon) {
        return icon != null && icon.trim().startsWith(PREFIX);
    }

    public static ItemStack stackFromIcon(String icon) {
        ParsedIcon parsed = parse(icon);
        if (parsed.itemId().isBlank()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(parsed.itemId());
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (!parsed.nbt().isBlank()) {
            try {
                stack.setTag(TagParser.parseTag(parsed.nbt()));
            } catch (Exception ignored) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    public static Component[] tooltip(String icon) {
        ItemStack stack = stackFromIcon(icon);
        if (stack.isEmpty()) {
            return new Component[0];
        }
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            lines.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        String summary = nbtSummary(stack);
        if (!summary.isBlank()) {
            lines.add(Component.literal("NBT: " + summary).withStyle(ChatFormatting.GOLD));
        }
        return lines.toArray(Component[]::new);
    }

    public static String nbtSummary(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return "";
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>(tag.getAllKeys());
        if (keys.isEmpty()) {
            return "";
        }
        int limit = Math.min(3, keys.size());
        String summary = String.join(", ", keys.subList(0, limit));
        if (keys.size() > limit) {
            summary += ", ...";
        }
        return summary;
    }

    private static ParsedIcon parse(String icon) {
        String value = icon == null ? "" : icon.trim();
        if (!value.startsWith(PREFIX)) {
            return new ParsedIcon("", "");
        }
        String body = value.substring(PREFIX.length()).trim();
        if (body.isBlank()) {
            return new ParsedIcon("", "");
        }
        int nbtStart = body.indexOf('{');
        if (nbtStart < 0) {
            return new ParsedIcon(body, "");
        }
        String itemId = body.substring(0, nbtStart).trim();
        String nbt = body.substring(nbtStart).trim();
        return new ParsedIcon(itemId, nbt);
    }

    private record ParsedIcon(String itemId, String nbt) {
    }
}
