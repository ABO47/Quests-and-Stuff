package com.abo47.questsandstuff.client.tablet.modal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;

final class PickerTooltips {
    private PickerTooltips() {
    }

    static Component[] item(String id) {
        ResourceLocation key = ResourceLocation.tryParse(stripTagPrefix(id));
        if (key == null) {
            return nameAndId(fallbackName(id), id);
        }
        if (isTag(id)) {
            return nameAndId(DisplayNameFormatter.resourceLeaf(key.toString()), "#" + key);
        }
        Item item = BuiltInRegistries.ITEM.getOptional(key).orElse(null);
        String name = item == null || item == Items.AIR ? DisplayNameFormatter.resourceLeaf(key.toString()) : item.getDescription().getString();
        return nameAndId(name, key.toString());
    }

    static Component[] block(String id) {
        ResourceLocation key = ResourceLocation.tryParse(stripTagPrefix(id));
        if (key == null) {
            return nameAndId(fallbackName(id), id);
        }
        if (isTag(id)) {
            return nameAndId(DisplayNameFormatter.resourceLeaf(key.toString()), "#" + key);
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
        String name = block == null || block == Blocks.AIR ? DisplayNameFormatter.resourceLeaf(key.toString()) : block.getName().getString();
        return nameAndId(name, key.toString());
    }

    static Component[] nameAndId(String name, String id) {
        String safeName = name == null || name.isBlank() ? fallbackName(id) : name;
        String safeId = id == null ? "" : id.trim();
        if (safeId.isBlank()) {
            return new Component[]{Component.literal(safeName).withStyle(ChatFormatting.WHITE)};
        }
        return new Component[]{
                Component.literal(safeName).withStyle(ChatFormatting.WHITE),
                Component.literal(safeId).withStyle(ChatFormatting.DARK_GRAY)
        };
    }

    static Component[] nameOnly(String name) {
        String safeName = name == null ? "" : name.trim();
        return new Component[]{Component.literal(safeName).withStyle(ChatFormatting.WHITE)};
    }

    private static boolean isTag(String id) {
        return id != null && id.trim().startsWith("#");
    }

    private static String stripTagPrefix(String id) {
        String value = id == null ? "" : id.trim();
        return value.startsWith("#") ? value.substring(1).trim() : value;
    }

    private static String fallbackName(String id) {
        String value = stripTagPrefix(id);
        return value.isBlank() ? "" : DisplayNameFormatter.resourceLeaf(value);
    }
}
