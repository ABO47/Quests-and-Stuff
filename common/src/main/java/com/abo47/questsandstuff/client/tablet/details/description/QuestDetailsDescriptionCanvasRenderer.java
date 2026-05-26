package com.abo47.questsandstuff.client.tablet.details.description;


import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
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
        if (!model.gridEnabled || !QuestDetailsEditState.canEdit(state)) {
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
        int alpha = QuestDetailsDescriptionLayout.opacityAlpha(model.canvasBgOpacityPercent);
        if (alpha <= 0) {
            return;
        }
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
        if (!QuestDetailsEditState.canEdit(state) || (!state.snapGuideXVisible && !state.snapGuideYVisible)) {
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
        CanvasImageLayer drawImage = CanvasRenderer.effectiveCanvasImage(state, image);
        int x = contentX + drawImage.x();
        int y = contentY + drawImage.y() - state.questDetailsDescScroll;
        if (y > contentY + contentH || y + drawImage.h() < contentY) {
            return;
        }
        CanvasImageLayerRenderer.draw(graphics, 0, 0, drawImage, x, y, drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY());
        if (isSelectedImage(state, drawImage.id()) && QuestDetailsEditState.canEdit(state) && selectedCount(state) <= 1) {
            drawImageSelection(graphics, state, contentX, contentY, contentW, contentH, drawImage);
        }
    }

    private static void drawText(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int contentX, int contentY, int contentW, int contentH) {
        CanvasTextLayer drawText = CanvasRenderer.effectiveCanvasText(state, text);
        int x = contentX + drawText.x();
        int y = contentY + drawText.y() - state.questDetailsDescScroll;
        if (y > contentY + contentH || y + drawText.h() < contentY) {
            return;
        }
        boolean inlineEditing = state.canvasTextEditOpen && drawText.id().equals(state.canvasTextEditTarget)
                && drawText.id().equals(state.questDetailsTextEditTarget);
        CanvasTextLayer rendered = inlineEditing ? drawText.withText(state.canvasTextEditDraft) : drawText;
        graphics.pose().pushPose();
        graphics.pose().translate(x + drawText.w() / 2.0f, y + drawText.h() / 2.0f, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawText.rotation())));
        CanvasTextRenderer.drawTextLayer(graphics, state, rendered, drawText.w(), drawText.h(), inlineEditing);
        graphics.pose().popPose();
        if (isSelectedText(state, drawText.id()) && QuestDetailsEditState.canEdit(state) && selectedCount(state) <= 1) {
            drawSelection(graphics, state, contentX, contentY, contentW, contentH, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
        }
    }

    private static void drawSelection(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int x, int y, int w, int h, int rotation) {
        withSelectionGeometry(state, contentW, contentH, () -> CanvasElementSelectionSlot.draw(graphics, state, contentX, contentY, x, y, w, h, rotation));
    }

    private static void drawImageSelection(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH, CanvasImageLayer image) {
        withSelectionGeometry(state, contentW, contentH, () -> {
            if (CanvasTransformGizmo.supports(image.asset())) {
                CanvasTransformGizmo.drawAtPivot(graphics, state, contentX, contentY, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch());
            } else {
                CanvasElementSelectionSlot.draw(graphics, state, contentX, contentY, image.x(), image.y(), image.w(), image.h(), image.rotation());
            }
        });
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

    private static int selectedCount(TabletUiState state) {
        return QuestDetailsDescriptionSelectionState.selectionSet(state).size();
    }
}
