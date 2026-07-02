package com.abo47.questsandstuff.quest.model.reward;

import com.abo47.questsandstuff.quest.runtime.reward.QuestRewardDelivery;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

public record LootTableQuestRewardDefinition(
        String id,
        ResourceLocation type,
        ResourceLocation lootTable,
        String title,
        String icon,
        boolean selectable
) implements QuestRewardDefinition {
    public LootTableQuestRewardDefinition(String id, ResourceLocation type, ResourceLocation lootTable, ResourceLocation fallbackItem, int amount) {
        this(id, type, lootTable, "", "", false);
    }

    public LootTableQuestRewardDefinition(String id, ResourceLocation type, ResourceLocation lootTable, String title, String icon) {
        this(id, type, lootTable, title, icon, false);
    }

    public LootTableQuestRewardDefinition(String id, ResourceLocation type, ResourceLocation lootTable, ResourceLocation fallbackItem, int amount, String title, String icon) {
        this(id, type, lootTable, title, icon, false);
    }

    public static Codec<LootTableQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(LootTableQuestRewardDefinition::id),
                ResourceLocation.CODEC.fieldOf("loot_table").forGetter(LootTableQuestRewardDefinition::lootTable),
                Codec.STRING.fieldOf("title").orElse("").forGetter(LootTableQuestRewardDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(LootTableQuestRewardDefinition::icon),
                Codec.BOOL.fieldOf("selectable").orElse(false).forGetter(LootTableQuestRewardDefinition::selectable)
        ).apply(instance, (id, lootTable, title, icon, selectable) -> new LootTableQuestRewardDefinition(id, type, lootTable, title, icon, selectable)));
    }

    public LootTableQuestRewardDefinition {
        title = title == null ? "" : title.trim();
        icon = icon == null ? "" : icon.trim();
    }

    @Override
    public void grant(ServerPlayer player) {
        LootParams params = new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSets.CHEST);
        LootTable table = player.server.getLootData().getLootTable(lootTable);
        List<ItemStack> generated = new ArrayList<>();
        if (table != LootTable.EMPTY) {
            table.getRandomItems(params, stack -> merge(generated, stack.copy()));
        }
        for (ItemStack stack : generated) {
            QuestRewardDelivery.giveItem(player, stack);
        }
    }

    private static void merge(List<ItemStack> stacks, ItemStack incoming) {
        for (ItemStack existing : stacks) {
            if (ItemStack.isSameItemSameTags(existing, incoming)) {
                existing.grow(incoming.getCount());
                return;
            }
        }
        stacks.add(incoming);
    }
}
