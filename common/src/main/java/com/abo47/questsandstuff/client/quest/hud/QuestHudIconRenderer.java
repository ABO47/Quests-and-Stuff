package com.abo47.questsandstuff.client.quest.hud;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconProvider;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

final class QuestHudIconRenderer {
    private QuestHudIconRenderer() {
    }

    static void draw(GuiGraphics graphics, String iconId, int x, int y, int size, int alpha) {
        String icon = iconId == null || iconId.isBlank() ? "minecraft:book" : iconId;
        IGuiTexture texture = UiIconAtlas.iconTexture(icon);
        if (texture == null) {
            if (ModelAssetPreviewRenderer.isModelAsset(icon)
                    && ModelAssetPreviewRenderer.renderModelAsset(graphics, x, y, size, size, icon)) {
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                return;
            }
            String entityId = EntityPreviewRenderer.entityId(icon);
            if (!entityId.isBlank()) {
                int yaw = EntityPreviewRenderer.entityYaw(icon);
                int spin = EntityPreviewRenderer.entitySpinSpeed(icon);
                if (EntityPreviewRenderer.renderEntityAsset(graphics, x, y, size, size, icon, yaw, spin, Minecraft.getInstance().getFrameTime())) {
                    graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    return;
                }
                icon = EntityPreviewRenderer.spawnEggIcon(entityId);
            }
            texture = DisplayIconProvider.iconTexture(icon);
        }
        float a = Math.max(0, Math.min(255, alpha)) / 255.0f;
        graphics.setColor(1.0f, 1.0f, 1.0f, a);
        texture.draw(graphics, 0, 0, x, y, size, size);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
