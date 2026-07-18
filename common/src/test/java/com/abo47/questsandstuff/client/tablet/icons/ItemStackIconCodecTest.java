package com.abo47.questsandstuff.client.tablet.icons;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStackIconCodecTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void plainStackUsesNormalItemIcon() {
        assertEquals("minecraft:diamond", ItemStackIconCodec.iconFromStack(new ItemStack(Items.DIAMOND)));
    }

    @Test
    void taggedStackUsesEncodedStackIcon() {
        ItemStack source = new ItemStack(Items.DIAMOND_SWORD);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Unbreakable", true);
        source.setTag(tag);

        String icon = ItemStackIconCodec.iconFromStack(source);
        ItemStack decoded = ItemStackIconCodec.stackFromIcon(icon);

        assertTrue(icon.startsWith(ItemStackIconCodec.PREFIX + "minecraft:diamond_sword"));
        assertEquals("minecraft:diamond_sword", BuiltInRegistries.ITEM.getKey(decoded.getItem()).toString());
        assertTrue(decoded.hasTag());
        assertTrue(decoded.getTag().getBoolean("Unbreakable"));
    }

    @Test
    void invalidEncodedIconReturnsEmptyStack() {
        assertTrue(ItemStackIconCodec.stackFromIcon(ItemStackIconCodec.PREFIX + "not an item").isEmpty());
        assertFalse(ItemStackIconCodec.isStackIcon("minecraft:diamond"));
    }
}
