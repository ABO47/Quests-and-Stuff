package com.abo47.questsandstuff.chunkclaim.model;

import net.minecraft.resources.ResourceLocation;

public record ClaimedChunk(ResourceLocation dimension, int x, int z, boolean forceLoaded) {
}
