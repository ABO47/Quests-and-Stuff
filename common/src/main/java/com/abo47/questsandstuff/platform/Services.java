package com.abo47.questsandstuff.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class Services {
    private static volatile PlatformService platform = new FallbackPlatformService();

    private Services() {
    }

    public static PlatformService platform() {
        return platform;
    }

    public static void setPlatform(PlatformService service) {
        platform = Objects.requireNonNull(service, "service");
    }

    private static final class FallbackPlatformService implements PlatformService {
        @Override
        public Path configDir() {
            return Paths.get("config");
        }

        @Override
        public String loaderName() {
            return "unknown";
        }

        @Override
        public String loaderVersion() {
            return "unknown";
        }

        @Override
        public String modVersion(String modId) {
            return "unknown";
        }

        @Override
        public void registerNetwork() {
        }

        @Override
        public void sendToPlayer(Object packet, ServerPlayer player) {
        }

        @Override
        public void sendToServer(Object packet) {
        }

        @Override
        public void openQuestTabletUi(Player player) {
        }
    }
}
