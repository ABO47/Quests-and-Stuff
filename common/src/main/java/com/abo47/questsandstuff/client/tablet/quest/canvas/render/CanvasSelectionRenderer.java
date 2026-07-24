package com.abo47.questsandstuff.client.tablet.quest.canvas.render;
import java.util.List;
import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

public final class CanvasSelectionRenderer {
    private static final int SELECTION_PAD = GRID_4;
    private static final int SINGLE_SELECTION_PAD = GRID_1;
    private static final int HANDLE_SIZE = GRID_5;
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
            int qx = CanvasGeometry.screenX(state, card.visualLogicalX());
            int qy = CanvasGeometry.screenY(state, card.visualLogicalY());
            int qw = CanvasGeometry.screenSpan(state, card.logicalWidth());
            int qh = CanvasGeometry.screenSpan(state, card.logicalHeight());
            minX = Math.min(minX, qx);
            minY = Math.min(minY, qy);
            maxX = Math.max(maxX, qx + qw);
            maxY = Math.max(maxY, qy + qh);
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
        CanvasElementSelectionSlot.drawBoxSelection(
                graphics,
                originX,
                originY,
                maxW,
                maxH,
                state.canvas.boxStartX,
                state.canvas.boxStartY,
                state.canvas.boxCurrentX,
                state.canvas.boxCurrentY
        );
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
        if (CanvasSelectionActions.totalCanvasSelectionCount(state) == 1) {
            return;
        }
        if (state.canvas.rotatingSelection && CanvasSelectionActions.totalCanvasSelectionCount(state) > 1) {
            drawRotatedSelectionBounds(graphics, originX, originY, state);
            return;
        }
        int left = state.canvas.selectionBoundsLeft;
        int top = state.canvas.selectionBoundsTop;
        int right = state.canvas.selectionBoundsRight;
        int bottom = state.canvas.selectionBoundsBottom;
        CanvasElementSelectionSlot.drawCombinedBounds(graphics, originX, originY, maxW, maxH, left, top, right, bottom, CanvasSelectionActions.totalCanvasSelectionCount(state) > 1);
    }

    private static void drawRotatedSelectionBounds(GuiGraphics graphics, int originX, int originY, TabletUiState state) {
        int startW = CanvasGeometry.screenWidth(state, state.canvas.rotateStartBoundsLeft, state.canvas.rotateStartBoundsRight);
        int startH = CanvasGeometry.screenHeight(state, state.canvas.rotateStartBoundsTop, state.canvas.rotateStartBoundsBottom);
        int pivotScreenX = CanvasGeometry.screenX(state, state.canvas.rotatePivotX);
        int pivotScreenY = CanvasGeometry.screenY(state, state.canvas.rotatePivotY);
        CanvasElementSelectionSlot.drawRotatedCombinedBounds(graphics, originX, originY, pivotScreenX, pivotScreenY, startW, startH, state.canvas.rotatePreviewAngle);
    }
}
