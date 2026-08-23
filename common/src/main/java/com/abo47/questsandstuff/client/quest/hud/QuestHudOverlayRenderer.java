package com.abo47.questsandstuff.client.quest.hud;

import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

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
        clearDepthBuffer(graphics);
        QuestCompletionNotificationOverlay.render(graphics);
        PinnedQuestHudOverlay.render(graphics);
        clearDepthBuffer(graphics);
        resetGuiState(graphics);
    }

    private static void clearDepthBuffer(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.disableScissor();
        RenderSystem.depthMask(true);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

    static void resetGuiState(GuiGraphics graphics) {
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}
