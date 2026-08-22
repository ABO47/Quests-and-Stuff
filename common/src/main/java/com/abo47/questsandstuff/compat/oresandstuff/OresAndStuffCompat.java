package com.abo47.questsandstuff.compat.oresandstuff;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class OresAndStuffCompat {
    private static final String BIO_SCAN_EVENTS = "com.abo47.oresandstuff.api.BioScanEvents";
    private static final String BIO_SCAN_EVENT = "com.abo47.oresandstuff.api.BioScanEvent";
    private static final String BIO_SCAN_API = "com.abo47.oresandstuff.api.BioScanApi";

    private static volatile boolean initialized;
    private static volatile Method grantScanMethod;
    private static volatile boolean available;
    private static volatile boolean availabilityChecked;

    private OresAndStuffCompat() {
    }

    public static boolean isAvailable() {
        if (!availabilityChecked) {
            available = classPresent(BIO_SCAN_EVENTS);
            availabilityChecked = true;
        }
        return available;
    }

    public static void init() {
        if (initialized || !isAvailable()) {
            return;
        }
        initialized = true;
        try {
            Method register = Class.forName(BIO_SCAN_EVENTS).getMethod("register", Consumer.class);
            Method playerAccessor = Class.forName(BIO_SCAN_EVENT).getMethod("player");
            Method entityIdAccessor = Class.forName(BIO_SCAN_EVENT).getMethod("entityId");
            resolveGrantScan();
            register.invoke(null, (Consumer<Object>) event ->
                    forward(event, playerAccessor, entityIdAccessor, OresAndStuffBioScanBridge::onScan));
            QuestsAndStuffMod.LOGGER.info("[QnS] Hooked Ores and Stuff bio scans");
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            QuestsAndStuffMod.LOGGER.warn("[QnS] Failed to hook Ores and Stuff bio scans", exception);
        }
    }

    static boolean grantTeamScan(ServerLevel level, UUID playerId, ResourceLocation entityId) {
        Method method = grantScanMethod;
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(null, level, playerId, entityId);
            return result instanceof Boolean granted && granted;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS] Failed granting team bio scan player={} entity={} diagnostic={}",
                    playerId,
                    entityId,
                    rootMessage(exception)
            );
            return false;
        }
    }

    private static void resolveGrantScan() throws ReflectiveOperationException {
        grantScanMethod = Class.forName(BIO_SCAN_API)
                .getMethod("grantScan", ServerLevel.class, UUID.class, ResourceLocation.class);
    }

    private static void forward(Object event, Method playerAccessor, Method entityIdAccessor,
                                BiConsumer<ServerPlayer, ResourceLocation> handler) {
        try {
            ServerPlayer player = (ServerPlayer) playerAccessor.invoke(event);
            ResourceLocation entityId = (ResourceLocation) entityIdAccessor.invoke(event);
            if (player != null && entityId != null) {
                handler.accept(player, entityId);
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS] Failed reading Ores and Stuff bio scan event diagnostic={}",
                    rootMessage(exception)
            );
        }
    }

    private static boolean classPresent(String className) {
        try {
            Class.forName(className, false, OresAndStuffCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName();
    }
}
