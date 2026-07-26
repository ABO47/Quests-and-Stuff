package com.abo47.questsandstuff.client.tablet.app;

import net.minecraft.resources.ResourceLocation;

public record AppDescriptor(String id, String translationKey, ResourceLocation iconTexture, String iconKey, AppComposer composer) {
    public AppDescriptor(String id, String translationKey, ResourceLocation iconTexture, AppComposer composer) {
        this(id, translationKey, iconTexture, null, composer);
    }
}
