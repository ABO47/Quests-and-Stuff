package com.abo47.questsandstuff.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.item.TabletItem;
import com.abo47.questsandstuff.loot.CompletedQuestLootCondition;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeContent {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, QuestsAndStuffMod.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, QuestsAndStuffMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuestsAndStuffMod.MODID);

    public static final RegistryObject<Item> TABLET = ITEMS.register("quest_tablet", () -> new TabletItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<LootItemConditionType> COMPLETED_QUEST_LOOT_CONDITION = LOOT_CONDITION_TYPES.register(
            "completed_quest",
            () -> new LootItemConditionType(CompletedQuestLootCondition.SERIALIZER)
    );
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.questsandstuff.main"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> TABLET.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(TABLET.get()))
            .build());

    private ForgeContent() {
    }

    public static void register(IEventBus modBus) {
        QuestsAndStuffMod.registerContent(TABLET, COMPLETED_QUEST_LOOT_CONDITION);
        ITEMS.register(modBus);
        LOOT_CONDITION_TYPES.register(modBus);
        TABS.register(modBus);
    }
}
