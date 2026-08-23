package com.abo47.questsandstuff.forge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockHooks;
import com.abo47.questsandstuff.quest.runtime.lock.ServerRecipeWrap;

public final class ForgeLockHooks {
    private ForgeLockHooks() {
    }

    public static void register() {
        ItemLockHooks.setCraftingPlayerResolver(ForgeLockHooks::resolveCraftingPlayer);
        ItemLockEnforcement.setLockedForAnyoneResolver(ForgeLockHooks::lockedForAnyOnlinePlayer);
    }

    private static boolean lockedForAnyOnlinePlayer(ItemStack stack) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || stack.isEmpty()) {
            return false;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ItemLockEnforcement.isLocked(player, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void wrapServerRecipes(MinecraftServer server) {
        ServerRecipeWrap.wrapAll(server.getRecipeManager());
    }

    private static Player resolveCraftingPlayer(Container grid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu == null) {
                    continue;
                }
                for (Slot slot : menu.slots) {
                    if (slot.container == grid) {
                        return player;
                    }
                }
            }
        }
        if (FMLEnvironment.dist.isClient()) {
            Minecraft minecraft = Minecraft.getInstance();
            Player localPlayer = minecraft.player;
            if (localPlayer != null && localPlayer.containerMenu != null) {
                for (Slot slot : localPlayer.containerMenu.slots) {
                    if (slot.container == grid) {
                        return localPlayer;
                    }
                }
            }
        }
        return ForgeHooks.getCraftingPlayer();
    }
}
