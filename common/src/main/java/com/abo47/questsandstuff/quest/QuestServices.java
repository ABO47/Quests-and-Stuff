package com.abo47.questsandstuff.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.runtime.QuestRuntimeEngine;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.persistence.quest.QuestProgressSavedData;
import com.abo47.questsandstuff.quest.sync.QuestPerformanceTracker;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import net.minecraft.server.MinecraftServer;
import com.abo47.questsandstuff.platform.Services;

import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;

public final class QuestServices {
    private static final Map<MinecraftServer, Bundle> SERVICES = new WeakHashMap<>();

    private QuestServices() {
    }

    public static void start(MinecraftServer server) {
        stop(server);
        Path root = Services.platform().configDir().resolve(QuestsAndStuffMod.MODID);

        QuestDefinitionStore definitionStore = new QuestDefinitionStore(root);
        definitionStore.load();

        QuestProgressSavedData progressData = QuestProgressSavedData.get(server);
        QuestPerformanceTracker performanceTracker = new QuestPerformanceTracker();
        QuestSyncService syncService = new QuestSyncService(definitionStore, progressData, performanceTracker);
        QuestRuntimeEngine runtimeEngine = new QuestRuntimeEngine(definitionStore, progressData, syncService, performanceTracker);
        syncService.setVisibilityFilter(runtimeEngine::isVisibleFor);
        EditorSessionService editorService = new EditorSessionService(definitionStore, runtimeEngine, syncService);

        SERVICES.put(server, new Bundle(definitionStore, progressData, syncService, runtimeEngine, editorService, performanceTracker));
    }

    public static void stop(MinecraftServer server) {
        Bundle bundle = SERVICES.remove(server);
        if (bundle != null) {
            bundle.definitionStore().saveAll();
            bundle.definitionStore().shutdown();
        }
    }

    public static QuestRuntimeEngine engine(MinecraftServer server) {
        return bundle(server).runtimeEngine();
    }

    public static QuestSyncService sync(MinecraftServer server) {
        return bundle(server).syncService();
    }

    public static QuestDefinitionStore definitions(MinecraftServer server) {
        return bundle(server).definitionStore();
    }

    public static EditorSessionService editor(MinecraftServer server) {
        return bundle(server).editorService();
    }

    public static QuestPerformanceTracker perf(MinecraftServer server) {
        return bundle(server).performanceTracker();
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
            QuestSyncService syncService,
            QuestRuntimeEngine runtimeEngine,
            EditorSessionService editorService,
            QuestPerformanceTracker performanceTracker
    ) {
    }
}
