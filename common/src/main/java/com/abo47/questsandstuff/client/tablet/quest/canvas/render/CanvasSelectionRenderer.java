package com.abo47.questsandstuff.client.tablet.quest.canvas.render;


import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class CanvasSelectionRenderer {
    private static final int SELECTION_PAD = 4;
    private static final int SINGLE_SELECTION_PAD = 1;
    private static final int HANDLE_SIZE = 5;
    private static final int ROTATED_SELECTION_THICKNESS = 2;

    private CanvasSelectionRenderer() {
    }

    public static void renderSelectionOverlay(WidgetGroup canvasViewport, TabletUiState state, List<QuestCardLayout> visibleCards) {
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawSelectionForeground(
                        graphics,
                        state,
                        visibleCards,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight()
                );
            }
        });
    }

    public static boolean isSelectionBoundsHit(TabletUiState state, int x, int y) {
        if (!state.selectionBoundsVisible) {
            return false;
        }
        return x >= state.selectionBoundsLeft && x <= state.selectionBoundsRight
                && y >= state.selectionBoundsTop && y <= state.selectionBoundsBottom;
    }

    public static boolean isSelectionResizeHandleHit(TabletUiState state, int x, int y) {
        if (!state.selectionBoundsVisible) {
            return false;
        }
        int left = state.selectionBoundsRight - HANDLE_SIZE;
        int top = state.selectionBoundsBottom - HANDLE_SIZE;
        return x >= left && x <= state.selectionBoundsRight
                && y >= top && y <= state.selectionBoundsBottom;
    }

    public static boolean isSelectionRotateHandleHit(TabletUiState state, int x, int y) {
        if (!state.selectionBoundsVisible) {
            return false;
        }
        int left = state.selectionBoundsRight - HANDLE_SIZE;
        int top = state.selectionBoundsTop;
        return x >= left && x <= state.selectionBoundsRight
                && y >= top && y <= top + HANDLE_SIZE;
    }

    public static void updateSelectionBounds(TabletUiState state, List<QuestCardLayout> cards) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int count = 0;
        String group = selectedGroupName(state);
        for (QuestCardLayout card : cards) {
            if (!state.selectedQuestIds.contains(card.questId())) {
                continue;
            }
            count++;
            minX = Math.min(minX, card.x());
            minY = Math.min(minY, card.y());
            maxX = Math.max(maxX, card.x() + card.width());
            maxY = Math.max(maxY, card.y() + card.height());
        }
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            CanvasImageLayer drawImage = CanvasRenderer.effectiveCanvasImage(state, image);
            if (!CanvasRenderer.isImageSelected(state, drawImage.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
            count++;
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            CanvasTextLayer drawText = CanvasRenderer.effectiveCanvasText(state, text);
            if (!CanvasRenderer.isTextSelected(state, drawText.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
            count++;
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        if (count <= 0) {
            state.selectionBoundsVisible = false;
            state.selectionBoundsLeft = 0;
            state.selectionBoundsTop = 0;
            state.selectionBoundsRight = 0;
            state.selectionBoundsBottom = 0;
            return;
        }
        state.selectionBoundsVisible = true;
        if (state.draggingSelection
                && count > 1
                && state.dragStartBoundsRight > state.dragStartBoundsLeft
                && state.dragStartBoundsBottom > state.dragStartBoundsTop
                && state.dragStartSelectionRight > state.dragStartSelectionLeft
                && state.dragStartSelectionBottom > state.dragStartSelectionTop) {
            int screenDx = CanvasGeometry.screenX(state, state.dragStartBoundsLeft + state.dragSelectionDeltaX)
                    - CanvasGeometry.screenX(state, state.dragStartBoundsLeft);
            int screenDy = CanvasGeometry.screenY(state, state.dragStartBoundsTop + state.dragSelectionDeltaY)
                    - CanvasGeometry.screenY(state, state.dragStartBoundsTop);
            state.selectionBoundsLeft = state.dragStartSelectionLeft + screenDx;
            state.selectionBoundsTop = state.dragStartSelectionTop + screenDy;
            state.selectionBoundsRight = state.dragStartSelectionRight + screenDx;
            state.selectionBoundsBottom = state.dragStartSelectionBottom + screenDy;
            return;
        }
        if (state.rotatingSelection
                && state.rotateStartBoundsRight > state.rotateStartBoundsLeft
                && state.rotateStartBoundsBottom > state.rotateStartBoundsTop) {
            int startW = CanvasGeometry.screenWidth(state, state.rotateStartBoundsLeft, state.rotateStartBoundsRight);
            int startH = CanvasGeometry.screenHeight(state, state.rotateStartBoundsTop, state.rotateStartBoundsBottom);
            int centerX = CanvasGeometry.screenX(state, state.rotatePivotX);
            int centerY = CanvasGeometry.screenY(state, state.rotatePivotY);
            state.selectionBoundsLeft = centerX - startW / 2 - SELECTION_PAD;
            state.selectionBoundsTop = centerY - startH / 2 - SELECTION_PAD;
            state.selectionBoundsRight = state.selectionBoundsLeft + startW + SELECTION_PAD * 2;
            state.selectionBoundsBottom = state.selectionBoundsTop + startH + SELECTION_PAD * 2;
            return;
        }
        int pad = count == 1 ? SINGLE_SELECTION_PAD : SELECTION_PAD;
        state.selectionBoundsLeft = minX - pad;
        state.selectionBoundsTop = minY - pad;
        state.selectionBoundsRight = maxX + pad;
        state.selectionBoundsBottom = maxY + pad;
    }

    public static void drawSelectionForeground(
            GuiGraphics graphics,
            TabletUiState state,
            List<QuestCardLayout> cards,
            int originX,
            int originY,
            int maxW,
            int maxH
    ) {
        if (!state.canEdit || state.createQuestModalOpen || state.questDetailsOpen) {
            return;
        }
        drawBoxSelectionRect(graphics, originX, originY, maxW, maxH, state);
        drawIndividualSelectionBounds(graphics, originX, originY, maxW, maxH, state, cards);
        drawSelectionBounds(graphics, originX, originY, maxW, maxH, state);
    }

    public static void renderAlignmentGuides(WidgetGroup canvasViewport, TabletUiState state) {
        if (!state.canEdit) {
            return;
        }
        int color = withAlpha(ModColors.WARNING, 225);
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (!state.snapGuideXVisible && !state.snapGuideYVisible) {
                    return;
                }
                int originX = getPositionX();
                int originY = getPositionY();
                if (state.snapGuideXVisible && state.snapGuideX >= 0 && state.snapGuideX < getSizeWidth()) {
                    int x = originX + state.snapGuideX;
                    graphics.fill(x, originY, x + 1, originY + getSizeHeight(), color);
                }
                if (state.snapGuideYVisible && state.snapGuideY >= 0 && state.snapGuideY < getSizeHeight()) {
                    int y = originY + state.snapGuideY;
                    graphics.fill(originX, y, originX + getSizeWidth(), y + 1, color);
                }
            }
        });
    }

    private static void drawIndividualSelectionBounds(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            TabletUiState state,
            List<QuestCardLayout> cards
    ) {
        if (!state.canEdit || (!state.boxSelecting && CanvasRenderer.totalCanvasSelectionCount(state) <= 1) || !state.pendingQuestTitleChangeId.isBlank()) {
            return;
        }
        int fill = withAlpha(ModColors.INTERACTIVE, 14);
        int border = withAlpha(ModColors.INTERACTIVE, 180);
        for (QuestCardLayout card : cards) {
            if (!state.selectedQuestIds.contains(card.questId())) {
                continue;
            }
            int x = card.x();
            int y = card.y();
            if (state.draggingSelection && state.dragStartPositions.containsKey(card.questId())) {
                x += selectionDragScreenX(state);
                y += selectionDragScreenY(state);
            }
            drawClippedRect(
                    graphics,
                    originX,
                    originY,
                    maxW,
                    maxH,
                    x - SINGLE_SELECTION_PAD,
                    y - SINGLE_SELECTION_PAD,
                    card.width() + SINGLE_SELECTION_PAD * 2,
                    card.height() + SINGLE_SELECTION_PAD * 2,
                    fill,
                    border
            );
        }
        String group = selectedGroupName(state);
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            CanvasImageLayer drawImage = CanvasRenderer.effectiveCanvasImage(state, image);
            if (!CanvasRenderer.isImageSelected(state, drawImage.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
            drawClippedRect(
                    graphics,
                    originX,
                    originY,
                    maxW,
                    maxH,
                    bounds[0] - SINGLE_SELECTION_PAD,
                    bounds[1] - SINGLE_SELECTION_PAD,
                    bounds[2] - bounds[0] + SINGLE_SELECTION_PAD * 2,
                    bounds[3] - bounds[1] + SINGLE_SELECTION_PAD * 2,
                    fill,
                    border
            );
        }
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            CanvasTextLayer drawText = CanvasRenderer.effectiveCanvasText(state, text);
            if (!CanvasRenderer.isTextSelected(state, drawText.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
            drawClippedRect(
                    graphics,
                    originX,
                    originY,
                    maxW,
                    maxH,
                    bounds[0] - SINGLE_SELECTION_PAD,
                    bounds[1] - SINGLE_SELECTION_PAD,
                    bounds[2] - bounds[0] + SINGLE_SELECTION_PAD * 2,
                    bounds[3] - bounds[1] + SINGLE_SELECTION_PAD * 2,
                    fill,
                    border
            );
        }
    }

    private static void drawBoxSelectionRect(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            TabletUiState state
    ) {
        if (!state.boxSelecting) {
            return;
        }
        int minX = Math.min(state.boxStartX, state.boxCurrentX);
        int minY = Math.min(state.boxStartY, state.boxCurrentY);
        int boxW = Math.max(1, Math.abs(state.boxCurrentX - state.boxStartX));
        int boxH = Math.max(1, Math.abs(state.boxCurrentY - state.boxStartY));
        drawClippedRect(
                graphics,
                originX,
                originY,
                maxW,
                maxH,
                minX,
                minY,
                boxW,
                boxH,
                withAlpha(ModColors.INTERACTIVE, 48),
                ModColors.INTERACTIVE
        );
    }

    private static int selectionDragScreenX(TabletUiState state) {
        return CanvasGeometry.screenX(state, state.dragStartBoundsLeft + state.dragSelectionDeltaX)
                - CanvasGeometry.screenX(state, state.dragStartBoundsLeft);
    }

    private static int selectionDragScreenY(TabletUiState state) {
        return CanvasGeometry.screenY(state, state.dragStartBoundsTop + state.dragSelectionDeltaY)
                - CanvasGeometry.screenY(state, state.dragStartBoundsTop);
    }

    private static void drawSelectionBounds(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            TabletUiState state
    ) {
        if (!state.canEdit || !state.selectionBoundsVisible || state.pendingQuestTitleChangeId != null && !state.pendingQuestTitleChangeId.isBlank()) {
            return;
        }
        if (state.boxSelecting) {
            return;
        }
        if (state.selectedQuestIds.isEmpty() && CanvasRenderer.totalCanvasSelectionCount(state) == 1) {
            return;
        }
        if (state.rotatingSelection && CanvasRenderer.totalCanvasSelectionCount(state) > 1) {
            drawRotatedSelectionBounds(graphics, originX, originY, state);
            return;
        }
        int left = state.selectionBoundsLeft;
        int top = state.selectionBoundsTop;
        int width = Math.max(1, state.selectionBoundsRight - state.selectionBoundsLeft);
        int height = Math.max(1, state.selectionBoundsBottom - state.selectionBoundsTop);
        drawClippedRect(graphics, originX, originY, maxW, maxH, left, top, width, height, withAlpha(ModColors.INTERACTIVE, 26), withAlpha(ModColors.INTERACTIVE, 214));

        int resizeX = state.selectionBoundsRight - HANDLE_SIZE;
        int resizeY = state.selectionBoundsBottom - HANDLE_SIZE;
        drawClippedRect(graphics, originX, originY, maxW, maxH, resizeX, resizeY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(ModColors.SURFACE_BASE, 230), ModColors.BORDER_BASE);

        int rotateX = state.selectionBoundsRight - HANDLE_SIZE;
        int rotateY = state.selectionBoundsTop;
        drawClippedRect(graphics, originX, originY, maxW, maxH, rotateX, rotateY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(ModColors.WARNING, 210), ModColors.WARNING);
    }

    private static void drawClippedRect(GuiGraphics graphics, int originX, int originY, int maxW, int maxH, int x, int y, int width, int height, int fill, int border) {
        int left = Math.max(0, x);
        int top = Math.max(0, y);
        int right = Math.min(maxW, x + Math.max(1, width));
        int bottom = Math.min(maxH, y + Math.max(1, height));
        if (right <= left || bottom <= top) {
            return;
        }
        if ((fill >>> 24) != 0) {
            graphics.fill(originX + left, originY + top, originX + right, originY + bottom, fill);
        }
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y, x + width, y + 1, border);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y + height - 1, x + width, y + height, border);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x, y, x + 1, y + height, border);
        drawClippedLine(graphics, originX, originY, maxW, maxH, x + width - 1, y, x + width, y + height, border);
    }

    private static void drawClippedLine(GuiGraphics graphics, int originX, int originY, int maxW, int maxH, int left, int top, int right, int bottom, int color) {
        int clippedLeft = Math.max(0, left);
        int clippedTop = Math.max(0, top);
        int clippedRight = Math.min(maxW, right);
        int clippedBottom = Math.min(maxH, bottom);
        if (clippedRight <= clippedLeft || clippedBottom <= clippedTop) {
            return;
        }
        graphics.fill(originX + clippedLeft, originY + clippedTop, originX + clippedRight, originY + clippedBottom, color);
    }

    private static void drawRotatedSelectionBounds(GuiGraphics graphics, int originX, int originY, TabletUiState state) {
        int width = CanvasGeometry.screenWidth(state, state.rotateStartBoundsLeft, state.rotateStartBoundsRight) + SELECTION_PAD * 2;
        int height = CanvasGeometry.screenHeight(state, state.rotateStartBoundsTop, state.rotateStartBoundsBottom) + SELECTION_PAD * 2;
        int pivotX = CanvasGeometry.screenX(state, state.rotatePivotX);
        int pivotY = CanvasGeometry.screenY(state, state.rotatePivotY);
        int color = withAlpha(ModColors.INTERACTIVE, 225);

        int halfW = width / 2;
        int halfH = height / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(originX + pivotX, originY + pivotY, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) state.rotatePreviewAngle));
        drawRotatedOutline(graphics, -halfW, -halfH, width - halfW, height - halfH, ROTATED_SELECTION_THICKNESS, color);
        graphics.pose().popPose();
    }

    private static void drawRotatedOutline(GuiGraphics graphics, int left, int top, int right, int bottom, int thickness, int color) {
        int t = Math.max(1, thickness);
        graphics.fill(left, top, right, top + t, color);
        graphics.fill(left, bottom - t, right, bottom, color);
        graphics.fill(left, top, left + t, bottom, color);
        graphics.fill(right - t, top, right, bottom, color);
    }
}
