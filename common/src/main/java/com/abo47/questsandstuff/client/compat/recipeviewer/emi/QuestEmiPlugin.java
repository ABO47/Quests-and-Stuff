package com.abo47.questsandstuff.client.compat.recipeviewer.emi;

import com.abo47.questsandstuff.client.quest.lock.ClientItemLocks;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

public class QuestEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.removeEmiStacks(stack -> ClientItemLocks.isLocked(stack.getItemStack()));
        registry.removeRecipes(recipe -> recipe.getOutputs().stream()
                .anyMatch(output -> ClientItemLocks.isLocked(output.getItemStack())));
    }
}
