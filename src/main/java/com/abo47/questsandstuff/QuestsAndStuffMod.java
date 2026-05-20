package com.abo47.questsandstuff;

import com.abo47.questsandstuff.command.QuestCommands;
import com.abo47.questsandstuff.item.QuestTabletItem;
import com.abo47.questsandstuff.loot.CompletedQuestLootCondition;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.quest.runtime.signal.ForgeQuestEventBridge;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(QuestsAndStuffMod.MODID)
public class QuestsAndStuffMod {
    public static final String MODID = "questsandstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static net.minecraft.server.MinecraftServer SERVER_REF;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> QUEST_TABLET = ITEMS.register("quest_tablet", () -> new QuestTabletItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<LootItemConditionType> COMPLETED_QUEST_LOOT_CONDITION = LOOT_CONDITION_TYPES.register(
            "completed_quest",
            () -> new LootItemConditionType(CompletedQuestLootCondition.SERIALIZER)
    );

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.questsandstuff.main"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> QUEST_TABLET.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(QUEST_TABLET.get());
            })
            .build());

    public QuestsAndStuffMod(FMLJavaModLoadingContext modLoadingContext) {
        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, QuestsAndStuffConfig.COMMON_SPEC);
        loadingContext.registerConfig(ModConfig.Type.CLIENT, QuestsAndStuffConfig.CLIENT_SPEC);

        IEventBus modBus = modLoadingContext.getModEventBus();
        ITEMS.register(modBus);
        LOOT_CONDITION_TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(new ForgeQuestEventBridge());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            QuestNetwork.register();
            TeamProgressProviders.bootstrapDefaults();
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QuestCommands.register(event.getDispatcher());
    }

    private void onServerStarted(ServerStartedEvent event) {
        SERVER_REF = event.getServer();
        try {
            Path assetsRoot = FMLPaths.CONFIGDIR.get().resolve(MODID).resolve("assets");
            Files.createDirectories(assetsRoot.resolve("pics"));
            Files.createDirectories(assetsRoot.resolve("sounds"));
        } catch (Exception e) {
            LOGGER.warn("Failed creating assets directory", e);
        }
        QuestServices.start(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        QuestServices.stop(event.getServer());
        if (SERVER_REF == event.getServer()) {
            SERVER_REF = null;
        }
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new com.abo47.questsandstuff.quest.persistence.quest.QuestServerReloadListener());
    }

    public static void debugLog(String message, Object... args) {
        if (QuestsAndStuffConfig.debugLoggingEnabled()) {
            LOGGER.info(message, args);
        }
    }
}
