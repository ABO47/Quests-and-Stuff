package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public final class ItemLockHooks {
    private static volatile Function<Container, Player> craftingPlayerResolver;

    private ItemLockHooks() {
    }

    public static void setCraftingPlayerResolver(Function<Container, Player> resolver) {
        craftingPlayerResolver = resolver;
    }

    public static Player resolveCraftingPlayer(Container grid) {
        Function<Container, Player> resolver = craftingPlayerResolver;
        return resolver == null ? null : resolver.apply(grid);
    }
}
