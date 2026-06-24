package com.abo47.questsandstuff.client.compat.recipeviewer;

public record RecipeViewerCapabilityStatus(RecipeViewerCapability capability, boolean available, String reason) {
    public RecipeViewerCapabilityStatus {
        if (capability == null) {
            throw new IllegalArgumentException("capability");
        }
        reason = reason == null || reason.isBlank() ? (available ? "available" : "not reported") : reason;
    }

    static RecipeViewerCapabilityStatus available(RecipeViewerCapability capability) {
        return new RecipeViewerCapabilityStatus(capability, true, "available");
    }

    static RecipeViewerCapabilityStatus missing(RecipeViewerCapability capability, String reason) {
        return new RecipeViewerCapabilityStatus(capability, false, reason);
    }
}
