package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeViewerCapabilityMatrixTest {
    @Test
    void probeReportsAvailableAndMissingClassesByCapability() {
        RecipeViewerCapabilityProbe probe = RecipeViewerCapabilityProbe.provider("TEST")
                .requires(RecipeViewerCapability.AVAILABLE, "java.lang.String")
                .requires(RecipeViewerCapability.SNAPSHOT_RENDERING, "missing.recipe.Viewer")
                .requiresAny(RecipeViewerCapability.VISIBLE_RECIPE_PICK, "missing.recipe.Screen", "java.lang.Integer")
                .build();

        RecipeViewerProviderCapabilities capabilities = probe.evaluate();

        assertTrue(capabilities.supports(RecipeViewerCapability.AVAILABLE));
        assertFalse(capabilities.supports(RecipeViewerCapability.SNAPSHOT_RENDERING));
        assertTrue(capabilities.reason(RecipeViewerCapability.SNAPSHOT_RENDERING).contains("missing.recipe.Viewer"));
        assertTrue(capabilities.supports(RecipeViewerCapability.VISIBLE_RECIPE_PICK));
    }

    @Test
    void integrationMatrixReportsEveryProviderAndCapability() {
        List<RecipeViewerProviderCapabilities> matrix = RecipeViewerIntegrations.capabilityMatrix();

        assertEquals(3, matrix.size());
        assertTrue(matrix.stream().anyMatch(provider -> provider.providerName().equals("JEI")));
        assertTrue(matrix.stream().anyMatch(provider -> provider.providerName().equals("EMI")));
        assertTrue(matrix.stream().anyMatch(provider -> provider.providerName().equals("REI")));
        for (RecipeViewerProviderCapabilities provider : matrix) {
            for (RecipeViewerCapability capability : RecipeViewerCapability.values()) {
                assertEquals(capability, provider.status(capability).capability(), provider.providerName() + " missing " + capability);
                assertFalse(provider.reason(capability).isBlank(), provider.providerName() + " blank reason for " + capability);
            }
        }
    }

    @Test
    void debugProbeHasStableProviderLinesAndReasons() {
        List<String> probe = RecipeViewerIntegrations.debugProbe();

        assertEquals(3, probe.size());
        assertTrue(probe.get(0).startsWith("JEI: "));
        assertTrue(probe.get(1).startsWith("EMI: "));
        assertTrue(probe.get(2).startsWith("REI: "));
        assertTrue(probe.stream().allMatch(line -> line.contains(RecipeViewerCapability.AVAILABLE.id() + "=")));
        assertTrue(probe.stream().allMatch(line -> line.contains(RecipeViewerCapability.VISIBLE_RECIPE_PICK.id() + "=")));
    }
}
