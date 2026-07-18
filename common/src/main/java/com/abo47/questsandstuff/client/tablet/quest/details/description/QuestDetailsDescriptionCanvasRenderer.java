package com.abo47.questsandstuff.client.tablet.quest.details.description;

import org.joml.Quaternionf;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasBackgroundOpacity;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel.ORDER_IMAGE;
import static com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel.ORDER_TEXT;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterBackgroundTexture;

public final class QuestDetailsDescriptionCanvasRenderer {
    private QuestDetailsDescriptionCanvasRenderer() {
    }

    public static void drawContent(GuiGraphics graphics, int mouseX, int mouseY, TabletUiState state, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        drawDescriptionBackground(graphics, state, model, contentX, contentY, contentW, contentH);
        drawGrid(graphics, mouseX, mouseY, state, contentX, contentY, contentW, contentH);
        drawElements(graphics, state, model, contentX, contentY, contentW, contentH);
        drawGuides(graphics, mouseX, mouseY, state, contentX, contentY, contentW, contentH);
    }

    private static void drawGrid(GuiGraphics graphics, int mouseX, int mouseY, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        if (!state.questDetails.questDetailsGridEnabled || !QuestDetailsEditController.canEdit(state)) {
            return;
        }
        int cell = CanvasGeometry.gridSize(state);
        int alpha = Math.max(20, Math.min(220, (255 * Math.max(0, Math.min(100, state.questDetails.questDetailsGridOpacityPercent))) / 100));
        int color = (alpha << 24) | (TabletGridControls.defaultGridColor(state) & 0x00FFFFFF);
        int spanW = contentW;
        int spanH = contentH;
        int paintW = spanW + 1;
        int paintH = spanH + 1;
        IGuiTexture line = SurfaceFactory.fill(color);
        for (int x = 0; x <= spanW; x += cell) {
            line.draw(graphics, mouseX, mouseY, contentX + x, contentY, 1, paintH);
        }
        int offset = Math.floorMod(-state.questDetails.questDetailsDescScroll, cell);
        for (int y = offset; y <= spanH; y += cell) {
            line.draw(graphics, mouseX, mouseY, contentX, contentY + y, paintW, 1);
        }
        line.draw(graphics, mouseX, mouseY, contentX + spanW, contentY, 1, paintH);
    }

    private static void drawDescriptionBackground(GuiGraphics graphics, TabletUiState state, QuestDetailsDescriptionModel model, int contentX, int contentY, int contentW, int contentH) {
        int paintW = contentW + 1;
        int paintH = contentH + 1;
        int opacityPercent = Math.max(0, Math.min(100, state.questDetails.questDetailsCanvasBgOpacityPercent));
        if (CanvasBackgroundOpacity.alpha(opacityPercent) <= 0) {
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(model.canvasBackground);
        if (texture == null) {
            CanvasBackgroundOpacity.drawFill(graphics, contentX, contentY, paintW, paintH, TabletColors.SURFACE_BASE, opacityPercent);
            return;
        }
        CanvasBackgroundOpacity.drawTexture(graphics, texture, 0, 0, contentX, contentY, paintW, paintH, opacityPercent);
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

    private static void drawGuides(GuiGraphics graphics, int mouseX, int mouseY, TabletUiState state, int contentX, int contentY, int contentW, int contentH) {
        if (!QuestDetailsEditController.canEdit(state) || (!state.canvas.snapGuideXVisible && !state.canvas.snapGuideYVisible)) {
            return;
        }
        int color = withAlpha(TabletColors.WARNING, 225);
        IGuiTexture guide = SurfaceFactory.fill(color);
        if (state.canvas.snapGuideXVisible && state.canvas.snapGuideX >= 0 && state.canvas.snapGuideX <= contentW) {
            int x = contentX + state.canvas.snapGuideX;
            guide.draw(graphics, mouseX, mouseY, x, contentY, 1, contentH + 1);
        }
        if (state.canvas.snapGuideYVisible && state.canvas.snapGuideY >= 0 && state.canvas.snapGuideY <= contentH) {
            int y = contentY + state.canvas.snapGuideY;
            guide.draw(graphics, mouseX, mouseY, contentX, y, contentW + 1, 1);
        }
    }

    private static void drawImage(GuiGraphics graphics, TabletUiState state, CanvasImageLayer image, int contentX, int contentY, int contentW, int contentH) {
        CanvasImageLayer drawImage = CanvasLayerMutations.effectiveQuestDetailsImage(state, image);
        withSelectionGeometry(state, contentW, contentH, () -> drawImageAtGeometry(graphics, state, drawImage, contentX, contentY, contentH));
        if (isSelectedImage(state, drawImage.id()) && QuestDetailsEditController.canEdit(state) && selectedCount(state) <= 1) {
            drawImageSelection(graphics, state, contentX, contentY, contentW, contentH, drawImage);
        }
    }

    private static void drawText(GuiGraphics graphics, TabletUiState state, CanvasTextLayer text, int contentX, int contentY, int contentW, int contentH) {
        CanvasTextLayer drawText = CanvasLayerMutations.effectiveQuestDetailsText(state, text);
        boolean inlineEditing = TextEditSession.isQuestDetailsEditing(state) && drawText.id().equals(state.canvas.canvasTextEditTarget);
        CanvasTextLayer rendered = inlineEditing ? drawText.withText(state.canvas.canvasTextEditDraft) : drawText;
        withSelectionGeometry(state, contentW, contentH, () -> drawTextAtGeometry(graphics, state, rendered, drawText, contentX, contentY, contentH, inlineEditing));
        if (isSelectedText(state, drawText.id()) && QuestDetailsEditController.canEdit(state) && selectedCount(state) <= 1) {
            drawSelection(graphics, state, contentX, contentY, contentW, contentH, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
        }
    }

    private static void drawImageAtGeometry(GuiGraphics graphics, TabletUiState state, CanvasImageLayer drawImage, int contentX, int contentY, int contentH) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
        int y = (int) Math.round(box.centerY() + box.top());
        if (y > contentH || y + box.height() < 0) {
            return;
        }
        CanvasImageLayerRenderer.drawAtPivot(graphics, 0, 0, drawImage, contentX + box.centerX(), contentY + box.centerY(), box.width(), box.height(), -box.left(), -box.top());
    }

    private static void drawTextAtGeometry(GuiGraphics graphics, TabletUiState state, CanvasTextLayer rendered, CanvasTextLayer geometryText, int contentX, int contentY, int contentH, boolean inlineEditing) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, geometryText.x(), geometryText.y(), geometryText.w(), geometryText.h(), geometryText.rotation());
        int y = (int) Math.round(box.centerY() + box.top());
        if (y > contentH || y + box.height() < 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(contentX + box.centerX(), contentY + box.centerY(), 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(geometryText.rotation())));
        CanvasTextRenderer.drawTextLayer(graphics, state, rendered, box.width(), box.height(), inlineEditing);
        graphics.pose().popPose();
    }

    private static void drawSelection(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH, int x, int y, int w, int h, int rotation) {
        withSelectionGeometry(state, contentW, contentH, () -> CanvasElementSelectionSlot.draw(graphics, state, contentX, contentY, x, y, w, h, rotation));
    }

    private static void drawImageSelection(GuiGraphics graphics, TabletUiState state, int contentX, int contentY, int contentW, int contentH, CanvasImageLayer image) {
        withSelectionGeometry(state, contentW, contentH, () -> {
            if (CanvasTransformGizmo.supports(image.asset())) {
                CanvasTransformGizmo.drawAtPivot(graphics, state, contentX, contentY, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch());
            } else {
                CanvasElementSelectionSlot.drawAtPivot(graphics, state, contentX, contentY, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            }
        });
    }

    private static void withSelectionGeometry(TabletUiState state, int contentW, int contentH, Runnable draw) {
        int oldContentX = state.canvas.canvasContentX;
        int oldContentY = state.canvas.canvasContentY;
        int oldContentW = state.canvas.canvasContentW;
        int oldContentH = state.canvas.canvasContentH;
        int oldOffsetX = state.canvas.canvasOffsetX;
        int oldOffsetY = state.canvas.canvasOffsetY;
        float oldZoom = state.canvas.canvasZoom;
        boolean oldGridSnap = state.canvas.gridSnapLocked;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = -state.questDetails.questDetailsDescScroll;
        state.canvas.canvasContentW = contentW;
        state.canvas.canvasContentH = contentH;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;
        state.canvas.gridSnapLocked = state.questDetails.questDetailsGridSnapLocked;
        try {
            draw.run();
        } finally {
            state.canvas.canvasContentX = oldContentX;
            state.canvas.canvasContentY = oldContentY;
            state.canvas.canvasContentW = oldContentW;
            state.canvas.canvasContentH = oldContentH;
            state.canvas.canvasOffsetX = oldOffsetX;
            state.canvas.canvasOffsetY = oldOffsetY;
            state.canvas.canvasZoom = oldZoom;
            state.canvas.gridSnapLocked = oldGridSnap;
        }
    }

    private static boolean isSelectedText(TabletUiState state, String id) {
        return id.equals(state.questDetails.questDetailsDescriptionSelection.primaryTextId()) || state.questDetails.questDetailsDescriptionSelection.textIds().contains(id);
    }

    private static boolean isSelectedImage(TabletUiState state, String id) {
        return id.equals(state.questDetails.questDetailsDescriptionSelection.primaryImageId()) || state.questDetails.questDetailsDescriptionSelection.imageIds().contains(id);
    }

    private static int selectedCount(TabletUiState state) {
        return QuestDetailsDescriptionSelectionState.selectionSet(state).size();
    }
}
