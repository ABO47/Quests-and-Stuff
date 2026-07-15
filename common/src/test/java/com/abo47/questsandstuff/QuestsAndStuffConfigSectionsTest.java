package com.abo47.questsandstuff;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestsAndStuffConfigSectionsTest {
    @Test
    void animationSectionRoundTripsIndependentSettings() {
        QuestsAndStuffConfigSections.Animations animations = new QuestsAndStuffConfigSections.Animations();
        JsonObject source = new JsonObject();
        source.addProperty("uiAnimations", false);
        source.addProperty("contextMenuAnimations", true);
        source.addProperty("toolsMenuAnimations", false);
        source.addProperty("minimapAnimations", false);
        source.addProperty("questWindowAnimations", true);
        source.addProperty("popupWindowAnimations", false);
        source.addProperty("connectionAnimations", true);
        source.addProperty("chapterSwitchAnimations", false);

        animations.read(source);
        JsonObject written = animations.write();

        assertFalse(written.get("uiAnimations").getAsBoolean());
        assertTrue(written.get("contextMenuAnimations").getAsBoolean());
        assertFalse(written.get("toolsMenuAnimations").getAsBoolean());
        assertFalse(written.get("minimapAnimations").getAsBoolean());
        assertTrue(written.get("questWindowAnimations").getAsBoolean());
        assertFalse(written.get("popupWindowAnimations").getAsBoolean());
        assertTrue(written.get("connectionAnimations").getAsBoolean());
        assertFalse(written.get("chapterSwitchAnimations").getAsBoolean());
    }

    @Test
    void canvasRewardsHudAndSecuritySectionsOwnTheirKeys() {
        QuestsAndStuffConfigSections.Canvas canvas = new QuestsAndStuffConfigSections.Canvas();
        JsonObject canvasSource = new JsonObject();
        canvasSource.addProperty("fullScreenMode", true);
        canvasSource.addProperty("minimap", false);
        canvasSource.addProperty("visualMinimap", true);
        canvasSource.addProperty("readOnlyCanvasFocus", false);
        canvasSource.addProperty("questEffectIcons", true);
        canvasSource.addProperty("miniNotifications", true);

        canvas.read(canvasSource);

        assertTrue(canvas.write().get("fullScreenMode").getAsBoolean());
        assertFalse(canvas.write().get("minimap").getAsBoolean());
        assertTrue(canvas.write().get("visualMinimap").getAsBoolean());
        assertFalse(canvas.write().get("readOnlyCanvasFocus").getAsBoolean());
        assertTrue(canvas.write().get("questEffectIcons").getAsBoolean());
        assertTrue(canvas.write().get("miniNotifications").getAsBoolean());

        QuestsAndStuffConfigSections.Rewards rewards = new QuestsAndStuffConfigSections.Rewards();
        JsonObject rewardSource = new JsonObject();
        rewardSource.addProperty("autoClaimRewards", true);
        rewards.read(rewardSource);
        assertTrue(rewards.write().get("autoClaimRewards").getAsBoolean());

        QuestsAndStuffConfigSections.Security security = new QuestsAndStuffConfigSections.Security();
        JsonObject securitySource = new JsonObject();
        securitySource.addProperty("commandRewards", false);
        security.read(securitySource);
        assertFalse(security.write().get("commandRewards").getAsBoolean());
    }

    @Test
    void chunkClaimsSectionOwnsProtectionFlagsAndCaps() {
        QuestsAndStuffConfigSections.ChunkClaims chunkClaims = new QuestsAndStuffConfigSections.ChunkClaims();
        JsonObject source = new JsonObject();
        source.addProperty("protectBreakPlace", false);
        source.addProperty("protectInteraction", false);
        source.addProperty("protectExplosions", false);
        source.addProperty("protectMobGriefing", false);
        source.addProperty("protectPvp", false);
        source.addProperty("maxClaimedChunks", 10);
        source.addProperty("maxForceLoadedChunks", 3);

        chunkClaims.read(source);
        JsonObject written = chunkClaims.write();

        assertFalse(written.get("protectBreakPlace").getAsBoolean());
        assertFalse(written.get("protectInteraction").getAsBoolean());
        assertFalse(written.get("protectExplosions").getAsBoolean());
        assertFalse(written.get("protectMobGriefing").getAsBoolean());
        assertFalse(written.get("protectPvp").getAsBoolean());
        assertEquals(10, written.get("maxClaimedChunks").getAsInt());
        assertEquals(3, written.get("maxForceLoadedChunks").getAsInt());
        assertEquals(QuestsAndStuffConfigSections.ChunkClaims.DEFAULT_MAX_CLAIMED, 100);
        assertEquals(QuestsAndStuffConfigSections.ChunkClaims.DEFAULT_MAX_FORCE_LOADED, 100);
        assertEquals(0, QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(-5));
        assertEquals(100000, QuestsAndStuffConfigSections.ChunkClaims.normalizeCap(999999));
    }

    @Test
    void hudSectionClampsDuration() {
        QuestsAndStuffConfigSections.Hud hud = new QuestsAndStuffConfigSections.Hud();
        JsonObject source = new JsonObject();
        source.addProperty("completionHud", false);
        source.addProperty("completionHudSound", false);
        source.addProperty("completionHudDurationMs", 120000);

        hud.read(source);
        JsonObject written = hud.write();

        assertFalse(written.get("completionHud").getAsBoolean());
        assertFalse(written.get("completionHudSound").getAsBoolean());
        assertEquals(QuestsAndStuffConfig.MAX_COMPLETION_HUD_DURATION_MS, written.get("completionHudDurationMs").getAsInt());
        assertEquals(0, QuestsAndStuffConfig.normalizeCompletionHudDurationMs(-50));
    }
}
