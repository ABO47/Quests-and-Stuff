package com.abo47.questsandstuff.client.compat.recipeviewer.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

public class QuestsAndStuffEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // Selection buttons are drawn by the shared screen overlay so Forge and Fabric use the same control.
    }
}
