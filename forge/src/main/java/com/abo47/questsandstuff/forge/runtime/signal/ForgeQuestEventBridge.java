package com.abo47.questsandstuff.forge.runtime.signal;

import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignal;
import com.abo47.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abo47.questsandstuff.quest.runtime.signal.QuestStatHelper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ForgeQuestEventBridge {
    private final Map<UUID, Map<String, Integer>> inventorySnapshots = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> statSnapshots = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestServices.engine(player.server).preparePlayerForFullSync(player);
            QuestServices.sync(player.server).syncFull(player);
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

        send(player, QuestSignalType.LOCATION_TICK, "", 1);
        send(player, QuestSignalType.XP_SNAPSHOT, "points", Math.max(0, player.totalExperience));
        send(player, QuestSignalType.XP_SNAPSHOT, "level", Math.max(0, player.experienceLevel));

        ResourceLocation biomeId = player.serverLevel().getBiome(player.blockPosition())
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(ResourceLocation.tryBuild("minecraft", "plains"));
        send(player, QuestSignalType.BIOME_ENTER, biomeId.toString(), 1);

        Map<Structure, LongSet> structures = player.serverLevel().structureManager().getAllStructuresAt(player.blockPosition());
        if (!structures.isEmpty()) {
            for (Structure structure : structures.keySet()) {
                ResourceLocation structureId = player.server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(structure);
                if (structureId != null) {
                    send(player, QuestSignalType.STRUCTURE_ENTER, structureId.toString(), 1);
                }
            }
        }

        pushInventorySnapshotDelta(player);
        pushStatSnapshotDelta(player);
    }

    @SubscribeEvent
    public void onPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItem().getItem().getItem());
            send(player, QuestSignalType.ITEM_COLLECTED, id.toString(), event.getItem().getItem().getCount());
        }
    }

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItem().getItem());
            send(player, QuestSignalType.ITEM_USED, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
            send(player, QuestSignalType.ITEM_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(event.getPos()).getBlock());
            send(player, QuestSignalType.BLOCK_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType());
            send(player, QuestSignalType.ENTITY_INTERACT, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
            send(player, QuestSignalType.ENTITY_KILLED, id.toString(), 1);
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, QuestSignalType.ADVANCEMENT, event.getAdvancement().getId().toString(), 1);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            send(player, QuestSignalType.DIMENSION_CHANGED, event.getTo().location().toString(), 1);
        }
    }

    @SubscribeEvent
    public void onXp(PlayerXpEvent.XpChange event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0) {
            send(player, QuestSignalType.XP_CHANGE, "", event.getAmount());
        }
    }

    @SubscribeEvent
    public void onCraft(PlayerEvent.ItemCraftedEvent event) {
        Player raw = event.getEntity();
        if (raw instanceof ServerPlayer player) {
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getCrafting().getItem());
            send(player, QuestSignalType.ITEM_CRAFTED, id.toString(), event.getCrafting().getCount());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            inventorySnapshots.remove(player.getUUID());
            statSnapshots.remove(player.getUUID());
        }
    }

    private void send(ServerPlayer player, QuestSignalType type, String key, int amount) {
        QuestServices.engine(player.server).onSignal(QuestSignal.of(type, player, key, amount, player.blockPosition()));
    }

    private void pushInventorySnapshotDelta(ServerPlayer player) {
        Map<String, Integer> current = new HashMap<>();
        for (var stack : player.getInventory().items) {
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            current.merge(id.toString(), stack.getCount(), Integer::sum);
        }

        Map<String, Integer> previous = inventorySnapshots.getOrDefault(player.getUUID(), Map.of());
        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            int prev = previous.getOrDefault(entry.getKey(), 0);
            int delta = entry.getValue() - prev;
            if (delta > 0) {
                send(player, QuestSignalType.INVENTORY_CHANGED, entry.getKey(), delta);
            }
        }

        inventorySnapshots.put(player.getUUID(), current);
    }

    private void pushStatSnapshotDelta(ServerPlayer player) {
        var engine = QuestServices.engine(player.server);
        Map<String, Integer> current = new HashMap<>();
        for (String statKey : engine.trackedStatTaskTargets()) {
            current.put(statKey, QuestStatHelper.readStat(player, statKey));
        }

        Map<String, Integer> previous = statSnapshots.getOrDefault(player.getUUID(), Map.of());
        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            int prev = previous.getOrDefault(entry.getKey(), Integer.MIN_VALUE);
            if (entry.getValue() != prev) {
                send(player, QuestSignalType.STAT_CHANGE, entry.getKey(), Math.max(0, entry.getValue()));
            }
        }

        statSnapshots.put(player.getUUID(), current);
    }
}
