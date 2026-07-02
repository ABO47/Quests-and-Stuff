package com.abo47.questsandstuff.client.tablet.app;

import net.minecraft.resources.ResourceLocation;

public record AppDescriptor(String id, String translationKey, ResourceLocation iconTexture, AppComposer composer) {
}
