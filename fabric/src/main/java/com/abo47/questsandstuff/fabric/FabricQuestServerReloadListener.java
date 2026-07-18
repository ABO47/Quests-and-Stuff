package com.abo47.questsandstuff.fabric;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.persistence.quest.QuestServerReloadListener;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;

public final class FabricQuestServerReloadListener implements IdentifiableResourceReloadListener {
    private final QuestServerReloadListener delegate = new QuestServerReloadListener();

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "quest_server_reload");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return IdentifiableResourceReloadListener.super.getFabricDependencies();
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller preparationsProfiler,
            ProfilerFiller reloadProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        return delegate.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
    }
}