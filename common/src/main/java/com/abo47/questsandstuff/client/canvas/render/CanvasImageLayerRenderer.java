package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasImageLayerRenderer {
    private CanvasImageLayerRenderer() {
    }

    public static void draw(GuiGraphics graphics, int mouseX, int mouseY, CanvasImageLayer image, int x, int y, int width, int height, int pivotX, int pivotY) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + pivotX, y + pivotY, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(image.rotation())));
        drawContent(graphics, mouseX, mouseY, image, width, height, pivotX, pivotY);
        graphics.pose().popPose();
    }

    private static void drawContent(GuiGraphics graphics, int mouseX, int mouseY, CanvasImageLayer image, int width, int height, int pivotX, int pivotY) {
        String asset = image.asset();
        String entityId = EntityPreviewRenderer.entityId(asset);
        if (!entityId.isBlank()) {
            if (!EntityPreviewRenderer.renderEntityAssetAtCenter(graphics, 0, 0, width, height, asset, image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), 0.0F)) {
                drawFallback(graphics, width, height, pivotX, pivotY);
            }
            return;
        }
        if (CanvasModelPreviewRenderer.isBlockModelAsset(asset)) {
            if (!CanvasModelPreviewRenderer.renderBlockModelAssetAtCenter(graphics, 0, 0, width, height, asset, image.entityYaw(), image.modelPitch())) {
                drawFallback(graphics, width, height, pivotX, pivotY);
            }
            return;
        }
        if (CanvasModelPreviewRenderer.isModelAsset(asset)) {
            if (!CanvasModelPreviewRenderer.renderModelAsset(graphics, -pivotX, -pivotY, width, height, asset, image.entityYaw(), image.modelPitch())) {
                drawFallback(graphics, width, height, pivotX, pivotY);
            }
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(asset);
        if (texture == null) {
            drawFallback(graphics, width, height, pivotX, pivotY);
            return;
        }
        texture.draw(graphics, mouseX, mouseY, -pivotX, -pivotY, width, height);
    }

    private static void drawFallback(GuiGraphics graphics, int width, int height, int pivotX, int pivotY) {
        graphics.fill(-pivotX, -pivotY, -pivotX + width, -pivotY + height, withAlpha(ModColors.TEXT_MUTED, 45));
    }
}
