package com.abo47.questsandstuff.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class QuestHudOverlayRenderer {
    private QuestHudOverlayRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        resetGuiState(graphics);
        QuestCompletionNotificationOverlay.render(graphics);
        PinnedQuestHudOverlay.render(graphics);
        resetGuiState(graphics);
    }

    static void resetGuiState(GuiGraphics graphics) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }
}
