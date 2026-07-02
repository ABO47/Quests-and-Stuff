package com.abo47.questsandstuff.quest.persistence.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class QuestServerReloadListener implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(
            PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller preparationsProfiler,
            ProfilerFiller reloadProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> Unit.INSTANCE, backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(ignored -> {
                    if (QuestsAndStuffMod.SERVER_REF == null) {
                        return;
                    }
                    QuestServiceRegistry.definitions(QuestsAndStuffMod.SERVER_REF).saveAll();
                    QuestServiceRegistry.definitions(QuestsAndStuffMod.SERVER_REF).load();
                    QuestServiceRegistry.engine(QuestsAndStuffMod.SERVER_REF).rebuildIndex();
                    var players = QuestsAndStuffMod.SERVER_REF.getPlayerList().getPlayers();
                    QuestServiceRegistry.engine(QuestsAndStuffMod.SERVER_REF).preparePlayersForFullSync(players);
                    QuestServiceRegistry.sync(QuestsAndStuffMod.SERVER_REF).syncFull(players);
                }, gameExecutor);
    }
}
