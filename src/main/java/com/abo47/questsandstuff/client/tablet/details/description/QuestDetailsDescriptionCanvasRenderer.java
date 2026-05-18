package com.abo47.questsandstuff.client.tablet.details.description;


import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel.ORDER_IMAGE;
import static com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionModel.ORDER_TEXT;

public final class QuestDetailsDescriptionCanvasRenderer {
    private QuestDetailsDescriptionCanvasRenderer() {
    }

    public static void drawContent(GuiGraphics graphics, TabletUiState state, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        drawDescriptionBackground(graphics, model, contentX, contentY, contentW, contentH);
        drawGrid(graphics, state, model, contentX, contentY, contentW, contentH);
        drawElements(graphics, state, model, contentX, contentY, contentW, contentH);
        drawGuides(graphics, state, contentX, contentY, contentW, contentH);
    }

    private static void drawGrid(GuiGraphics graphics, TabletUiState state, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        if (!model.gridEnabled || !state.questDetailsEditMode || !state.canEdit) {
            return;
        }
        int cell = CanvasGeometry.gridSize(state);
        int alpha = Math.max(20, Math.min(220, (255 * Math.max(0, Math.min(100, model.gridOpacityPercent))) / 100));
        int color = (alpha << 24) | (ModColors.TEXT_PRIMARY & 0x00FFFFFF);
        int spanW = contentW;
        int spanH = contentH;
        int paintW = spanW + 1;
        int paintH = spanH + 1;
        for (int x = 0; x <= spanW; x += cell) {
            graphics.fill(contentX + x, contentY, contentX + x + 1, contentY + paintH, color);
        }
        int offset = Math.floorMod(-state.questDetailsDescScroll, cell);
        for (int y = offset; y <= spanH; y += cell) {
            graphics.fill(contentX, contentY + y, contentX + paintW, contentY + y + 1, color);
        }
        graphics.fill(contentX + spanW, contentY, contentX + spanW + 1, contentY + paintH, color);
    }

    private static void drawDescriptionBackground(GuiGraphics graphics, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        IGuiTexture texture = chapterBackgroundTexture(model.canvasBackground);
        if (texture == null) {
            return;
        }
        int paintW = contentW + 1;
        int paintH = contentH + 1;
        int bgOpacity = Math.max(0, Math.min(100, model.canvasBgOpacityPercent));
        int alpha = bgOpacity >= 100 ? 255 : Math.max(0, Math.min(220, 255 * bgOpacity / 100));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha / 255.0f);
        texture.draw(graphics, 0, 0, contentX, contentY, paintW, paintH);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void drawElements(GuiGraphics graphics, TabletUiState state, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        for (String key : model.order) {
            if (key.startsWith(ORDER_IMAGE)) {
                CanvasImageLayer image = model.image(key.substring(ORDER_IMAGE.length()));
                if (image != null) {
                    drawImage(graphics, state, image, contentX, contentY, contentW, contentH);
                }
            } else if (key.startsWith(ORDER_TEXT)) {
                CanvasTextLayer text = model.text(key.substring(ORDER_TEXT.length()));
                if (text != null) {
                    drawText(graphics, state, text, contentX, contentY, contentW, contentH);
                }
            }
        }
    }

    private static void drawGuides(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        if (!state.canEdit || !state.questDetailsEditMode || (!state.snapGuideXVisible && !state.snapGuideYVisible)) {
            return;
        }
        int color = withAlpha(ModColors.WARNING, 225);
        if (state.snapGuideXVisible && state.snapGuideX >= 0 && state.snapGuideX <= contentW) {
            int x = contentX + state.snapGuideX;
            graphics.fill(x, contentY, x + 1, contentY + contentH + 1, color);
        }
        if (state.snapGuideYVisible && state.snapGuideY >= 0 && state.snapGuideY <= contentH) {
            int y = contentY + state.snapGuideY;
            graphics.fill(contentX, y, contentX + contentW + 1, y + 1, color);
        }
    }

    private static void drawImage(GuiGraphics graphics, TabletUiState state, CanvasImageLayer image, int contentX, int contentY, int contentW, int contentH) {
        int x = contentX + image.x();
        int y = contentY + image.y() - state.questDetailsDescScroll;
        if (y > contentY + contentH || y + image.h() < contentY) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x + image.w() / 2.0f, y + image.h() / 2.0f, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(image.rotation())));
        String entityId = EntityPreviewRenderer.entityId(image.asset());
        IGuiTexture texture = entityId.isBlank() ? chapterBackgroundTexture(image.asset()) : null;
        if (!entityId.isBlank()) {
            if (!EntityPreviewRenderer.renderEntityAsset(graphics, -image.w() / 2, -image.h() / 2, image.w(), image.h(), image.asset(), image.entityYaw(), image.entitySpinSpeed(), 0.0F)) {
                graphics.fill(-image.w() / 2, -image.h() / 2, image.w() / 2, image.h() / 2, withAlpha(ModColors.TEXT_MUTED, 45));
            }
        } else if (texture == null) {
            graphics.fill(-image.w() / 2, -image.h() / 2, image.w() / 2, image.h() / 2, withAlpha(ModColors.TEXT_MUTED, 45));
        } else {
            texture.draw(graphics, 0, 0, -image.w() / 2.0f, -image.h() / 2.0f, image.w(), image.h());
        }
        graphics.pose().popPose();
        if (isSelectedImage(state, image.id()) && state.canEdit && state.questDetailsEditMode) {
            drawSelection(graphics, state, contentX, contentY, contentW, contentH, image.x(), image.y(), image.w(), image.h(), image.rotation());
        }
    }

    private static void drawText(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int contentX, int contentY, int contentW, int contentH) {
        int x = contentX + text.x();
        int y = contentY + text.y() - state.questDetailsDescScroll;
        if (y > contentY + contentH || y + text.h() < contentY) {
            return;
        }
        boolean inlineEditing = state.canvasTextEditOpen && text.id().equals(state.canvasTextEditTarget)
                && text.id().equals(state.questDetailsTextEditTarget);
        CanvasTextLayer rendered = inlineEditing ? text.withText(state.canvasTextEditDraft) : text;
        graphics.pose().pushPose();
        graphics.pose().translate(x + text.w() / 2.0f, y + text.h() / 2.0f, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(text.rotation())));
        CanvasTextRenderer.drawTextLayer(graphics, state, rendered, text.w(), text.h(), inlineEditing);
        graphics.pose().popPose();
        if (isSelectedText(state, text.id()) && state.canEdit && state.questDetailsEditMode) {
            drawSelection(graphics, state, contentX, contentY, contentW, contentH, text.x(), text.y(), text.w(), text.h(), text.rotation());
        }
    }

    private static void drawSelection(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int x, int y, int w, int h, int rotation) {
        withSelectionGeometry(state, contentW, contentH, () -> CanvasElementSelectionSlot.draw(graphics, state, contentX, contentY, x, y, w, h, rotation));
    }

    private static void withSelectionGeometry(TabletUiState state, int contentW, int contentH, Runnable draw) {
        int oldContentX = state.canvasContentX;
        int oldContentY = state.canvasContentY;
        int oldContentW = state.canvasContentW;
        int oldContentH = state.canvasContentH;
        int oldOffsetX = state.canvasOffsetX;
        int oldOffsetY = state.canvasOffsetY;
        float oldZoom = state.canvasZoom;
        boolean oldGridSnap = state.gridSnapLocked;
        state.canvasContentX = 0;
        state.canvasContentY = -state.questDetailsDescScroll;
        state.canvasContentW = contentW;
        state.canvasContentH = contentH;
        state.canvasOffsetX = 0;
        state.canvasOffsetY = 0;
        state.canvasZoom = 1.0f;
        state.gridSnapLocked = state.questDetailsGridSnapLocked;
        try {
            draw.run();
        } finally {
            state.canvasContentX = oldContentX;
            state.canvasContentY = oldContentY;
            state.canvasContentW = oldContentW;
            state.canvasContentH = oldContentH;
            state.canvasOffsetX = oldOffsetX;
            state.canvasOffsetY = oldOffsetY;
            state.canvasZoom = oldZoom;
            state.gridSnapLocked = oldGridSnap;
        }
    }

    private static boolean isSelectedText(TabletUiState state, String id) {
        return id.equals(state.questDetailsSelectedTextId) || state.questDetailsSelectedTextIds.contains(id);
    }

    private static boolean isSelectedImage(TabletUiState state, String id) {
        return id.equals(state.questDetailsSelectedImageId) || state.questDetailsSelectedImageIds.contains(id);
    }
}
