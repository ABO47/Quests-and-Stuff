package com.abo47.questsandstuff.quest.model.reward;

import com.abo47.questsandstuff.quest.runtime.signal.QuestItemMatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ItemQuestRewardDefinition(
        String id,
        ResourceLocation type,
        ResourceLocation item,
        int amount,
        String nbt,
        String title,
        String icon,
        boolean selectable
) implements QuestRewardDefinition {
    public ItemQuestRewardDefinition(String id, ResourceLocation type, ResourceLocation item, int amount, String nbt) {
        this(id, type, item, amount, nbt, "", "", false);
    }

    public ItemQuestRewardDefinition(String id, ResourceLocation type, ResourceLocation item, int amount, String nbt, String title, String icon) {
        this(id, type, item, amount, nbt, title, icon, false);
    }

    public static Codec<ItemQuestRewardDefinition> codec(ResourceLocation type) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(ItemQuestRewardDefinition::id),
                ResourceLocation.CODEC.fieldOf("item").forGetter(ItemQuestRewardDefinition::item),
                Codec.INT.fieldOf("amount").orElse(1).forGetter(ItemQuestRewardDefinition::amount),
                Codec.STRING.fieldOf("nbt").orElse("").forGetter(ItemQuestRewardDefinition::nbt),
                Codec.STRING.fieldOf("title").orElse("").forGetter(ItemQuestRewardDefinition::title),
                Codec.STRING.fieldOf("icon").orElse("").forGetter(ItemQuestRewardDefinition::icon),
                Codec.BOOL.fieldOf("selectable").orElse(false).forGetter(ItemQuestRewardDefinition::selectable)
        ).apply(instance, (id, item, amount, nbt, title, icon, selectable) -> new ItemQuestRewardDefinition(id, type, item, amount, nbt, title, icon, selectable)));
    }

    public ItemQuestRewardDefinition {
        nbt = nbt == null ? "" : nbt.trim();
        title = title == null ? "" : title.trim();
        icon = icon == null ? "" : icon.trim();
    }

    @Override
    public void grant(ServerPlayer player) {
        Item resolved = BuiltInRegistries.ITEM.get(item);
        if (resolved == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(resolved, safeAmount());
        QuestItemMatcher.applyNbt(stack, nbt);
        QuestRewardDelivery.giveItem(player, stack);
    }
}
