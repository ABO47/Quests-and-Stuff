package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.command.QuestCommands;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class QuestsAndStuffFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Services.setPlatform(new FabricPlatformService());
        FabricContent.register();
        QuestsAndStuffMod.bootstrapCommon();
        FabricQuestEventBridge.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> QuestCommands.register(dispatcher));
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricQuestServerReloadListener());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            QuestsAndStuffMod.SERVER_REF = server;
            QuestsAndStuffMod.prepareAssetsDirectory();
            QuestServiceRegistry.start(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            QuestServiceRegistry.stop(server);
            if (QuestsAndStuffMod.SERVER_REF == server) {
                QuestsAndStuffMod.SERVER_REF = null;
            }
        });
    }
}