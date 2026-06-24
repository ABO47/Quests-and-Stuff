package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public record RecipeViewerProviderCapabilities(String providerName, List<RecipeViewerCapabilityStatus> statuses) {
    public RecipeViewerProviderCapabilities {
        providerName = providerName == null || providerName.isBlank() ? "unknown" : providerName;
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
    }

    public boolean supports(RecipeViewerCapability capability) {
        return status(capability).available();
    }

    public String reason(RecipeViewerCapability capability) {
        return status(capability).reason();
    }

    public RecipeViewerCapabilityStatus status(RecipeViewerCapability capability) {
        if (capability == null) {
            return RecipeViewerCapabilityStatus.missing(RecipeViewerCapability.AVAILABLE, "not reported");
        }
        for (RecipeViewerCapabilityStatus status : statuses) {
            if (status.capability() == capability) {
                return status;
            }
        }
        return RecipeViewerCapabilityStatus.missing(capability, "not reported");
    }

    public List<RecipeViewerCapabilityStatus> missingCapabilities() {
        List<RecipeViewerCapabilityStatus> missing = new ArrayList<>();
        for (RecipeViewerCapabilityStatus status : statuses) {
            if (!status.available()) {
                missing.add(status);
            }
        }
        return List.copyOf(missing);
    }

    public String debugLine() {
        StringJoiner joiner = new StringJoiner(", ", providerName + ": ", "");
        Map<RecipeViewerCapability, RecipeViewerCapabilityStatus> byCapability = new EnumMap<>(RecipeViewerCapability.class);
        for (RecipeViewerCapabilityStatus status : statuses) {
            byCapability.put(status.capability(), status);
        }
        for (RecipeViewerCapability capability : RecipeViewerCapability.values()) {
            RecipeViewerCapabilityStatus status = byCapability.getOrDefault(
                    capability,
                    RecipeViewerCapabilityStatus.missing(capability, "not reported")
            );
            joiner.add(capability.id() + "=" + (status.available() ? "yes" : "no(" + status.reason() + ")"));
        }
        return joiner.toString();
    }
}
