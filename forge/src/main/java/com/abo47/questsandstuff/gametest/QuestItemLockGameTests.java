package com.abo47.questsandstuff.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.Unpooled;

import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestTaskDefinition;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.quest.model.task.generic.SimpleQuestTaskDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;
import com.abo47.questsandstuff.quest.runtime.lock.LockedCraftingRecipe;
import com.abo47.questsandstuff.quest.runtime.lock.LockedRecipeSerializer;
import com.abo47.questsandstuff.quest.runtime.lock.OpenMenuIndex;
import com.abo47.questsandstuff.quest.runtime.lock.ServerRecipeWrap;
import com.abo47.questsandstuff.quest.runtime.lock.StageBridge;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(QuestsAndStuffMod.MODID)
public final class QuestItemLockGameTests {
    private static final ResourceLocation LOG_RECIPE_ID =
            new ResourceLocation("minecraft", "oak_planks");

    private QuestItemLockGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void serverRecipesAreWrappedWithGate(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getServer().getRecipeManager();
        ServerRecipeWrap.wrapAll(manager);
        var holder = manager.byKey(LOG_RECIPE_ID);
        if (holder.isEmpty()) {
            throw new GameTestAssertException("oak_planks recipe missing");
        }
        if (!(holder.get() instanceof LockedCraftingRecipe)) {
            throw new GameTestAssertException("crafting recipes should be wrapped after wrapAll");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void gatedRecipeRoundTripsThroughNetwork(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getServer().getRecipeManager();
        ServerRecipeWrap.wrapAll(manager);
        var holder = manager.byKey(LOG_RECIPE_ID);
        if (holder.isEmpty() || !(holder.get() instanceof LockedCraftingRecipe wrapped)) {
            throw new GameTestAssertException("wrapped oak_planks recipe missing");
        }
        if (wrapped.getSerializer() != LockedRecipeSerializer.INSTANCE) {
            throw new GameTestAssertException("wrapped recipe must report the lock serializer");
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            LockedRecipeSerializer.INSTANCE.toNetwork(buf, wrapped);
            CraftingRecipe decoded = LockedRecipeSerializer.INSTANCE.fromNetwork(LOG_RECIPE_ID, buf);
            if (!(decoded instanceof LockedCraftingRecipe decodedLocked)) {
                throw new GameTestAssertException("network decode did not reconstruct the wrapper");
            }
            if (!decodedLocked.getId().equals(LOG_RECIPE_ID)) {
                throw new GameTestAssertException("decoded wrapper lost the inner recipe id");
            }
            ItemStack output = decodedLocked.getResultItem(helper.getLevel().registryAccess());
            if (!output.is(Items.OAK_PLANKS)) {
                throw new GameTestAssertException("decoded wrapper has wrong output");
            }
        } finally {
            buf.release();
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void gateFailsClosedWithoutContextAndReopens(GameTestHelper helper) {
        var holder = helper.getLevel().getServer().getRecipeManager().byKey(LOG_RECIPE_ID);
        if (holder.isEmpty() || !(holder.get() instanceof CraftingRecipe inner)) {
            throw new GameTestAssertException("oak_planks recipe not found");
        }
        LockedCraftingRecipe gated = new LockedCraftingRecipe(inner);
        CraftingContainer grid = singleItemGrid(new ItemStack(Items.OAK_LOG));
        Level level = helper.getLevel();

        boolean unlockedBefore = ItemLockEnforcement.locksActive();
        try {
            ItemLockEnforcement.setLocksActive(true);
            if (gated.matches(grid, level)) {
                throw new GameTestAssertException("gated recipe must fail closed with no crafter context");
            }
            if (!gated.assemble(grid, level.registryAccess()).isEmpty()) {
                throw new GameTestAssertException("assemble must be empty while locked without context");
            }

            ItemLockEnforcement.setLocksActive(false);
            if (!gated.matches(grid, level)) {
                throw new GameTestAssertException("recipe should match again once locks are inactive");
            }
            ItemStack assembled = gated.assemble(grid, level.registryAccess());
            if (!assembled.is(Items.OAK_PLANKS)) {
                throw new GameTestAssertException("unlock should restore normal assemble output");
            }
        } finally {
            ItemLockEnforcement.setLocksActive(unlockedBefore);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void lockIndexLifecycleAndRevoke(GameTestHelper helper) throws IOException {
        Path root = Files.createTempDirectory("qas_item_lock_lifecycle_");
        QuestDefinitionStore store = null;
        try {
            store = new QuestDefinitionStore(root);
            QuestTaskDefinition task = lockedTask("gather", List.of("minecraft:iron_pickaxe"));
            store.upsert(questWithTasks("test/lock_quest", Map.of("gather", task)));

            RuntimeEngine engine = new RuntimeEngine(store, null, null, null);
            ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
            if (!engine.itemLockExists(pickaxe)) {
                throw new GameTestAssertException("locked item should have bindings after index build");
            }
            if (!engine.itemLockBindings(pickaxe).get(0).questId().equals("test/lock_quest")) {
                throw new GameTestAssertException("binding should point at the locking quest");
            }
            if (!ItemLockEnforcement.locksActive()) {
                throw new GameTestAssertException("locks-active fast path should engage");
            }

            store.remove("test/lock_quest");
            engine.rebuildIndex();
            if (engine.itemLockExists(pickaxe)) {
                throw new GameTestAssertException("revoke (definition removal) should clear bindings");
            }
        } finally {
            if (store != null) {
                store.shutdown();
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "questschemagametests.empty")
    public static void openMenuIndexGuardsAndStageBridgeDispatch(GameTestHelper helper) {
        OpenMenuIndex.unrecord(null);
        if (OpenMenuIndex.resolveByGrid(null) != null) {
            throw new GameTestAssertException("resolveByGrid(null) must return null");
        }

        AtomicInteger completed = new AtomicInteger();
        AtomicInteger revoked = new AtomicInteger();
        StageBridge.setHook(new StageBridge.GrantHook() {
            @Override
            public void onQuestCompleted(ServerPlayer player, String questId) {
                completed.incrementAndGet();
            }

            @Override
            public void onQuestRevoked(ServerPlayer player, String questId) {
                revoked.incrementAndGet();
            }
        });
        try {
            StageBridge.onQuestCompleted(null, "some/quest");
            StageBridge.onQuestRevoked(null, "some/quest");
            if (completed.get() != 0 || revoked.get() != 0) {
                throw new GameTestAssertException("null player must not dispatch stage hooks");
            }
            if (StageBridge.installed() == false && completed.get() != 0) {
                throw new GameTestAssertException("unreachable state");
            }
        } finally {
            StageBridge.setHook(null);
        }
        helper.succeed();
    }

    static QuestTaskDefinition lockedTask(String id, List<String> locks) {
        return new SimpleQuestTaskDefinition(
                id,
                new ResourceLocation(QuestsAndStuffMod.MODID, "item"),
                QuestSignalType.ITEM_COLLECTED,
                1,
                "minecraft:oak_log",
                "",
                "",
                locks
        );
    }

    static QuestDefinition questWithTasks(String id, Map<String, QuestTaskDefinition> tasks) {
        return new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                new QuestDisplay(
                        id,
                        "",
                        List.of(),
                        Map.of("Main", ChapterDef.DEFAULT),
                        "minecraft:book",
                        "minecraft:barrier"
                ),
                new QuestSettings(false, QuestVisibilityMode.LOCKED, false, false, false, true),
                Set.of(),
                tasks,
                Map.of()
        );
    }

    private static CraftingContainer singleItemGrid(ItemStack stack) {
        return new FixedGrid(stack);
    }

    private static class FixedGrid implements CraftingContainer {
        private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

        FixedGrid(ItemStack first) {
            items.set(0, first);
        }

        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return items.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, ItemStack value) {
            items.set(index, value);
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.clear();
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void fillStackedContents(StackedContents contents) {
        }
    }
}
