package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidIconCodec {
    private static final String PREFIX = "fluid|";

    private FluidIconCodec() {
    }

    public static String iconFromFluid(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        return id == null ? "" : PREFIX + id;
    }

    public static boolean isFluidIcon(String icon) {
        return icon != null && icon.startsWith(PREFIX);
    }

    public static String fluidId(String icon) {
        if (!isFluidIcon(icon)) {
            return "";
        }
        ResourceLocation id = ResourceLocation.tryParse(icon.substring(PREFIX.length()).trim());
        return id == null ? "" : id.toString();
    }

    public static Fluid fluidFromIcon(String icon) {
        ResourceLocation id = ResourceLocation.tryParse(fluidId(icon));
        if (id == null) {
            return Fluids.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    public static ItemStack bucketStack(String icon) {
        Fluid fluid = fluidFromIcon(icon);
        Item bucket = fluid == Fluids.EMPTY ? Items.AIR : fluid.getBucket();
        return bucket == null || bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
    }

    public static String displayName(String icon) {
        String id = fluidId(icon);
        if (id.isBlank()) {
            return "";
        }
        Fluid fluid = fluidFromIcon(icon);
        if (fluid == Fluids.EMPTY) {
            return DisplayNameFormatter.resourceLeaf(id);
        }
        try {
            return FluidStack.create(fluid, 1000).getDisplayName().getString();
        } catch (Exception ignored) {
            ItemStack bucket = bucketStack(icon);
            return bucket.isEmpty() ? DisplayNameFormatter.resourceLeaf(id) : bucket.getHoverName().getString();
        }
    }

    public static Component[] tooltip(String icon) {
        String id = fluidId(icon);
        if (id.isBlank()) {
            return new Component[]{Component.translatable("ui.questsandstuff.icon.unknown").withStyle(ChatFormatting.RED)};
        }
        String name = displayName(icon);
        return new Component[]{
                Component.literal(name).withStyle(ChatFormatting.WHITE),
                Component.literal(id).withStyle(ChatFormatting.DARK_GRAY)
        };
    }
}
