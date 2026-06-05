package com.abo47.questsandstuff.fabric;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.platform.PlatformService;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.nio.file.Path;

public final class FabricPlatformService implements PlatformService {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public String loaderVersion() {
        return modVersion("fabricloader");
    }

    @Override
    public String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .filter(version -> !version.isBlank())
                .orElse("unknown");
    }

    @Override
    public void registerNetwork() {
        FabricQuestNetwork.register();
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        FabricQuestNetwork.sendToPlayer(packet, player);
    }

    @Override
    public void sendToServer(Object packet) {
        FabricQuestNetwork.sendToServer(packet);
    }

    @Override
    public void openTabletUi(Player player) {
        try {
            Class<?> hooks = Class.forName("com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks");
            Method method = hooks.getMethod("openTabletUiFromItem", Player.class);
            method.invoke(null, player);
        } catch (ReflectiveOperationException e) {
            QuestsAndStuffMod.LOGGER.warn("Failed to open tablet UI on Fabric", e);
        }
    }
}
