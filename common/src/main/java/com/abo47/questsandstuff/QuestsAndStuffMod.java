package com.abo47.questsandstuff;

import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.platform.Services;
import com.abo47.questsandstuff.quest.runtime.team.TeamProgressProviders;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public final class QuestsAndStuffMod {
    public static final String MODID = "questsandstuff";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer SERVER_REF;

    private static Supplier<Item> questTablet = unregistered("quest_tablet");
    private static Supplier<LootItemConditionType> completedQuestLootCondition = unregistered("completed_quest");
    private static boolean bootstrapped;

    public static final Supplier<Item> QUEST_TABLET = () -> questTablet.get();
    public static final Supplier<LootItemConditionType> COMPLETED_QUEST_LOOT_CONDITION = () -> completedQuestLootCondition.get();

    private QuestsAndStuffMod() {
    }

    public static void registerContent(Supplier<Item> questTabletSupplier, Supplier<LootItemConditionType> completedQuestLootConditionSupplier) {
        questTablet = Objects.requireNonNull(questTabletSupplier, "questTabletSupplier");
        completedQuestLootCondition = Objects.requireNonNull(completedQuestLootConditionSupplier, "completedQuestLootConditionSupplier");
    }

    public static synchronized void bootstrapCommon() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        QuestsAndStuffConfig.load();
        QuestNetwork.register();
        TeamProgressProviders.bootstrapDefaults();
    }

    public static void prepareAssetsDirectory() {
        try {
            Path assetsRoot = Services.platform().configDir().resolve(MODID).resolve("assets");
            Files.createDirectories(assetsRoot.resolve("pics"));
            Files.createDirectories(assetsRoot.resolve("sounds"));
        } catch (Exception e) {
            LOGGER.warn("Failed creating assets directory", e);
        }
    }

    public static void debugLog(String message, Object... args) {
        if (QuestsAndStuffConfig.debugLoggingEnabled()) {
            LOGGER.info(message, args);
        }
    }

    private static <T> Supplier<T> unregistered(String id) {
        return () -> {
            throw new IllegalStateException("Quests and Stuff content was requested before platform registration: " + id);
        };
    }
}
