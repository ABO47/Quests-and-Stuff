package com.abo47.questsandstuff.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.PerformanceTracker;
import com.abo47.questsandstuff.quest.sync.SyncService;
import com.abo47.questsandstuff.chunkclaim.ChunkClaimService;
import net.minecraft.server.MinecraftServer;
import com.abo47.questsandstuff.platform.Services;

import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;

public final class QuestServiceRegistry {
    private static final Map<MinecraftServer, Bundle> SERVICES = new WeakHashMap<>();

    private QuestServiceRegistry() {
    }

    public static void start(MinecraftServer server) {
        stop(server);
        Path root = Services.platform().configDir().resolve(QuestsAndStuffMod.MODID);

        QuestDefinitionStore definitionStore = new QuestDefinitionStore(root);
        definitionStore.load();

        QuestProgressSavedData progressData = QuestProgressSavedData.get(server);
        PerformanceTracker performanceTracker = new PerformanceTracker();
        SyncService syncService = new SyncService(definitionStore, progressData, performanceTracker);
        RuntimeEngine runtimeEngine = new RuntimeEngine(definitionStore, progressData, syncService, performanceTracker);
        syncService.setVisibilityFilter(runtimeEngine::isVisibleFor);
        EditorSessionService editorService = new EditorSessionService(definitionStore, runtimeEngine, syncService);
        ChunkClaimService chunkClaimService = new ChunkClaimService(server);

        SERVICES.put(server, new Bundle(definitionStore, progressData, syncService, runtimeEngine, editorService, performanceTracker, chunkClaimService));

        chunkClaimService.applyAllForceLoads();
    }

    public static void stop(MinecraftServer server) {
        Bundle bundle = SERVICES.remove(server);
        if (bundle != null) {
            bundle.definitionStore().saveAll();
            bundle.definitionStore().shutdown();
        }
    }

    public static RuntimeEngine engine(MinecraftServer server) {
        return bundle(server).runtimeEngine();
    }

    public static SyncService sync(MinecraftServer server) {
        return bundle(server).syncService();
    }

    public static QuestDefinitionStore definitions(MinecraftServer server) {
        return bundle(server).definitionStore();
    }

    public static EditorSessionService editor(MinecraftServer server) {
        return bundle(server).editorService();
    }

    public static PerformanceTracker perf(MinecraftServer server) {
        return bundle(server).performanceTracker();
    }

    public static ChunkClaimService chunkClaims(MinecraftServer server) {
        return bundle(server).chunkClaimService();
    }

    private static Bundle bundle(MinecraftServer server) {
        Bundle bundle = SERVICES.get(server);
        if (bundle == null) {
            throw new IllegalStateException("Quest services were not initialized for this server yet");
        }
        return bundle;
    }

    private record Bundle(
            QuestDefinitionStore definitionStore,
            QuestProgressSavedData progressData,
            SyncService syncService,
            RuntimeEngine runtimeEngine,
            EditorSessionService editorService,
            PerformanceTracker performanceTracker,
            ChunkClaimService chunkClaimService
    ) {
    }
}
