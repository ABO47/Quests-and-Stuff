package com.abo47.questsandstuff.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.runtime.lock.possession.PossessionPolicy;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalHelper;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class FabricQuestEventBridge {
    private static final Map<UUID, Map<String, Integer>> INVENTORY_SNAPSHOTS = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> STAT_SNAPSHOTS = new HashMap<>();
    private static final Map<UUID, String> DIMENSION_SNAPSHOTS = new HashMap<>();
    private static boolean registered;

    private FabricQuestEventBridge() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLogout(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                onPlayerTick(player);
            }
            QuestServiceRegistry.chunkClaims(server).suppressFire();
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer serverPlayer && !stack.isEmpty()) {
                if (PossessionPolicy.deniesUse(serverPlayer, stack)) {
                    return InteractionResultHolder.fail(stack);
                }
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                QuestSignalHelper.send(serverPlayer, QuestSignalType.ITEM_INTERACT, id.toString(), 1);
            }
            return InteractionResultHolder.pass(stack);
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level) {
                return ChunkClaimProtection.allowedBreakPlace(serverPlayer, level, pos);
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level) {
                BlockPos pos = hitResult.getBlockPos();
                if (!ChunkClaimProtection.allowedInteract(serverPlayer, level, pos)) {
                    return InteractionResult.FAIL;
                }
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock());
                QuestSignalHelper.send(serverPlayer, QuestSignalType.BLOCK_INTERACT, id.toString(), 1);
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level) {
                if (!ChunkClaimProtection.allowedInteract(serverPlayer, level, entity.blockPosition())) {
                    return InteractionResult.FAIL;
                }
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                QuestSignalHelper.send(serverPlayer, QuestSignalType.ENTITY_INTERACT, id.toString(), 1);
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer attacker
                    && entity instanceof ServerPlayer target
                    && world instanceof ServerLevel level) {
                if (!ChunkClaimProtection.allowedPvp(attacker, target, level, target.blockPosition())) {
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static void onAwardStat(ServerPlayer player, Stat<?> stat, int amount) {
        if (player == null || stat == null || amount <= 0) {
            return;
        }
        Object value = stat.getValue();
        if (stat.getType() == Stats.ITEM_CRAFTED && value instanceof Item item) {
            QuestSignalHelper.send(player, QuestSignalType.ITEM_CRAFTED, BuiltInRegistries.ITEM.getKey(item).toString(), amount);
            return;
        }
        if (stat.getType() == Stats.ITEM_USED && value instanceof Item item) {
            QuestSignalHelper.send(player, QuestSignalType.ITEM_USED, BuiltInRegistries.ITEM.getKey(item).toString(), amount);
            return;
        }
        if (stat.getType() == Stats.ITEM_PICKED_UP && value instanceof Item item) {
            QuestSignalHelper.send(player, QuestSignalType.ITEM_COLLECTED, BuiltInRegistries.ITEM.getKey(item).toString(), amount);
            return;
        }
        if (stat.getType() == Stats.ENTITY_KILLED && value instanceof EntityType<?> entityType) {
            QuestSignalHelper.send(player, QuestSignalType.ENTITY_KILLED, BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString(), amount);
        }
    }

    public static void onAdvancement(ServerPlayer player, Advancement advancement) {
        if (player != null && advancement != null) {
            QuestSignalHelper.send(player, QuestSignalType.ADVANCEMENT, advancement.getId().toString(), 1);
        }
    }

    public static void onXp(ServerPlayer player, int amount) {
        if (player != null && amount > 0) {
            QuestSignalHelper.send(player, QuestSignalType.XP_CHANGE, "", amount);
        }
    }

    private static void onPlayerLogin(ServerPlayer player) {
        if (player == null) {
            return;
        }
        DIMENSION_SNAPSHOTS.put(player.getUUID(), player.level().dimension().location().toString());
        QuestServiceRegistry.engine(player.server).preparePlayerForFullSync(player);
        QuestServiceRegistry.sync(player.server).syncFull(player);
    }

    private static void onPlayerLogout(ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUUID();
        INVENTORY_SNAPSHOTS.remove(id);
        STAT_SNAPSHOTS.remove(id);
        DIMENSION_SNAPSHOTS.remove(id);
    }

    private static void onPlayerTick(ServerPlayer player) {
        QuestSignalHelper.tick(player, INVENTORY_SNAPSHOTS, STAT_SNAPSHOTS);

        String dimension = player.level().dimension().location().toString();
        String previousDimension = DIMENSION_SNAPSHOTS.put(player.getUUID(), dimension);
        if (previousDimension != null && !previousDimension.equals(dimension)) {
            QuestSignalHelper.send(player, QuestSignalType.DIMENSION_CHANGED, dimension, 1);
        }
    }

}