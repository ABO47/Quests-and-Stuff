package com.abo47.questsandstuff.forge;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.command.QuestCommands;
import com.abo47.questsandstuff.forge.runtime.signal.ForgeQuestEventBridge;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.persistence.quest.QuestServerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuestsAndStuffMod.MODID)
public final class QuestsAndStuffForge {
    public QuestsAndStuffForge(FMLJavaModLoadingContext modLoadingContext) {
        Services.setPlatform(new ForgePlatformService());

        var modBus = modLoadingContext.getModEventBus();
        ForgeContent.register(modBus);
        modBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(new ForgeQuestEventBridge());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(QuestsAndStuffMod::bootstrapCommon);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QuestCommands.register(event.getDispatcher());
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new QuestServerReloadListener());
    }

    private void onServerStarted(ServerStartedEvent event) {
        QuestsAndStuffMod.SERVER_REF = event.getServer();
        QuestsAndStuffMod.prepareAssetsDirectory();
        QuestServices.start(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        QuestServices.stop(event.getServer());
        if (QuestsAndStuffMod.SERVER_REF == event.getServer()) {
            QuestsAndStuffMod.SERVER_REF = null;
        }
    }
}
