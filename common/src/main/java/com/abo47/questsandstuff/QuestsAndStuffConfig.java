package com.abo47.questsandstuff;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.abo47.questsandstuff.platform.Services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class QuestsAndStuffConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final int DEFAULT_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.DEFAULT_DURATION_MS;
    public static final int MIN_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.MIN_DURATION_MS;
    public static final int MAX_COMPLETION_HUD_DURATION_MS = QuestsAndStuffConfigSections.Hud.MAX_DURATION_MS;

    private static boolean loaded;
    private static final QuestsAndStuffConfigSections.Debug DEBUG = new QuestsAndStuffConfigSections.Debug();
    private static final QuestsAndStuffConfigSections.Animations ANIMATIONS = new QuestsAndStuffConfigSections.Animations();
    private static final QuestsAndStuffConfigSections.Canvas CANVAS = new QuestsAndStuffConfigSections.Canvas();
    private static final QuestsAndStuffConfigSections.Rewards REWARDS = new QuestsAndStuffConfigSections.Rewards();
    private static final QuestsAndStuffConfigSections.Hud HUD = new QuestsAndStuffConfigSections.Hud();
    private static final QuestsAndStuffConfigSections.Security SECURITY = new QuestsAndStuffConfigSections.Security();
    private static final QuestsAndStuffConfigSections.ChunkClaims CHUNK_CLAIMS = new QuestsAndStuffConfigSections.ChunkClaims();

    private QuestsAndStuffConfig() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = configFile();
        if (Files.isRegularFile(file)) {
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                if (parsed.isJsonObject()) {
                    read(parsed.getAsJsonObject());
                }
            } catch (Exception e) {
                QuestsAndStuffMod.LOGGER.warn("Failed reading Quests and Stuff config {}, keeping defaults", file, e);
            }
        }
        save();
    }

    public static boolean debugLoggingEnabled() {
        load();
        return DEBUG.debugLogging;
    }

    public static void setDebugLoggingEnabled(boolean enabled) {
        load();
        if (DEBUG.debugLogging != enabled) {
            DEBUG.debugLogging = enabled;
            save();
        }
    }

    public static boolean uiAnimationsEnabled() {
        load();
        return ANIMATIONS.ui;
    }

    public static void setUiAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.ui != enabled) {
            ANIMATIONS.ui = enabled;
            save();
        }
    }

    public static boolean contextMenuAnimationSettingEnabled() {
        load();
        return ANIMATIONS.contextMenu;
    }

    public static boolean contextMenuAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.contextMenu;
    }

    public static void setContextMenuAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.contextMenu != enabled) {
            ANIMATIONS.contextMenu = enabled;
            save();
        }
    }

    public static boolean toolsMenuAnimationSettingEnabled() {
        load();
        return ANIMATIONS.toolsMenu;
    }

    public static boolean toolsMenuAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.toolsMenu;
    }

    public static void setToolsMenuAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.toolsMenu != enabled) {
            ANIMATIONS.toolsMenu = enabled;
            save();
        }
    }

    public static boolean minimapAnimationSettingEnabled() {
        load();
        return ANIMATIONS.minimap;
    }

    public static boolean minimapAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.minimap;
    }

    public static void setMinimapAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.minimap != enabled) {
            ANIMATIONS.minimap = enabled;
            save();
        }
    }

    public static boolean questWindowAnimationSettingEnabled() {
        load();
        return ANIMATIONS.questWindow;
    }

    public static boolean questWindowAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.questWindow;
    }

    public static void setQuestWindowAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.questWindow != enabled) {
            ANIMATIONS.questWindow = enabled;
            save();
        }
    }

    public static boolean popupWindowAnimationSettingEnabled() {
        load();
        return ANIMATIONS.popupWindow;
    }

    public static boolean popupWindowAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.popupWindow;
    }

    public static void setPopupWindowAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.popupWindow != enabled) {
            ANIMATIONS.popupWindow = enabled;
            save();
        }
    }

    public static boolean connectionAnimationSettingEnabled() {
        load();
        return ANIMATIONS.connection;
    }

    public static boolean connectionAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.connection;
    }

    public static void setConnectionAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.connection != enabled) {
            ANIMATIONS.connection = enabled;
            save();
        }
    }

    public static boolean chapterSwitchAnimationSettingEnabled() {
        load();
        return ANIMATIONS.chapterSwitch;
    }

    public static boolean chapterSwitchAnimationsEnabled() {
        load();
        return ANIMATIONS.ui && ANIMATIONS.chapterSwitch;
    }

    public static void setChapterSwitchAnimationsEnabled(boolean enabled) {
        load();
        if (ANIMATIONS.chapterSwitch != enabled) {
            ANIMATIONS.chapterSwitch = enabled;
            save();
        }
    }

    public static boolean fullScreenModeEnabled() {
        load();
        return CANVAS.fullScreenMode;
    }

    public static void setFullScreenModeEnabled(boolean enabled) {
        load();
        if (CANVAS.fullScreenMode != enabled) {
            CANVAS.fullScreenMode = enabled;
            save();
        }
    }

    public static boolean minimapEnabled() {
        load();
        return CANVAS.minimap;
    }

    public static void setMinimapEnabled(boolean enabled) {
        load();
        if (CANVAS.minimap != enabled) {
            CANVAS.minimap = enabled;
            save();
        }
    }

    public static boolean visualMinimapEnabled() {
        load();
        return CANVAS.visualMinimap;
    }

    public static void setVisualMinimapEnabled(boolean enabled) {
        load();
        if (CANVAS.visualMinimap != enabled) {
            CANVAS.visualMinimap = enabled;
            save();
        }
    }

    public static boolean readOnlyCanvasFocusEnabled() {
        load();
        return CANVAS.readOnlyFocus;
    }

    public static void setReadOnlyCanvasFocusEnabled(boolean enabled) {
        load();
        if (CANVAS.readOnlyFocus != enabled) {
            CANVAS.readOnlyFocus = enabled;
            save();
        }
    }

    public static boolean questEffectIconsEnabled() {
        load();
        return CANVAS.questEffectIcons;
    }

    public static void setQuestEffectIconsEnabled(boolean enabled) {
        load();
        if (CANVAS.questEffectIcons != enabled) {
            CANVAS.questEffectIcons = enabled;
            save();
        }
    }

    public static boolean canvasMiniNotificationsEnabled() {
        load();
        return CANVAS.miniNotifications;
    }

    public static void setCanvasMiniNotificationsEnabled(boolean enabled) {
        load();
        if (CANVAS.miniNotifications != enabled) {
            CANVAS.miniNotifications = enabled;
            save();
        }
    }

    public static boolean autoClaimRewardsEnabled() {
        load();
        return REWARDS.autoClaim;
    }

    public static void setAutoClaimRewardsEnabled(boolean enabled) {
        load();
        if (REWARDS.autoClaim != enabled) {
            REWARDS.autoClaim = enabled;
            save();
        }
    }

    public static boolean commandRewardsEnabled() {
        load();
        return SECURITY.commandRewards;
    }

    public static void setCommandRewardsEnabled(boolean enabled) {
        load();
        if (SECURITY.commandRewards != enabled) {
            SECURITY.commandRewards = enabled;
            save();
        }
    }

    public static boolean completionHudEnabled() {
        load();
        return HUD.enabled;
    }

    public static void setCompletionHudEnabled(boolean enabled) {
        load();
        if (HUD.enabled != enabled) {
            HUD.enabled = enabled;
            save();
        }
    }

    public static boolean completionHudSoundEnabled() {
        load();
        return HUD.sound;
    }

    public static void setCompletionHudSoundEnabled(boolean enabled) {
        load();
        if (HUD.sound != enabled) {
            HUD.sound = enabled;
            save();
        }
    }

    public static int completionHudDurationMs() {
        load();
        return HUD.durationMs;
    }

    public static void setCompletionHudDurationMs(int durationMs) {
        load();
        int normalized = normalizeCompletionHudDurationMs(durationMs);
        if (HUD.durationMs != normalized) {
            HUD.durationMs = normalized;
            save();
        }
    }

    public static int normalizeCompletionHudDurationMs(int durationMs) {
        return QuestsAndStuffConfigSections.Hud.normalizeDurationMs(durationMs);
    }

    public static boolean chunkClaimProtectBreakPlace() {
        load();
        return CHUNK_CLAIMS.protectBreakPlace;
    }

    public static boolean chunkClaimProtectInteraction() {
        load();
        return CHUNK_CLAIMS.protectInteraction;
    }

    public static boolean chunkClaimProtectExplosions() {
        load();
        return CHUNK_CLAIMS.protectExplosions;
    }

    public static boolean chunkClaimProtectMobGriefing() {
        load();
        return CHUNK_CLAIMS.protectMobGriefing;
    }

    public static boolean chunkClaimProtectPvp() {
        load();
        return CHUNK_CLAIMS.protectPvp;
    }

    public static boolean chunkClaimProtectFire() {
        load();
        return CHUNK_CLAIMS.protectFire;
    }

    public static int chunkClaimMaxClaimedChunks() {
        load();
        return CHUNK_CLAIMS.maxClaimedChunks;
    }

    public static int chunkClaimMaxForceLoadedChunks() {
        load();
        return CHUNK_CLAIMS.maxForceLoadedChunks;
    }

    public static int minChunkClaimCap() {
        return QuestsAndStuffConfigSections.ChunkClaims.MIN_CAP;
    }

    public static int maxChunkClaimCap() {
        return QuestsAndStuffConfigSections.ChunkClaims.MAX_CAP;
    }

    public static boolean setChunkClaimProtectBreakPlace(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectBreakPlace != value) {
            CHUNK_CLAIMS.protectBreakPlace = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimProtectInteraction(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectInteraction != value) {
            CHUNK_CLAIMS.protectInteraction = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimProtectExplosions(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectExplosions != value) {
            CHUNK_CLAIMS.protectExplosions = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimProtectMobGriefing(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectMobGriefing != value) {
            CHUNK_CLAIMS.protectMobGriefing = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimProtectPvp(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectPvp != value) {
            CHUNK_CLAIMS.protectPvp = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimProtectFire(boolean value) {
        load();
        if (CHUNK_CLAIMS.protectFire != value) {
            CHUNK_CLAIMS.protectFire = value;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimMaxClaimedChunks(int value) {
        load();
        int normalized = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(value);
        if (CHUNK_CLAIMS.maxClaimedChunks != normalized) {
            CHUNK_CLAIMS.maxClaimedChunks = normalized;
            save();
        }
        return true;
    }

    public static boolean setChunkClaimMaxForceLoadedChunks(int value) {
        load();
        int normalized = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(value);
        if (CHUNK_CLAIMS.maxForceLoadedChunks != normalized) {
            CHUNK_CLAIMS.maxForceLoadedChunks = normalized;
            save();
        }
        return true;
    }

    public static boolean updateChunkClaims(boolean protectBreakPlace, boolean protectInteraction,
                                            boolean protectExplosions, boolean protectMobGriefing, boolean protectPvp,
                                            boolean protectFire,
                                            int maxClaimedChunks, int maxForceLoadedChunks) {
        load();
        CHUNK_CLAIMS.protectBreakPlace = protectBreakPlace;
        CHUNK_CLAIMS.protectInteraction = protectInteraction;
        CHUNK_CLAIMS.protectExplosions = protectExplosions;
        CHUNK_CLAIMS.protectMobGriefing = protectMobGriefing;
        CHUNK_CLAIMS.protectPvp = protectPvp;
        CHUNK_CLAIMS.protectFire = protectFire;
        CHUNK_CLAIMS.maxClaimedChunks = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(maxClaimedChunks);
        CHUNK_CLAIMS.maxForceLoadedChunks = QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(maxForceLoadedChunks);
        save();
        return true;
    }

    private static void read(JsonObject root) {
        DEBUG.read(QuestsAndStuffConfigSections.object(root, "debug"));
        ANIMATIONS.read(QuestsAndStuffConfigSections.object(root, "animations"));
        CANVAS.read(QuestsAndStuffConfigSections.object(root, "canvas"));
        REWARDS.read(QuestsAndStuffConfigSections.object(root, "rewards"));
        HUD.read(QuestsAndStuffConfigSections.object(root, "hud"));
        SECURITY.read(QuestsAndStuffConfigSections.object(root, "security"));
        CHUNK_CLAIMS.read(QuestsAndStuffConfigSections.object(root, "chunkClaims"));
    }

    private static synchronized void save() {
        JsonObject root = new JsonObject();
        root.add("debug", DEBUG.write());
        root.add("animations", ANIMATIONS.write());
        root.add("canvas", CANVAS.write());
        root.add("rewards", REWARDS.write());
        root.add("hud", HUD.write());
        root.add("security", SECURITY.write());
        root.add("chunkClaims", CHUNK_CLAIMS.write());

        Path file = configFile();
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            QuestsAndStuffMod.LOGGER.warn("Failed writing Quests and Stuff config {}", file, e);
        }
    }

    private static Path configFile() {
        return Services.platform().configDir().resolve(QuestsAndStuffMod.MODID).resolve("config.json");
    }
}
