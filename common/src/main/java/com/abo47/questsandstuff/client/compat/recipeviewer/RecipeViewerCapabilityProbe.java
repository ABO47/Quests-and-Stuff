package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RecipeViewerCapabilityProbe {
    private final String providerName;
    private final List<CapabilityCheck> checks;

    private RecipeViewerCapabilityProbe(String providerName, List<CapabilityCheck> checks) {
        this.providerName = providerName;
        this.checks = List.copyOf(checks);
    }

    public static Builder provider(String providerName) {
        return new Builder(providerName);
    }

    public RecipeViewerProviderCapabilities evaluate() {
        List<RecipeViewerCapabilityStatus> statuses = new ArrayList<>();
        Set<RecipeViewerCapability> reported = EnumSet.noneOf(RecipeViewerCapability.class);
        for (CapabilityCheck check : checks) {
            statuses.add(check.evaluate());
            reported.add(check.capability());
        }
        for (RecipeViewerCapability capability : RecipeViewerCapability.values()) {
            if (!reported.contains(capability)) {
                statuses.add(RecipeViewerCapabilityStatus.missing(capability, "not reported"));
            }
        }
        return new RecipeViewerProviderCapabilities(providerName, statuses);
    }

    public static final class Builder {
        private final String providerName;
        private final List<CapabilityCheck> checks = new ArrayList<>();

        private Builder(String providerName) {
            this.providerName = providerName;
        }

        public Builder requires(RecipeViewerCapability capability, String... classNames) {
            checks.add(CapabilityCheck.all(capability, classNames));
            return this;
        }

        public Builder requiresAny(RecipeViewerCapability capability, String... classNames) {
            checks.add(CapabilityCheck.any(capability, classNames));
            return this;
        }

        public RecipeViewerCapabilityProbe build() {
            return new RecipeViewerCapabilityProbe(providerName, checks);
        }
    }

    private record CapabilityCheck(RecipeViewerCapability capability, boolean any, List<String> classNames) {
        private static CapabilityCheck all(RecipeViewerCapability capability, String... classNames) {
            return new CapabilityCheck(capability, false, clean(classNames));
        }

        private static CapabilityCheck any(RecipeViewerCapability capability, String... classNames) {
            return new CapabilityCheck(capability, true, clean(classNames));
        }

        private RecipeViewerCapabilityStatus evaluate() {
            if (classNames.isEmpty()) {
                return RecipeViewerCapabilityStatus.available(capability);
            }
            List<String> missing = missingClasses();
            if (any) {
                return missing.size() < classNames.size()
                        ? RecipeViewerCapabilityStatus.available(capability)
                        : RecipeViewerCapabilityStatus.missing(capability, "missing any of " + String.join(", ", classNames));
            }
            return missing.isEmpty()
                    ? RecipeViewerCapabilityStatus.available(capability)
                    : RecipeViewerCapabilityStatus.missing(capability, "missing " + String.join(", ", missing));
        }

        private List<String> missingClasses() {
            List<String> missing = new ArrayList<>();
            for (String className : classNames) {
                if (!RecipeViewerReflectionUtils.classPresent(className)) {
                    missing.add(className);
                }
            }
            return missing;
        }

        private static List<String> clean(String[] classNames) {
            if (classNames == null || classNames.length == 0) {
                return List.of();
            }
            return Arrays.stream(classNames)
                    .filter(value -> value != null && !value.isBlank())
                    .toList();
        }
    }
}
