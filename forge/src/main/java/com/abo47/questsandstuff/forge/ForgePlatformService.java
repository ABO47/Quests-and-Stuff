package com.abo47.questsandstuff.forge;

import com.abo47.questsandstuff.client.tablet.shell.TabletClientHooks;
import com.abo47.questsandstuff.platform.PlatformService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.nio.file.Path;

public final class ForgePlatformService implements PlatformService {
    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String loaderName() {
        return "forge";
    }

    @Override
    public String loaderVersion() {
        String version = ForgeVersion.getVersion();
        return version == null || version.isBlank() ? "unknown" : version;
    }

    @Override
    public String modVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .filter(version -> !version.isBlank())
                .orElse("unknown");
    }

    @Override
    public void registerNetwork() {
        ForgeQuestNetwork.register();
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        ForgeQuestNetwork.sendToPlayer(packet, player);
    }

    @Override
    public void sendToServer(Object packet) {
        ForgeQuestNetwork.sendToServer(packet);
    }

    @Override
    public void openTabletUi(Player player) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TabletClientHooks.openTabletUiFromItem(player));
    }
}
