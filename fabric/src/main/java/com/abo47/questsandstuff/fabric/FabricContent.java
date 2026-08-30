package com.abo47.questsandstuff.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.item.TabletItem;
import com.abo47.questsandstuff.loot.CompletedQuestLootCondition;
import com.abo47.questsandstuff.quest.runtime.lock.LockedRecipeSerializer;

public final class FabricContent {
    public static Item TABLET;
    public static LootItemConditionType COMPLETED_QUEST_LOOT_CONDITION;
    public static CreativeModeTab MAIN_TAB;

    private FabricContent() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                LockedRecipeSerializer.ID,
                LockedRecipeSerializer.INSTANCE
        );
        TABLET = Registry.register(
                BuiltInRegistries.ITEM,
                id("quest_tablet"),
                new TabletItem(new Item.Properties().stacksTo(1))
        );
        COMPLETED_QUEST_LOOT_CONDITION = Registry.register(
                BuiltInRegistries.LOOT_CONDITION_TYPE,
                id("completed_quest"),
                new LootItemConditionType(CompletedQuestLootCondition.SERIALIZER)
        );
        QuestsAndStuffMod.registerContent(() -> TABLET, () -> COMPLETED_QUEST_LOOT_CONDITION);

        Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id("ui_click"),
                SoundEvent.createVariableRangeEvent(id("ui_click"))
        );

        MAIN_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                id("main"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.questsandstuff.main"))
                        .icon(() -> TABLET.getDefaultInstance())
                        .displayItems((parameters, output) -> output.accept(TABLET))
                        .build()
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, path);
    }
}
