package com.abo47.questsandstuff.client.compat.recipeviewer;

import net.minecraft.resources.ResourceLocation;

public final class JeiRecipeTypeUids {
    private JeiRecipeTypeUids() {
    }

    public static ResourceLocation alias(ResourceLocation vanillaTypeKey) {
        if (vanillaTypeKey == null) {
            return null;
        }
        return switch (vanillaTypeKey.getPath()) {
            case "smelting" -> new ResourceLocation(vanillaTypeKey.getNamespace(), "furnace");
            case "campfire_cooking" -> new ResourceLocation(vanillaTypeKey.getNamespace(), "campfire");
            default -> vanillaTypeKey;
        };
    }
}
