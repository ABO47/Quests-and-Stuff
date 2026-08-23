package com.abo47.questsandstuff.forge.runtime.signal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockMenuGating;
import com.abo47.questsandstuff.quest.runtime.lock.ServerRecipeWrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalHelper;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeQuestEventBridge {
    private final Map<UUID, Map<String, Integer>> inventorySnapshots = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> statSnapshots = new HashMap<>();

    @SubscribeEvent
    public void onItemCrafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemLockEnforcement.undoLockedCraft(
                    player, event.getCrafting(), event.getInventory());
        }
    }

    @SubscribeEvent
    public void onContainerOpen(net.minecraftforge.event.entity.player.PlayerContainerEvent.Open event) {
        ItemLockMenuGating.gateCraftingMenu(
                event.getEntity(), event.getContainer());
    }

    @SubscribeEvent
    public void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerRecipeWrap.wrapAll(player.server.getRecipeManager());
            QuestServiceRegistry.engine(player.server).preparePlayerForFullSync(player);
            QuestServiceRegistry.sync(player.server).syncFull(player);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }

        QuestSignalHelper.tick(player, inventorySnapshots, statSnapshots);
    }

    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItem().getItem().getItem());
            QuestSignalHelper.send(player, QuestSignalType.ITEM_COLLECTED, id.toString(), event.getItem().getItem().getCount());
        }
    }

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItem().getItem());
            QuestSignalHelper.send(player, QuestSignalType.ITEM_USED, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            QuestSignalHelper.send(player, QuestSignalType.ITEM_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(player.level().getBlockState(event.getPos()).getBlock());
            QuestSignalHelper.send(player, QuestSignalType.BLOCK_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getTarget().getType());
            QuestSignalHelper.send(player, QuestSignalType.ENTITY_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            QuestSignalHelper.send(player, QuestSignalType.ENTITY_KILLED, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestSignalHelper.send(player, QuestSignalType.ADVANCEMENT, event.getAdvancement().getId().toString(), 1);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestSignalHelper.send(player, QuestSignalType.DIMENSION_CHANGED, event.getTo().location().toString(), 1);
        }
    }

    @SubscribeEvent
    public void onXp(PlayerXpEvent.XpChange event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0) {
            QuestSignalHelper.send(player, QuestSignalType.XP_CHANGE, "", event.getAmount());
        }
    }

    @SubscribeEvent
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        Player raw = event.getEntity();
        if (raw instanceof ServerPlayer player) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getCrafting().getItem());
            QuestSignalHelper.send(player, QuestSignalType.ITEM_CRAFTED, id.toString(), event.getCrafting().getCount());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            inventorySnapshots.remove(player.getUUID());
            statSnapshots.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getPlayer() instanceof ServerPlayer player) {
            if (!ChunkClaimProtection.allowedBreakPlace(player, level, event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            if (!ChunkClaimProtection.allowedBreakPlace(player, level, event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockInteractCancel(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            if (!ChunkClaimProtection.allowedInteract(player, level, event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(pos -> ChunkClaimProtection.isProtectedChunk(level, new ChunkPos(pos), true));
        }
    }

    @SubscribeEvent
    public void onMobGrief(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof LivingEntity living
                && living.level() instanceof ServerLevel level) {
            if (ChunkClaimProtection.isProtectedChunk(level, new ChunkPos(living.blockPosition()), false, true)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer attacker
                && event.getTarget() instanceof ServerPlayer target
                && attacker.level() instanceof ServerLevel level) {
            if (!ChunkClaimProtection.allowedPvp(attacker, target, level, target.blockPosition())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        QuestServiceRegistry.chunkClaims(event.getServer()).suppressFire();
    }

}
