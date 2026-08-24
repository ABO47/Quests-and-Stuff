package com.abo47.questsandstuff.client.quest.lock;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.RecipeManager;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class LockClientRefresh {
    private static RecipeBook lastBook;

    private LockClientRefresh() {
    }

    public static void onRecipesUpdated(RecipeManager manager, RegistryAccess access) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:Lock] client recipes updated, refreshing book filter");
        ClientItemLocks.entries();
        ClientBookFilter.refresh(minecraft.player.getRecipeBook());
    }

    public static void tickClient(Minecraft minecraft) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            lastBook = null;
            return;
        }
        RecipeBook book = minecraft.player.getRecipeBook();
        if (book != lastBook) {
            lastBook = book;
            QuestsAndStuffMod.debugLog("[QnS:Lock] recipe book instance changed, applying filter");
            ClientItemLocks.entries();
            ClientBookFilter.refresh(book);
        }
    }

    public static void reset() {
        lastBook = null;
        ClientBookFilter.reset();
    }
}
