package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;


import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class CanvasSelectionRenderer {
    private static final int SELECTION_PAD = GRID_4;
    private static final int SINGLE_SELECTION_PAD = GRID_1;
    private static final int HANDLE_SIZE = GRID_5;
    private static final int ROTATED_SELECTION_THICKNESS = GRID_2;

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
        if (!state.canvas.selectionBoundsVisible) {
            return false;
        }
        return x >= state.canvas.selectionBoundsLeft && x <= state.canvas.selectionBoundsRight
                && y >= state.canvas.selectionBoundsTop && y <= state.canvas.selectionBoundsBottom;
    }

    public static boolean isSelectionResizeHandleHit(TabletUiState state, int x, int y) {
        if (!state.canvas.selectionBoundsVisible) {
            return false;
        }
        int left = state.canvas.selectionBoundsRight - HANDLE_SIZE;
        int top = state.canvas.selectionBoundsBottom - HANDLE_SIZE;
        return x >= left && x <= state.canvas.selectionBoundsRight
                && y >= top && y <= state.canvas.selectionBoundsBottom;
    }

    public static boolean isSelectionRotateHandleHit(TabletUiState state, int x, int y) {
        if (!state.canvas.selectionBoundsVisible) {
            return false;
        }
        int left = state.canvas.selectionBoundsRight - HANDLE_SIZE;
        int top = state.canvas.selectionBoundsTop;
        return x >= left && x <= state.canvas.selectionBoundsRight
                && y >= top && y <= top + HANDLE_SIZE;
    }

    public static void updateSelectionBounds(TabletUiState state, List<QuestCardLayout> cards) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int count = 0;
        String chapter = selectedChapterName(state);
        for (QuestCardLayout card : cards) {
            if (!state.canvas.canvasSelection.questIds().contains(card.questId())) {
                continue;
            }
            count++;
            minX = Math.min(minX, card.x());
            minY = Math.min(minY, card.y());
            maxX = Math.max(maxX, card.x() + card.width());
            maxY = Math.max(maxY, card.y() + card.height());
        }
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            CanvasImageLayer drawImage = CanvasLayerMutations.effectiveCanvasImage(state, image);
            if (!CanvasSelectionActions.isImageSelected(state, drawImage.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, drawImage.x(), drawImage.y(), drawImage.w(), drawImage.h(), drawImage.pivotX(), drawImage.pivotY(), drawImage.rotation());
            count++;
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
            if (!CanvasSelectionActions.isTextSelected(state, drawText.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBounds(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
            count++;
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            if (!CanvasSelectionActions.isExclusiveChoiceSelected(state, drawEc.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), drawEc.pivotX(), drawEc.pivotY(), drawEc.rotation());
            count++;
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
        }
        if (count <= 0) {
            state.canvas.selectionBoundsVisible = false;
            state.canvas.selectionBoundsLeft = 0;
            state.canvas.selectionBoundsTop = 0;
            state.canvas.selectionBoundsRight = 0;
            state.canvas.selectionBoundsBottom = 0;
            return;
        }
        state.canvas.selectionBoundsVisible = true;
        if (state.canvas.draggingSelection
                && count > 1
                && state.canvas.dragStartBoundsRight > state.canvas.dragStartBoundsLeft
                && state.canvas.dragStartBoundsBottom > state.canvas.dragStartBoundsTop
                && state.canvas.dragStartSelectionRight > state.canvas.dragStartSelectionLeft
                && state.canvas.dragStartSelectionBottom > state.canvas.dragStartSelectionTop) {
            int screenDx = CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft + state.canvas.dragSelectionDeltaX)
                    - CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft);
            int screenDy = CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop + state.canvas.dragSelectionDeltaY)
                    - CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop);
            state.canvas.selectionBoundsLeft = state.canvas.dragStartSelectionLeft + screenDx;
            state.canvas.selectionBoundsTop = state.canvas.dragStartSelectionTop + screenDy;
            state.canvas.selectionBoundsRight = state.canvas.dragStartSelectionRight + screenDx;
            state.canvas.selectionBoundsBottom = state.canvas.dragStartSelectionBottom + screenDy;
            return;
        }
        if (state.canvas.rotatingSelection
                && state.canvas.rotateStartBoundsRight > state.canvas.rotateStartBoundsLeft
                && state.canvas.rotateStartBoundsBottom > state.canvas.rotateStartBoundsTop) {
            int startW = CanvasGeometry.screenWidth(state, state.canvas.rotateStartBoundsLeft, state.canvas.rotateStartBoundsRight);
            int startH = CanvasGeometry.screenHeight(state, state.canvas.rotateStartBoundsTop, state.canvas.rotateStartBoundsBottom);
            int centerX = CanvasGeometry.screenX(state, state.canvas.rotatePivotX);
            int centerY = CanvasGeometry.screenY(state, state.canvas.rotatePivotY);
            state.canvas.selectionBoundsLeft = centerX - startW / 2 - SELECTION_PAD;
            state.canvas.selectionBoundsTop = centerY - startH / 2 - SELECTION_PAD;
            state.canvas.selectionBoundsRight = state.canvas.selectionBoundsLeft + startW + SELECTION_PAD * 2;
            state.canvas.selectionBoundsBottom = state.canvas.selectionBoundsTop + startH + SELECTION_PAD * 2;
            return;
        }
        int pad = count == 1 ? SINGLE_SELECTION_PAD : SELECTION_PAD;
        state.canvas.selectionBoundsLeft = minX - pad;
        state.canvas.selectionBoundsTop = minY - pad;
        state.canvas.selectionBoundsRight = maxX + pad;
        state.canvas.selectionBoundsBottom = maxY + pad;
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
        if (!state.root.canEdit || state.questDetails.questDetailsOpen) {
            return;
        }
        drawBoxSelectionRect(graphics, originX, originY, maxW, maxH, state);
        drawIndividualSelectionBounds(graphics, originX, originY, maxW, maxH, state, cards);
        drawSelectionBounds(graphics, originX, originY, maxW, maxH, state);
    }

    public static void renderAlignmentGuides(WidgetGroup canvasViewport, TabletUiState state) {
        if (!state.root.canEdit) {
            return;
        }
        int color = withAlpha(TabletColors.WARNING, 225);
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (!state.canvas.snapGuideXVisible && !state.canvas.snapGuideYVisible) {
                    return;
                }
                int originX = getPositionX();
                int originY = getPositionY();
                if (state.canvas.snapGuideXVisible && state.canvas.snapGuideX >= 0 && state.canvas.snapGuideX < getSizeWidth()) {
                    int x = originX + state.canvas.snapGuideX;
                    SurfaceFactory.fill(color).draw(graphics, 0, 0, x, originY, 1, getSizeHeight());
                }
                if (state.canvas.snapGuideYVisible && state.canvas.snapGuideY >= 0 && state.canvas.snapGuideY < getSizeHeight()) {
                    int y = originY + state.canvas.snapGuideY;
                    SurfaceFactory.fill(color).draw(graphics, 0, 0, originX, y, getSizeWidth(), 1);
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
        if (!state.root.canEdit || (!state.canvas.boxSelecting && CanvasSelectionActions.totalCanvasSelectionCount(state) <= 1) || !state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            return;
        }
        int fill = withAlpha(TabletColors.INTERACTIVE, 14);
        int border = withAlpha(TabletColors.INTERACTIVE, 180);
        for (QuestCardLayout card : cards) {
            if (!state.canvas.canvasSelection.questIds().contains(card.questId())) {
                continue;
            }
            int x = card.x();
            int y = card.y();
            if (state.canvas.draggingSelection && state.canvas.dragStartPositions.containsKey(card.questId())) {
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
        String chapter = selectedChapterName(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            CanvasImageLayer drawImage = CanvasLayerMutations.effectiveCanvasImage(state, image);
            if (!CanvasSelectionActions.isImageSelected(state, drawImage.id())) {
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
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
            if (!CanvasSelectionActions.isTextSelected(state, drawText.id())) {
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
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            if (!CanvasSelectionActions.isExclusiveChoiceSelected(state, drawEc.id())) {
                continue;
            }
            int[] bounds = CanvasElementSelectionSlot.screenBoundsAtPivot(state, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), drawEc.pivotX(), drawEc.pivotY(), drawEc.rotation());
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
        if (!state.canvas.boxSelecting) {
            return;
        }
        int minX = Math.min(state.canvas.boxStartX, state.canvas.boxCurrentX);
        int minY = Math.min(state.canvas.boxStartY, state.canvas.boxCurrentY);
        int boxW = Math.max(1, Math.abs(state.canvas.boxCurrentX - state.canvas.boxStartX));
        int boxH = Math.max(1, Math.abs(state.canvas.boxCurrentY - state.canvas.boxStartY));
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
                withAlpha(TabletColors.INTERACTIVE, 48),
                TabletColors.INTERACTIVE
        );
    }

    private static int selectionDragScreenX(TabletUiState state) {
        return CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft + state.canvas.dragSelectionDeltaX)
                - CanvasGeometry.screenX(state, state.canvas.dragStartBoundsLeft);
    }

    private static int selectionDragScreenY(TabletUiState state) {
        return CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop + state.canvas.dragSelectionDeltaY)
                - CanvasGeometry.screenY(state, state.canvas.dragStartBoundsTop);
    }

    private static void drawSelectionBounds(
            GuiGraphics graphics,
            int originX,
            int originY,
            int maxW,
            int maxH,
            TabletUiState state
    ) {
        if (!state.root.canEdit || !state.canvas.selectionBoundsVisible || state.questDetails.pendingQuestTitleChangeId != null && !state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            return;
        }
        if (state.canvas.boxSelecting) {
            return;
        }
        if (state.canvas.canvasSelection.questIds().isEmpty() && CanvasSelectionActions.totalCanvasSelectionCount(state) == 1) {
            return;
        }
        if (state.canvas.rotatingSelection && CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
            drawRotatedSelectionBounds(graphics, originX, originY, state);
            return;
        }
        int left = state.canvas.selectionBoundsLeft;
        int top = state.canvas.selectionBoundsTop;
        int width = Math.max(1, state.canvas.selectionBoundsRight - state.canvas.selectionBoundsLeft);
        int height = Math.max(1, state.canvas.selectionBoundsBottom - state.canvas.selectionBoundsTop);
        drawClippedRect(graphics, originX, originY, maxW, maxH, left, top, width, height, withAlpha(TabletColors.INTERACTIVE, 26), withAlpha(TabletColors.INTERACTIVE, 214));

        int resizeX = state.canvas.selectionBoundsRight - HANDLE_SIZE;
        int resizeY = state.canvas.selectionBoundsBottom - HANDLE_SIZE;
        drawClippedRect(graphics, originX, originY, maxW, maxH, resizeX, resizeY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(TabletColors.SURFACE_BASE, 230), TabletColors.BORDER_BASE);

        if (CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
            int rotateX = state.canvas.selectionBoundsRight - HANDLE_SIZE;
            int rotateY = state.canvas.selectionBoundsTop;
            drawClippedRect(graphics, originX, originY, maxW, maxH, rotateX, rotateY, HANDLE_SIZE, HANDLE_SIZE, withAlpha(TabletColors.WARNING, 210), TabletColors.WARNING);
        }
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
            SurfaceFactory.fill(fill).draw(graphics, 0, 0, originX + left, originY + top, right - left, bottom - top);
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
        SurfaceFactory.fill(color).draw(graphics, 0, 0, originX + clippedLeft, originY + clippedTop, clippedRight - clippedLeft, clippedBottom - clippedTop);
    }

    private static void drawRotatedSelectionBounds(GuiGraphics graphics, int originX, int originY, TabletUiState state) {
        int width = CanvasGeometry.screenWidth(state, state.canvas.rotateStartBoundsLeft, state.canvas.rotateStartBoundsRight) + SELECTION_PAD * 2;
        int height = CanvasGeometry.screenHeight(state, state.canvas.rotateStartBoundsTop, state.canvas.rotateStartBoundsBottom) + SELECTION_PAD * 2;
        int pivotX = CanvasGeometry.screenX(state, state.canvas.rotatePivotX);
        int pivotY = CanvasGeometry.screenY(state, state.canvas.rotatePivotY);
        int color = withAlpha(TabletColors.INTERACTIVE, 225);

        int halfW = width / 2;
        int halfH = height / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(originX + pivotX, originY + pivotY, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) state.canvas.rotatePreviewAngle));
        drawRotatedOutline(graphics, -halfW, -halfH, width - halfW, height - halfH, ROTATED_SELECTION_THICKNESS, color);
        graphics.pose().popPose();
    }

    private static void drawRotatedOutline(GuiGraphics graphics, int left, int top, int right, int bottom, int thickness, int color) {
        int t = Math.max(1, thickness);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, left, top, right - left, t);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, left, bottom - t, right - left, t);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, left, top, t, bottom - top);
        SurfaceFactory.fill(color).draw(graphics, 0, 0, right - t, top, t, bottom - top);
    }
}
