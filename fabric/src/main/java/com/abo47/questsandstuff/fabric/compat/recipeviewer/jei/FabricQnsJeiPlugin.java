package com.abo47.questsandstuff.fabric.compat.recipeviewer.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.ResourceLocation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.compat.recipeviewer.ItemLockViewerSync;
import com.abo47.questsandstuff.client.compat.recipeviewer.LockViewerBridges;

@JeiPlugin
public final class FabricQnsJeiPlugin implements IModPlugin {
    private final FabricJeiGateBridge bridge = new FabricJeiGateBridge();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "lock_sync");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        bridge.onRuntimeAvailable(runtime);
        LockViewerBridges.setJei(bridge);
        ItemLockViewerSync.ensureSubscribed();
    }

    @Override
    public void onRuntimeUnavailable() {
        LockViewerBridges.clearJei();
        bridge.onRuntimeUnavailable();
    }
}
