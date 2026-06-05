package com.abo47.questsandstuff.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;

public interface PlatformService {
    Path configDir();

    String loaderName();

    String loaderVersion();

    String modVersion(String modId);

    void registerNetwork();

    void sendToPlayer(Object packet, ServerPlayer player);

    void sendToServer(Object packet);

    void openTabletUi(Player player);
}
